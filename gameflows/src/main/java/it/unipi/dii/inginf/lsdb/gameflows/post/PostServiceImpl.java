package it.unipi.dii.inginf.lsdb.gameflows.post;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.inginf.lsdb.gameflows.comment.CommentServiceFactory;
import it.unipi.dii.inginf.lsdb.gameflows.persistence.GameflowsCollection;
import it.unipi.dii.inginf.lsdb.gameflows.persistence.MongoConnection;
import it.unipi.dii.inginf.lsdb.gameflows.persistence.Neo4jConnection;
import it.unipi.dii.inginf.lsdb.gameflows.persistence.PersistenceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.summary.ResultSummary;

import java.util.*;

import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;
import static org.neo4j.driver.Values.parameters;

class PostServiceImpl implements PostService {
	// Logger
	private static final Logger LOGGER = LogManager.getLogger(PostServiceImpl.class);


	/**
	 * Create a post node in neo4j. It also creates the :BELONGS and
	 * :WRITES relationships between Videogame-Post and Post-User
	 * @param tx neo4j transaction object
	 * @param postId id of the post to create
	 * @param author username of the author
	 * @param videogameId id of the videogame community the post belongs to
	 * @return the result of the operation
	 */
	private static Result createPostNode (@NotNull Transaction tx, @NotNull ObjectId postId,
	                                      @NotNull String author, @NotNull ObjectId videogameId)
	{
		return tx.run("MATCH (u:User {username: $user})\n" +
			"MATCH (v:Videogame {videogame_id: $videogame})\n" +
			"MERGE (v)<-[:BELONGS]-(p:Post {post_id: $post})<-[:WRITES {timestamp: datetime()}]-(u)",
			parameters("user", author,
					"videogame", videogameId.toString(),
					"post", postId.toString())
		);
	}

	/**
	 * Delete a post node from neo4j, by its id. It also removes all the comments
	 * which reply to the specified post.
	 * @param tx neo4j transaction object
	 * @param id id of the post to remove
	 * @return the result of the deletion of the post node
	 */
	private static Result deletePostNodeById (@NotNull Transaction tx,
	                                          @NotNull ObjectId id)
	{
		// Cascade effect on comments
		tx.run("MATCH (p:Post)<-[r:REPLY]-(c:Comment) " +
				"WHERE p.post_id = $post_id " +
				"DETACH DELETE c",
			parameters("post_id", id.toString()));

		return tx.run("MATCH (p: Post {post_id: $id}) " +
				"DETACH DELETE p",
			parameters("id", id.toString())
		);
	}

	/**
	 * Create a :LIKES relationship between a user and a post
	 * @param tx neo4j transaction object
	 * @param id id of the post
	 * @param user username of the user
	 * @return result of the operation
	 */
	private static Result createLikeRelationship (@NotNull Transaction tx,
	                                              @NotNull ObjectId id,
	                                              @NotNull String user)
	{
		return tx.run("MATCH (p:Post {post_id: $id}) " +
						"MATCH (u:User {username: $user}) " +
						"MERGE (u)-[r:LIKES]->(p) " +
						"ON CREATE SET r.timestamp = datetime()",
				parameters("id", id.toString(), "user", user));
	}

	/**
	 * Delete a :LIKES relationship between a user and a post
	 * @param tx neo4j transaction object
	 * @param id id of the post
	 * @param author username of the author
	 * @return result of the operation
	 */
	private static Result deleteLikeRelationship (@NotNull Transaction tx,
	                                              @NotNull ObjectId id,
	                                              @NotNull String author)
	{
		return tx.run("MATCH (u:User {username:$author})"+
				"-[r:LIKES]->(p:Post {post_id: $id}) "+
				"DELETE r",
			parameters("id", id.toString(), "author", author));
	}


	/**
	 * Match all the posts liked by a given user that belong to a specified videogame community
	 * @param tx neo4j transaction object
	 * @param username username of user
	 * @param videogame id of videogame community
	 * @return result of query
	 */
	private static Result matchLikedPosts(@NotNull Transaction tx,
	                                      @NotNull String username,
	                                      @NotNull ObjectId videogame)
	{
		return tx.run("MATCH (u: User {username: $username})-[:LIKES]->(p:Post)" +
				"-[:BELONGS]->(v:Videogame {videogame_id: $videogame})" +
				"RETURN p.post_id as post",
				parameters("username", username, "videogame", videogame.toString()));
	}









	/**
	 * Insert a post in the databases.
	 * @param post the post to insert
	 * @return the MongoDB id of the inserted document on success, null on failure
	 */
	@Override
	public ObjectId insertPost(@NotNull Post post) {
		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection();
		     Neo4jConnection neo4jConnection = PersistenceFactory.getNeo4jConnection()
		) {
			LOGGER.info("insertPost() | Insert post");
			return insertPost(mongoConnection, neo4jConnection, post);

		} catch (Exception e) {
			LOGGER.error("insertPost() | " +
				"Unable to close MongoConnection or Neo4jConnection instance " +
				"due to error: " + e);
		}
		return null;

	}


	/**
	 * Insert a post in the databases.
	 * @param mongoConnection an already opened connection to MongoDB
	 * @param neo4jConnection an already opened connection to Neo4j
	 * @param post the post to insert
	 * @return the MongoDB id of the inserted document on success, null on failure
	 */
	@Override
	public ObjectId insertPost(@NotNull MongoConnection mongoConnection,
	                           @NotNull Neo4jConnection neo4jConnection,
	                           @NotNull Post post)
	{
		// Check neo4j connectivity (reduce need of rollback)
		if (!neo4jConnection.verifyConnectivity()) {
			LOGGER.error("insertPost() | Neo4j connection is down");
			return null;
		}

		// Get MongoDB post collection
		MongoCollection<Document> posts = mongoConnection.getCollection(GameflowsCollection.post);
		ObjectId newPostId;

		// Insert in post collection
		try {
			InsertOneResult insertResult = posts.insertOne(post.toDocument());

			if (insertResult.getInsertedId() == null) {
				LOGGER.error("insertPost() | " +
					"Unable to insert new post document in MongoDB");
				return null;
			}

			newPostId = insertResult.getInsertedId().asObjectId().getValue();

		} catch (MongoException ex) {
			LOGGER.error("insertPost() | " +
				"Unable to insert new post document in MongoDB, due to errors: " + ex);
			return null;
		}

		// Insert in Neo4j
		try (Session session = neo4jConnection.getSession()) {
			ResultSummary resultSummary = session.writeTransaction(
				tx -> createPostNode(
						tx,
						newPostId,
						post.getAuthor(),
						post.getVideogameCommunity().getVideogameCommunityId()
				).consume()
			);

			if (resultSummary.counters().nodesCreated() == 0 &&
				resultSummary.counters().relationshipsCreated() == 0) {
				LOGGER.error("insertVideogameCommunity() | Unable to create videogame node");
				rollbackMongoInsertPost(mongoConnection, newPostId);
				return null;
			}

			LOGGER.info("insertVideogameCommunity() | Videogame node created");

		} catch (Neo4jException ex) {
			LOGGER.error("insertVideogameCommunity() | Creation of Neo4j node failed: " + ex);
			rollbackMongoInsertPost(mongoConnection, newPostId);
			return null;
		}

		return newPostId;


	}


	/**
	 * Rollback an insert query of a post.
	 * It deletes the specified MongoDB object from the post collection.
	 *
	 * @param connection an already opened MongoDB connection
	 * @param id id of the object to remove
	 */
	private void rollbackMongoInsertPost (@NotNull MongoConnection connection, @NotNull ObjectId id) {
		MongoCollection<Document> posts = connection.getCollection(GameflowsCollection.post);
		try {
			posts.deleteOne(eq("_id", id));
			LOGGER.info("rollbackMongoInsertPost() | Post " + id + " has been removed");

		} catch (MongoException ex) {
			LOGGER.error("rollbackMongoInsertPost() | Cannot remove post from DB");
		}
	}


	/**
	 * Browse the posts of a given videogame community.
	 * The posts can be sorted by likes or timestamp. The user can also choose
	 * to view only the posts written by influencers.
	 * @param videogameId id of the videogame community
	 * @param filter sorting field (likes/timestamp)
	 * @param influencerPost true to view only the posts written by influencers, false otherwise
	 * @param skip how many posts to skip
	 * @param limit how many posts to return
	 * @return a list of posts on success, null on failure
	 */
	@Override
	public List<Post> browsePosts(@NotNull ObjectId videogameId,
	                              @NotNull PostFilter filter, boolean influencerPost,
	                              int skip, int limit)
	{
		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("browsePosts() | Browse posts");
			return browsePosts(mongoConnection, videogameId, filter, influencerPost, skip, limit);

		} catch (Exception e) {
			LOGGER.error("browsePosts() | " +
				"Unable to close MongoConnection instance " +
				"due to error: " + e);
		}
		return null;
	}

	/**
	 * Browse the posts of a given videogame community.
	 * The posts can be sorted by likes or timestamp. The user can also choose
	 * to view only the posts written by influencers.
	 * @param connection an already opened MongoDB connection
	 * @param videogameId id of the videogame community
	 * @param filter sorting field (likes/timestamp)
	 * @param influencerPost true to view only the posts written by influencers, false otherwise
	 * @param skip how many posts to skip
	 * @param limit how many posts to return
	 * @return a list of posts on success, null on failure
	 */
	@Override
	public List<Post> browsePosts(@NotNull MongoConnection connection,
	                              @NotNull ObjectId videogameId,
	                              @NotNull PostFilter filter, boolean influencerPost,
	                              int skip, int limit)
	{
		// Get post collections
		MongoCollection<Document> posts = connection.getCollection(GameflowsCollection.post);

		// Filter by videogame community id.
		// If flag influencerPost is true, then return only posts written
		// influencer users
		Bson findQuery = eq("community.community_id", videogameId);
		if (influencerPost) {
			findQuery = and(findQuery, eq("isAuthorInfluencer", true));
		}

		// Sort by date or by number of likes?
		Bson sortQuery;
		if (filter == PostFilter.date) {
			sortQuery = orderBy(descending("timestamp"));
		}
		else if (filter == PostFilter.like) {
			sortQuery = orderBy(descending("likes"));
		}
		else {
			throw new IllegalArgumentException("Invalid post filter");
		}

		// Execute query
		try (MongoCursor<Document> result =
				posts.find(findQuery).sort(sortQuery)
					.skip(skip).limit(limit)
					.cursor()
		){
			// Fetch the result
			List<Post> list = new ArrayList<>();

			while (result.hasNext()) {
				list.add(Post.fromDocument(result.next()));
			}

			LOGGER.info("browsePosts() | " +
					"Fetched " + list.size() + " posts from MongoDB");

			return list;

		} catch (MongoException ex) {
			LOGGER.error("browsePosts() | " +
				"Unable to read posts from MongoDB due to errors: " + ex);
			return null;
		}
	}


	/**
	 * Find a post by id
	 * @param id id of the post
	 * @return the post on success, null if the post doesn't exist or on error
	 */
	@Override
	public Post find(@NotNull ObjectId id) {
		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("find() | Find post " + id);
			return find(mongoConnection, id);

		} catch (Exception e) {
			LOGGER.error("find() | " +
				"Unable to close MongoConnection instance " +
				"due to error: " + e);
		}
		return null;
	}


	/**
	 * Find a post by id
	 * @param connection an already opened MongoDB connection
	 * @param id id of the post
	 * @return the post on success, null if the post doesn't exist or on error
	 */
	@Override
	public Post find (@NotNull MongoConnection connection, @NotNull ObjectId id) {
		// Get post collection
		MongoCollection<Document> posts = connection.getCollection(GameflowsCollection.post);

		try (MongoCursor<Document> cursor = posts.find(eq("_id", id)).cursor())
		{
			if (cursor.hasNext()) {
				return Post.fromDocument(cursor.next());
			}
			else {
				LOGGER.error("find() | Unable to find post " + id + "in MongoDB");
				return null;
			}

		} catch (MongoException ex) {
			LOGGER.error("find() | Unable to find post " + id +
				" in MongoDB due to an error: " + ex);
			return null;
		}
	}


	/**
	 * Add or remove a "like" to a given post.
	 * In order to successfully add a "like", a :LIKES relationship between user and post
	 * can't already exist.
	 * In order to successfully remove a "like", a :LIKES relationship between user and post
	 * 	must already exist.
	 * @param postId id of the post
	 * @param user username of the user who wants to add/remove a like
	 * @param like true to add a like, false to remove the like
	 * @return true on success, false on failure
	 */
	boolean likePost(@NotNull ObjectId postId,
	                        @NotNull String user, boolean like)
	{
		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection();
		     Neo4jConnection neo4jConnection = PersistenceFactory.getNeo4jConnection()
		) {
			LOGGER.info("likePost() | " + ((like) ? "like" : "dislike") + " post "
					+ postId + " by user " + user);
			return likePost(mongoConnection, neo4jConnection, postId, user, like);

		} catch (Exception e) {
			LOGGER.error("likePost() | " +
				"Unable to close MongoConnection or Neo4jConnection instance " +
				"due to error: " + e);
		}
		return false;
	}

	/**
	 * Add or remove a "like" to a given post.
	 * In order to successfully add a "like", a :LIKES relationship between user and post
	 * can't already exist.
	 * In order to successfully remove a "like", a :LIKES relationship between user and post
	 * 	must already exist.
	 * @param mongoConnection an already opened connection to MongoDB
	 * @param neo4jConnection an already opened connection to Neo4j
	 * @param postId id of the post
	 * @param user username of the user who wants to add/remove a like
	 * @param like true to add a like, false to remove the like
	 * @return true on success, false on failure
	 */
	boolean likePost(@NotNull MongoConnection mongoConnection,
	                        @NotNull Neo4jConnection neo4jConnection,
	                        @NotNull ObjectId postId,
	                        @NotNull String user, boolean like)
	{
		// Check neo4j connectivity (reduce need of rollback)
		if (!mongoConnection.verifyConnectivity()) {
			LOGGER.error("likeComment() | MongoDB connection is down");
			return false;
		}

		// Update in Neo4j
		try(Session session = neo4jConnection.getSession()){
			ResultSummary resultSummary = session.writeTransaction(
					tx -> {
						Result result;
						if (like){
							// Add a like
							result = createLikeRelationship(tx, postId, user);
						}
						else {
							// Remove a like
							result = deleteLikeRelationship(tx, postId, user);
						}
						return result.consume();
					}
			);

			// Check the results
			if (resultSummary.counters().relationshipsCreated() == 0 &&
					resultSummary.counters().relationshipsDeleted() == 0)
			{
				LOGGER.error("likePost() | Unable to create/delete like relationship in Neo4j");
				return false;
			}

			LOGGER.info("likePost() | like relation created/destroyed in Neo4j");

		} catch(Neo4jException ne) {
			LOGGER.error("likePost() | " +
					"Create/Delete of LIKE relationship in neo4j failed: " + ne);
			return false;
		}

		// Get MongoDB posts collection
		MongoCollection<Document> posts = mongoConnection.getCollection(GameflowsCollection.post);

		// Increment or decrement likes counter
		Bson filter = eq("_id", postId);
		Bson updates = Updates.inc("likes", (like ? 1 : -1));

		// Update MongoDB
		try {
			UpdateResult result = posts.updateOne(filter, updates);
			if (result.getModifiedCount() == 0){
				LOGGER.error("likePost() | " + "No post updated");
				return false;
			}

			LOGGER.info("likePost() | " +
				"Post updated in MongoDB, with id " + postId);
			return true;

		} catch (MongoException me){
			LOGGER.error("likePost() | " +
				"Unable to update post in MongoDB : " + me);
			return false;
		}

	}


	/**
	 * Get all the posts liked by a given user that belong to a specified videogame community
	 * @param username username of user
	 * @param videogameId id of videogame community
	 * @return set with the id of the liked posts on success, null on error
	 */
	Set<ObjectId> getLikedPostsOfVideogameCommunity (@NotNull String username,
	                                                 @NotNull ObjectId videogameId)
	{
		try (Neo4jConnection neo4jConnection = PersistenceFactory.getNeo4jConnection()
		) {
			LOGGER.info("getLikedPostsOfVideogameCommunity() | " +
					"user: " + username + ", videogame community " + videogameId);
			return getLikedPostsOfVideogameCommunity(neo4jConnection, username, videogameId);

		} catch (Exception e) {
			LOGGER.error("getLikedPostsOfVideogameCommunity() | " +
					"Unable to close Neo4jConnection instance " +
					"due to error: " + e);
		}
		return null;
	}


	/**
	 * Get all the posts liked by a given user that belong to a specified videogame community
	 * @param neo4jConnection an already opened connection to neo4j
	 * @param username username of user
	 * @param videogameId id of videogame community
	 * @return set with the id of the liked posts on success, null on error
	 */
	Set<ObjectId> getLikedPostsOfVideogameCommunity(@NotNull Neo4jConnection neo4jConnection,
	                                                @NotNull String username,
	                                                @NotNull ObjectId videogameId)
	{
		try (Session session = neo4jConnection.getSession()) {
			Set<ObjectId> postSet = session.readTransaction(
					tx -> {
						Result result = matchLikedPosts(tx, username, videogameId);

						Set<ObjectId> set = new HashSet<>();
						while (result.hasNext()) {
							Record record = result.next();
							set.add(
									new ObjectId(
											record.get("post").asString()
									)
							);
						}
						return set;
					}
			);

			LOGGER.info("getLikedPostsOfVideogameCommunity() | " +
					"Successfully got list of liked posts: " + postSet.size());
			return postSet;

		} catch (Neo4jException ex) {
			LOGGER.error("getLikedPostsOfVideogameCommunity() |" +
					"Unable to read from neo4j database due to error: " + ex);
			return null;
		}
	}




	/**
	 * Delete a post by id
	 * @param id id of the post to delete
	 * @return true on success, false on error
	 */
	@Override
	public boolean deletePostById (@NotNull ObjectId id) {
		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection();
		     Neo4jConnection neo4jConnection = PersistenceFactory.getNeo4jConnection()
		) {
			LOGGER.info("deletePostById() | Delete post " + id);
			return deletePostById(mongoConnection, neo4jConnection, id);

		} catch (Exception e) {
			LOGGER.error("deletePostById() | " +
				"Unable to close MongoConnection or Neo4jConnection instance " +
				"due to error: " + e);
		}
		return false;
	}


	/**
	 * Delete a post by id. It also deletes all the comments of the post.
	 * @param mongoConnection an already opened MongoDB connection
	 * @param neo4jConnection an already opened Neo4j connection
	 * @param id id of the post to delete
	 * @return true on success, false on error
	 */
	@Override
	public boolean deletePostById (@NotNull MongoConnection mongoConnection,
	                               @NotNull Neo4jConnection neo4jConnection,
	                               @NotNull ObjectId id)
	{
		// Check neo4j connectivity (reduce need of rollback)
		if (!neo4jConnection.verifyConnectivity()) {
			LOGGER.error("deletePostById() | Neo4j connection is down");
			return false;
		}

		// Get post collection
		MongoCollection<Document> posts = mongoConnection.getCollection(GameflowsCollection.post);

		try {
			// Delete document
			DeleteResult result = posts.deleteOne(eq("_id", id));

			// Check result
			if (result.getDeletedCount() == 0) {
				LOGGER.error("deletePostById() |  No post deleted in MongoDB");
				return false;
			}
			else {
				LOGGER.info("deletePostById() | post successfully deleted in MongoDB!");
			}

			// Delete all the comment of the post
			boolean deleteCommentResult =
				CommentServiceFactory.getService()
					.deleteCommentsByPostId_MongoDB(mongoConnection, id);

			if (!deleteCommentResult) {
				LOGGER.warn("deletePostById() |  No comment deleted in MongoDB");
			}

		} catch (MongoException me){
			LOGGER.error("deletePostById() | " +
				"Unable to delete post in MongoDB : " + me);
			return false;
		}

		// delete on Neo4j
		try (Session session = neo4jConnection.getSession()) {
			ResultSummary resultSummary = session.writeTransaction(
				tx -> deletePostNodeById(tx, id).consume()
			);

			// Check result
			if (resultSummary.counters().nodesDeleted() == 0) {
				LOGGER.error("deletePostById() | Unable to delete post node on Neo4j");
				return false;
			}

			LOGGER.info("deletePostById() | post node deleted on Neo4j");

		} catch(Neo4jException ne){
			LOGGER.error("deletePostById() | Deletion of Neo4j post node failed: " + ne);
			return false;
		}

		return true;
	}


	/**
	 * Delete all the documents in the post collection of MongoDB
	 * which belong to a given videogame community.
	 * @param videogameCommunityId id of the videogame community
	 * @return true if the documents were successfully deleted, false if no document exist, null on error
	 */
	@Override
	public Boolean deletePostByVideogameCommunity_MongoDB(ObjectId videogameCommunityId) {
		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("deletePostByVideogameCommunity_MongoDB() | " +
					"Delete all posts of videogame " + videogameCommunityId + " from MongoDB");
			return deletePostByVideogameCommunity_MongoDB(mongoConnection, videogameCommunityId);

		} catch (Exception e) {
			LOGGER.error("deletePostByVideogameCommunity_MongoDB() | " +
				"Unable to close MongoConnection instance " +
				"due to error: " + e);
		}
		return null;

	}

	/**
	 * Delete all the documents in the post collection of MongoDB
	 * which belong to a given videogame community.
	 * @param connection an already opened MongoDB connection
	 * @param videogameCommunityId id of the videogame community
	 * @return true if the documents were successfully deleted, false if no document exist, null on error
	 */
	@Override
	public Boolean deletePostByVideogameCommunity_MongoDB(@NotNull MongoConnection connection,
	                                                      ObjectId videogameCommunityId)
	{
		// Get post collection
		MongoCollection<Document> posts = connection.getCollection(GameflowsCollection.post);

		try {
			DeleteResult result = posts.deleteMany(
				eq("community.community_id", videogameCommunityId)
			);

			if (result.getDeletedCount() == 0) {
				LOGGER.warn("deletePostByVideogameCommunity_MongoDB | " +
					"No posts to delete for videogame community " + videogameCommunityId.toString());
				return false;
			}

			LOGGER.info("deletePostByVideogameCommunity_MongoDB | " +
				"Deleted " + result.getDeletedCount() + " posts " +
				"for videogame community " + videogameCommunityId.toString());
			return true;

		} catch (MongoException ex) {
			LOGGER.error("deletePostByVideogameCommunity_MongoDB | " +
				"Unable to delete posts due to errors: " + ex);
			return null;
		}
	}


	/**
	 * MongoDB aggregation which selects the best users by number of posts
	 * in a given videogame community.
	 * @param videogameCommunityId id of the videogame community
	 * @param limit how many users to return
	 * @return the results of the aggregation on success, null on failure
	 */
	@Override
	public List<ResultBestUserByPostAggregation> bestUsersByNumberOfPosts (
			@NotNull ObjectId videogameCommunityId, int limit
	){
		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("bestUsersByNumberOfPosts() | Aggregation: best users by number of posts");
			return bestUsersByNumberOfPosts(mongoConnection, videogameCommunityId, limit);

		} catch (Exception e) {
			LOGGER.error("bestUsersByNumberOfPosts() | " +
				"Unable to close MongoConnection instance " +
				"due to error: " + e);
		}
		return null;
	}


	/**
	 * MongoDB aggregation which selects the best users by number of posts
	 * in a given videogame community.
	 * @param connection an already opened MongoDB connection
	 * @param videogameCommunityId id of the videogame community
	 * @param limit how many users to return
	 * @return the results of the aggregation on success, null on failure
	 */
	@Override
	public List<ResultBestUserByPostAggregation> bestUsersByNumberOfPosts (
			@NotNull MongoConnection connection, @NotNull ObjectId videogameCommunityId, int limit
	){
		// Get post collection
		MongoCollection<Document> posts = connection.getCollection(GameflowsCollection.post);

		// STAGE 1: match by videogame id
		Bson matchStage = match(eq("community.community_id", videogameCommunityId));
		// STAGE 2: group by author
		Bson groupStage = group("$author", sum("num_post", 1));
		// STAGE 3: sort by num_post
		Bson sortStage = sort(descending("num_post"));
		// STAGE 4: limit
		Bson limitStage = limit(limit);

		try {
			List<Document> postList =
				posts.aggregate(Arrays.asList(matchStage, groupStage, sortStage, limitStage))
					.into(new ArrayList<>());

			List<ResultBestUserByPostAggregation> resultList = new ArrayList<>();
			for (Document doc : postList) {
				resultList.add(
					new ResultBestUserByPostAggregation(
						doc.getString("_id"),
						doc.getInteger("num_post")
					)
				);
			}

			LOGGER.info("bestUsersByNumberOfPosts() | " +
				"Aggregation returned " + resultList.size() + " documents");

			return resultList;

		} catch (MongoException ex) {
			LOGGER.error("bestUsersByNumberOfPosts() | " +
				"Unable to execute the aggregation due to error: " + ex);
			return null;
		}
	}


	/**
	 * MongoDB aggregation which compute the videogame communities that received more likes
	 * in a given time period
	 * @param fromDate starting date of the time period
	 * @param toDate ending date of the time period
	 * @param limit how many videogame communities to return
	 * @return the results of the aggregation on success, null on error
	 */
	@Override
	public List<ResultBestVideogameCommunityAggregation> bestVideogameCommunities(
			@NotNull Date fromDate, @NotNull Date toDate, int limit)
	{
		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("bestVideogameCommunities() | Aggregation: best videogame communities " +
					"from " + fromDate + " to " + toDate + " by likes");
			return bestVideogameCommunities(mongoConnection, fromDate, toDate, limit);

		} catch (Exception e) {
			LOGGER.error("bestVideogameCommunities() | " +
					"Unable to close MongoConnection instance due to errors: " + e);
		}
		return null;
	}


	/**
	 * MongoDB aggregation which compute the videogame communities that received more likes
	 * in a given time period
	 * @param connection an already opened MongoDB connection
	 * @param fromDate starting date of the time period
	 * @param toDate ending date of the time period
	 * @param limit how many videogame communities to return
	 * @return the results of the aggregation on success, null on error
	 */
	@Override
	public List<ResultBestVideogameCommunityAggregation> bestVideogameCommunities(
			@NotNull MongoConnection connection, @NotNull Date fromDate,
			@NotNull Date toDate, int limit)
	{
		// Get post collection
		MongoCollection<Document> posts = connection.getCollection(GameflowsCollection.post);

		// STAGE 1: match timestamp
		Bson matchStage = match(
				and(
						gte("timestamp", fromDate),
						lte("timestamp", toDate)
				)
		);

		// STAGE 2: group by videogame community
		Bson groupStage = new Document("$group",
				new Document("_id",
						new Document("id", "$community.community_id")
								.append("name", "$community.community_name")
				)
				.append("sum_likes", new Document("$sum", "$likes")));

		// STAGE 3: sort by likes
		Bson sortStage = sort(descending("sum_likes"));

		// STAGE 4: limit
		Bson limitStage = limit(limit);

		try {
			// Execute aggregation
			List<Document> postList =
					posts.aggregate(Arrays.asList(matchStage, groupStage, sortStage, limitStage))
							.into(new ArrayList<>());

			// Fetch results
			List<ResultBestVideogameCommunityAggregation> resultList = new ArrayList<>();
			for (Document doc : postList) {
				resultList.add(
						new ResultBestVideogameCommunityAggregation(
								doc.getEmbedded(List.of("_id", "id"), ObjectId.class),
								doc.getEmbedded(List.of("_id", "name"), String.class),
								doc.getInteger("sum_likes")
						)
				);
			}

			LOGGER.info("bestVideogameCommunities() | " +
					"Aggregation returned " + resultList.size() + " documents");

			return resultList;

		} catch (MongoException ex) {
			LOGGER.error("bestVideogameCommunities() | " +
					"Unable to execute the aggregation due to error: " + ex);
			return null;
		}

	}


}

