package it.unipi.dii.inginf.lsdb.gameflows.comment;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.inginf.lsdb.gameflows.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.conversions.Bson;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.summary.ResultSummary;

import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;
import static com.mongodb.client.model.Aggregates.*;
import static org.neo4j.driver.Values.parameters;


class CommentServiceImpl implements CommentService {
	private static final Logger LOGGER = LogManager.getLogger(CommentServiceImpl.class);


//---------------------------NEO4J-QUERIES---------------------------//

	/**
	 * Create a comment node in neo4j. It also creates the :REPLY and
	 * :WRITES relationships between Post-Comment and Comment-User.
	 * @param tx neo4j transaction object
	 * @param id id of comment to insert
	 * @param author username of the author
	 * @param postId id of the comment to which the comment reply
	 * @return result of operation
	 */
	private static Result createCommentNode (@NotNull Transaction tx,
	                                         @NotNull ObjectId id,
	                                         @NotNull String author,
	                                         @NotNull ObjectId postId)
	{
		return tx.run("MATCH (u:User {username: $author}) " +
				"MATCH (p:Post {post_id: $post})" +
				"MERGE (p)<-[r:REPLY]-(c: Comment {comment_id: $id})<-[:WRITES]-(u) " +
				"ON CREATE SET r.timestamp = datetime()",
			parameters(
				"author", author,
				"id", id.toString(),
				"post", postId.toString()
			));
	}

	/**
	 * Create a :LIKES operation between User and Comment
	 * @param tx neo4j transaction object
	 * @param id id of the comment
	 * @param user username of the user
	 * @return result of operation
	 */
	private static Result createLikeRelationship (@NotNull Transaction tx,
	                                              @NotNull ObjectId id,
	                                              @NotNull String user)
	{
		return tx.run("MATCH (c:Comment {comment_id:$id}) "+
						"MATCH (u:User {username: $user}) "+
						"MERGE (u)-[r:LIKES]->(c) " +
						"ON CREATE SET r.timestamp = datetime()",
				parameters("id", id.toString(), "user", user));
	}

	/**
	 * Remove a :LIKES operation between User and Comment
	 * @param tx neo4j transaction object
	 * @param id id of the comment
	 * @param author username of the user
	 * @return result of operation
	 */
	private static Result deleteLikeRelationship (@NotNull Transaction tx,
	                                              @NotNull ObjectId id,
	                                              @NotNull String author)
	{
		return tx.run("MATCH (u:User {username:$author})"+
						"-[r:LIKES]->(c:Comment{comment_id:$id})  "+
						"DELETE r",
				parameters("id", id.toString(), "author", author));
	}


	/**
	 * Delete a Comment node by id
	 * @param tx neo4j transaction object
	 * @param id id of the post to remove
	 * @return result of operation
	 */
	private static Result deleteCommentNodeById (@NotNull Transaction tx,
	                                             @NotNull ObjectId id)
	{
		return tx.run("MATCH (c:Comment{comment_id: $id}) " +
						"DETACH DELETE c",
				parameters("id", id.toString())
		);
	}

	/**
	 * Match all the comments liked by a given user that reply to a specified post
	 * @param tx neo4j transaction object
	 * @param username username of user
	 * @param post id of post
	 * @return result of query
	 */
	private static Result matchLikedComments(@NotNull Transaction tx,
	                                         @NotNull String username,
	                                         @NotNull ObjectId post)
	{
		return tx.run("MATCH (u: User {username: $username})-[:LIKES]->(c:Comment)" +
						"-[:REPLY]->(p:Post {post_id: $post}) " +
						"RETURN c.comment_id as comment",
				parameters("username", username, "post", post.toString()));
	}







//---------------------------CREATE----------------------------------//

	/**
	 * Insert a comment in the databases.
	 * @param comment comment to insert
	 * @return MongoDB id of the inserted
	 */
	@Override
	public ObjectId addComment (@NotNull Comment comment){
		try(MongoConnection mongoConnection = PersistenceFactory.getMongoConnection();
		    Neo4jConnection neo4jConnection = PersistenceFactory.getNeo4jConnection()
		){
			LOGGER.info("addComment() | adding comment");
			return addComment(mongoConnection, neo4jConnection, comment);

		}catch (Exception e) {
			LOGGER.error("addComment() | " +
					"Connection to MongoDB or Neo4j failed due to errors: " + e);
		}
		return null;
	}

	/**
	 * Insert a comment in the databases.
	 * @param mongoConnection an already opened MongoDB connection
	 * @param neo4jConnection an already opened Neo4j connection
	 * @param comment comment to insert
	 * @return MongoDB id of the inserted
	 */
	@Override
	public ObjectId addComment(MongoConnection mongoConnection,
	                           Neo4jConnection neo4jConnection,
	                           @NotNull Comment comment)
	{
		if(mongoConnection == null){
			LOGGER.fatal("addComment() | MongoDB connection cannot be null!");
			throw new IllegalArgumentException("MongoDB connection cannot be null!");
		}
		if(neo4jConnection == null){
			LOGGER.fatal("addComment() | Neo4j connection cannot be null!");
			throw new IllegalArgumentException("Neo4j connection cannot be null!");
		}

		// Check neo4j connectivity (reduce need of rollback)
		if (!neo4jConnection.verifyConnectivity()) {
			LOGGER.error("addComment() | Neo4j connection is down");
			return null;
		}

		//Insert on MongoDB
		MongoCollection<Document> comments = mongoConnection.getCollection(GameflowsCollection.comment);
		ObjectId insertedCommentId;

		try {
			InsertOneResult result = comments.insertOne(comment.toDocument());

			if (result.getInsertedId() == null) {
				LOGGER.error("addComment() | Couldn't retrieve inserted ObjectId");
				return null;
			}

			insertedCommentId = result.getInsertedId().asObjectId().getValue();
			LOGGER.info("addComment() | " +
					"Inserted new comment in MongoDB, with id " + insertedCommentId.toString());

		} catch (MongoException me) {
			LOGGER.error("addComment() | Insertion new comment in MongoDB failed: " + me);
			return null;
		}

		// Insert on Neo4j
		try (Session session = neo4jConnection.getSession()) {
			ResultSummary resultSummary = session.writeTransaction(
					tx -> createCommentNode(tx,
						insertedCommentId,
						comment.getAuthor(),
						comment.getPost().getPostId()
					).consume()
			);

			if (resultSummary.counters().nodesCreated() == 0 ||
				resultSummary.counters().relationshipsCreated() != 2)
			{
				LOGGER.error("addComment() | Unable to create comment node on Neo4j");
				return null;
			}
			LOGGER.info("addComment() | comment node created on Neo4j");

		} catch (Neo4jException ne) {
			LOGGER.error("addComment() | Creation of Neo4j comment node failed: " + ne);
			return null;
		}

		return insertedCommentId;
	}


//---------------------------READ/VIEW----------------------------------//

	/**
	 * Find a comment by _id
	 * @param id id of the comment
	 * @return the requested comment on success, null otherwise
	 */
	@Override
	public Comment find(ObjectId id){
		try(MongoConnection connection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("find() | view all comments for a single post");
			return find(connection, id);

		}catch (Exception e) {
			LOGGER.error("find() | Connection to MongoDB failed: " + e);
		}
		return null;
	}

	/**
	 * Find a comment by _id
	 * @param mongoConnection an already opened MongoDB connection
	 * @param id id of the comment
	 * @return the requested comment on success, null otherwise
	 */
	@Override
	public Comment find(MongoConnection mongoConnection, ObjectId id){
		if(mongoConnection == null){
			LOGGER.fatal("find() | Database connection cannot be null!");
			throw new IllegalArgumentException("Database connection cannot be null!");
		}

		MongoCollection<Document> comments = mongoConnection.getCollection(GameflowsCollection.comment);

		try (MongoCursor<Document> cursor =
				    comments.find(eq("_id", id)).iterator()
		){
			if (cursor.hasNext()){
				return Comment.fromDocument(cursor.next());
			}
			else {
				LOGGER.error("find() | Comment doesn't exist");
				return null;
			}

		} catch (MongoException me){
			LOGGER.error("find() | Error while retrieving comments, message: " + me);
			return null;
		}
	}


	/**
	 * Browse the comment of a given post
	 * @param postId id of the post
	 * @param skip how many documents to skip
	 * @param limit how many documents to return
	 * @return list of the comments on success, null otherwise
	 */
	@Override
	public List<Comment> browseByPost(ObjectId postId, int skip, int limit) {
		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("browseByPost() | post id: " + postId);
			return browseByPost(mongoConnection, postId, skip, limit);

		} catch (Exception e) {
			LOGGER.error("browseByPost() | " +
					"Connection to MongoDB failed, message: " + e);
		}
		return null;
	}

	/**
	 * Browse the comment of a given post
	 * @param connection an already opened MongoDB connection
	 * @param postId id of the post
	 * @param skip how many documents to skip
	 * @param limit how many documents to return
	 * @return list of the comments on success, null otherwise
	 */
	@Override
	public List<Comment> browseByPost(MongoConnection connection, ObjectId postId, int skip, int limit) {
		if(connection == null){
			LOGGER.fatal("browseByPost() | MongoDB connection cannot be null!");
			throw new IllegalArgumentException("MongoDB connection cannot be null!");
		}

		// Get comment collection
		MongoCollection<Document> comments = connection.getCollection(GameflowsCollection.comment);

		// Find comment by post id
		try (MongoCursor<Document> cursor =
					comments.find(eq("post.post_id", postId))
							.sort(descending("timestamp"))
							.skip(skip).limit(limit).cursor()
		){
			// Fetch result
			List<Comment> list = new ArrayList<>();
			while (cursor.hasNext()) {
				list.add(Comment.fromDocument(cursor.next()));
			}

			return list;

		} catch (MongoException ex) {
			LOGGER.error("browseByPost() | " +
					"Unable to get comment documents due to errors: " + ex);
			return null;
		}

	}


//---------------------------UPDATE----------------------------------//

	/**
	 * Increment/Decrement comment likes (like/dislike)
	 * @param commentId id of the comment
	 * @param username username of the user
	 * @param like true to add a like, false to remove a like
	 * @return true on success, false on error
	 */
	boolean likeComment(ObjectId commentId, String username, boolean like){
		try(MongoConnection mongoConnection = PersistenceFactory.getMongoConnection();
		    Neo4jConnection neo4jConnection = PersistenceFactory.getNeo4jConnection()
		){
			LOGGER.info("likeComment() | updating comment");
			return likeComment(mongoConnection, neo4jConnection, commentId, username, like);

		}catch (Exception e) {
			LOGGER.error("likeComment() | Connection to MongoDB or Neo4j failed, message: " + e);
		}
		return false;
	}

	/**
	 * Increment/Decrement comment likes (like/dislike)
	 * @param mongoConnection an already opened MongoDB connection
	 * @param neo4jConnection an already opened Neo4j connection
	 * @param commentId id of the comment
	 * @param user username of the user
	 * @param like true to add a like, false to remove a like
	 * @return true on success, false on error
	 */
	boolean likeComment (MongoConnection mongoConnection,
	                            Neo4jConnection neo4jConnection,
	                            ObjectId commentId,
	                            String user, boolean like
	){
		if(mongoConnection == null){
			LOGGER.fatal("likeComment() | MongoDB connection cannot be null!");
			throw new IllegalArgumentException("MongoDB connection cannot be null!");
		}
		if(neo4jConnection == null){
			LOGGER.fatal("likeComment() | Neo4j connection cannot be null!");
			throw new IllegalArgumentException("Neo4j connection cannot be null!");
		}

		// Check mongoDB connectivity (reduce need of rollback)
		if (!mongoConnection.verifyConnectivity()) {
			LOGGER.error("likeComment() | MongoDB connection is down");
			return false;
		}

		//update on Neo4j
		try(Session session = neo4jConnection.getSession()){
			// Create or delete relationship :LIKES
			ResultSummary resultSummary = session.writeTransaction(
					tx -> {
						Result result;
						if (like){
							result = createLikeRelationship(tx, commentId, user);
						}
						else {
							result = deleteLikeRelationship(tx, commentId, user);
						}
						return result.consume();
					}
			);

			// Check if transaction failed
			if (resultSummary.counters().relationshipsCreated() == 0 &&
					resultSummary.counters().relationshipsDeleted() == 0)
			{
				LOGGER.error("likeComment() | Unable to create/delete like relationship in Neo4j");
				return false;
			}

			LOGGER.info("likeComment() | like relation created/destroyed in Neo4j");

		} catch(Neo4jException ne){
			LOGGER.error("likeComment() | " +
					"Create/Delete of LIKE relationship in neo4j failed: " + ne);
			return false;
		}

		// Get comment collection
		MongoCollection<Document> comments = mongoConnection.getCollection(GameflowsCollection.comment);

		// Increment or decrement likes counter
		Bson filter = eq("_id", commentId);
		Bson updates = Updates.inc("likes", (like ? 1 : -1));

		// Update MongoDB
		try {
			UpdateResult result = comments.updateOne(filter, updates);
			// Check update result
			if (result.getModifiedCount() == 0){
				LOGGER.error("likeComment() | " + "No comment updated");
				return false;
			}

			LOGGER.info("likeComment() | " +
					"Comment updated in MongoDB, with id " + commentId);
			return true;

		} catch (MongoException me){
			LOGGER.error("likeComment() | " +
					"Unable to update comment in MongoDB : " + me);
			return false;
		}

	}




//---------------------------DELETE----------------------------------//
	/**
	 * Delete a comment by id
	 * @param id id of the comment to delete
	 * @return true on success, false on error
	 */
	@Override
	public boolean deleteCommentById (ObjectId id){
		try(MongoConnection mongoConnection = PersistenceFactory.getMongoConnection();
		    Neo4jConnection neo4jConnection = PersistenceFactory.getNeo4jConnection()
		){
			LOGGER.info("deleteCommentById() | deleting comment");
			return deleteCommentById(mongoConnection, neo4jConnection, id);

		}catch (Exception e) {
			LOGGER.error("deleteCommentById() | Connection to MongoDB or Neo4j failed, message: " + e);
		}
		return false;
	}


	/**
	 * Delete a comment by id
	 * @param mongoConnection an already opened MongoDB connection
	 * @param neo4jConnection an already opened Neo4j connection
	 * @param id id of the comment to delete
	 * @return true on success, false on error
	 */
	@Override
	public boolean deleteCommentById(MongoConnection mongoConnection,
	                                 Neo4jConnection neo4jConnection,
	                                 ObjectId id){
		if(mongoConnection == null){
			LOGGER.fatal("deleteCommentById() | MongoDB connection cannot be null!");
			throw new IllegalArgumentException("MongoDB connection cannot be null!");
		}
		if(neo4jConnection == null){
			LOGGER.fatal("deleteCommentById() | Neo4j connection cannot be null!");
			throw new IllegalArgumentException("Neo4j connection cannot be null!");
		}

		// Check neo4j connectivity (reduce need of rollback)
		if (!neo4jConnection.verifyConnectivity()) {
			LOGGER.error("deleteCommentById() | Neo4j connection is down");
			return false;
		}

		//delete on MongoDB
		MongoCollection<Document> comments = mongoConnection.getCollection(GameflowsCollection.comment);

		try{
			DeleteResult result = comments.deleteOne(eq("_id", id));

			if (result.getDeletedCount() == 0) {
				LOGGER.warn("deleteCommentById() |  No comments deleted in MongoDB");
			}
			else {
				LOGGER.info("deleteCommentById() | comment succesfully deleted in MongoDB!");
			}

		} catch (MongoException me){
			LOGGER.error("deleteCommentById() | " +
					"Unable to delete comment in MongoDB : " + me);
			return false;
		}

		// delete on Neo4j
		try (Session session = neo4jConnection.getSession()) {
			ResultSummary resultSummary = session.writeTransaction(
					tx -> deleteCommentNodeById(tx, id).consume()
			);

			if (resultSummary.counters().nodesDeleted() == 0) {
				LOGGER.error("deleteCommentById() | Unable to delete comment node on Neo4j");
				return false;
			}

			LOGGER.info("deleteCommentById() | comment node deleted on Neo4j");

		} catch(Neo4jException ne){
			LOGGER.error("deleteCommentById() | Deletion of Neo4j comment node failed: " + ne);
			return false;
		}

		return true;
	}



	/**
	 * Delete all comments of a post on MongoDB
	 * @param postId id of the post
	 * @return true if documents were deleted, false if there is no comment to delete, null on error
	 */
	@Override
	public Boolean deleteCommentsByPostId_MongoDB (ObjectId postId) {
		try(MongoConnection mongoConnection = PersistenceFactory.getMongoConnection()){
			LOGGER.info("deleteCommentsByPostId_MongoDB() | deleting comment");
			return deleteCommentsByPostId_MongoDB(mongoConnection, postId);

		} catch (Exception e) {
			LOGGER.error("deleteCommentsByPostId_MongoDB() | Connection to MongoDB failed, message: " + e);
		}
		return null;
	}

	/**
	 * Delete all comments of a post on MongoDB
	 * @param mongoConnection an already opened MongoDB connection
	 * @param postId id of the post
	 * @return true if documents were deleted, false if there is no comment to delete, null on error
	 */
	@Override
	public Boolean deleteCommentsByPostId_MongoDB (MongoConnection mongoConnection,
	                                               ObjectId postId)
	{
		if(mongoConnection == null){
			LOGGER.fatal("deleteCommentsByPostId_MongoDB() | MongoDB connection cannot be null!");
			throw new IllegalArgumentException("MongoDB connection cannot be null!");
		}

		//delete on MongoDB
		MongoCollection<Document> comments = mongoConnection.getCollection(GameflowsCollection.comment);
		try{
			DeleteResult result = comments.deleteMany(
					eq("post.post_id", postId)
			);

			if (result.getDeletedCount() == 0){
				LOGGER.error("deleteCommentsByPostId_MongoDB() | No comments deleted in MongoDB");
				return false;
			}
			else {
				LOGGER.info("deleteCommentsByPostId_MongoDB() | " +
						"comments deleted in MongoDB: " + result.getDeletedCount());
			}
			return true;

		} catch (MongoException me){
			LOGGER.error("deleteCommentsByPostId_MongoDB() | " +
					"Unable to delete comment in MongoDB : " + me);
			return null;
		}
	}





	/**
	 * delete all comments of a videogame community
	 * @param videogameCommunityId is of the videogame community
	 * @return true if documents were deleted, false if there is no comment to delete, null on error
	 */
	@Override
	public Boolean deleteCommentsByVideogameCommunity_MongoDB(ObjectId videogameCommunityId){
		try(MongoConnection mongoConnection = PersistenceFactory.getMongoConnection()){
			LOGGER.info("deleteCommentsByVideogameCommunityId_MongoDB() | deleting comment");
			return deleteCommentsByVideogameCommunity_MongoDB(mongoConnection, videogameCommunityId);

		}catch (Exception e) {
			LOGGER.error("deleteCommentsByVideogameCommunityId_MongoDB() | " +
					"Connection to MongoDB failed, message: " + e);
		}
		return null;
	}

	/**
	 * Delete all comments of a videogame community
	 * @param mongoConnection an already opened MongoDB connection
	 * @param videogameCommunityId is of the videogame community
	 * @return true if documents were deleted, false if there is no comment to delete, null on error
	 */
	@Override
	public Boolean deleteCommentsByVideogameCommunity_MongoDB(MongoConnection mongoConnection,
	                                                          ObjectId videogameCommunityId)
	{
		if(mongoConnection == null){
			LOGGER.fatal("deleteCommentsByVideogameCommunityId_MongoDB() | " +
					"MongoDB connection cannot be null!");
			throw new IllegalArgumentException("MongoDB connection cannot be null!");
		}

		// Get collection comments
		MongoCollection<Document> comments = mongoConnection.getCollection(GameflowsCollection.comment);
		try{
			DeleteResult result = comments.deleteMany(
					eq("post.community_id", videogameCommunityId)
			);

			if (result.getDeletedCount() == 0){
				LOGGER.warn("deleteCommentsByVideogameCommunityId_MongoDB() | " +
						" No comments deleted in MongoDB");
				return false;
			}
			else {
				LOGGER.info("deleteCommentsByVideogameCommunityId_MongoDB() | " +
						"comments deleted in MongoDB: " + result.getDeletedCount());
				return true;
			}

		} catch (MongoException me){
			LOGGER.error("deleteCommentsByVideogameCommunityId_MongoDB() | " +
					"Unable to delete comment in MongoDB : " + me);
			return null;
		}
	}



//---------------------------AGGREGATIONS----------------------------------//

	/**
	 * Compute the average number of comments per post
	 * @return the results of the aggregation on success, null on failure
	 */
	@Override
	public List<ResultAverageCommentPerPost> averageNumberOfCommentsPerPost(int skip, int limit)
	{
		try(MongoConnection connection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("averageNumberOfCommentsPerPost() |  " +
					"AVG number of comments per post in a community");
			return averageNumberOfCommentsPerPost(connection, skip, limit);

		}catch (Exception e) {
			LOGGER.error("averageNumberOfCommentsPerPost() | " +
					"Connection to MongoDB failed: " + e);
		}
		return null;
	}

	/**
	 * Compute the average number of comments per post
	 * @param connection an already opened MongoDB connection
	 * @return the results of the aggregation on success, null on failure
	 */
	@Override
	public List<ResultAverageCommentPerPost> averageNumberOfCommentsPerPost(
			MongoConnection connection, int skip, int limit
	){
		if(connection == null){
			LOGGER.fatal("averageNumberOfCommentsPerPost() | " +
					"MongoDB connection cannot be null!");
			throw new IllegalArgumentException("MongoDB connection cannot be null!");
		}

		// Comment collection
		MongoCollection<Document> comments =
				connection.getCollection(GameflowsCollection.comment);

		//stage 1: group (sum/count of post in a community)
		Bson group_sum = new Document("$group",
				new Document("_id",
					new Document("post_id","$post.post_id")
						.append("community_id","$post.community_id")
						.append("community_name", "$post.community_name"))
						.append("comments_per_post", new Document("$count", new Document()))
		);

		//stage 2: group (avg comments per post of a community)
		Bson group_avg = new Document("$group",
				new Document("_id", new Document("community_id","$_id.community_id")
						.append("community_name", "$_id.community_name"))
						.append("avg_comments_per_post", new Document("$avg","$comments_per_post")));

		//stage 3: sort (desc by avg)
		Bson sort_avg = sort(descending("avg_comments_per_post"));

		try {
			// Aggregation:  average number of comments per post in a community (disc sorted)
			List<Document> documentList =
					comments.aggregate(
								Arrays.asList(
										group_sum,
										group_avg,
										sort_avg,
										skip(skip),
										limit(limit)
								)
							).into(new ArrayList<>());

			if (documentList.isEmpty()) {
				LOGGER.error("averageNumberOfCommentsPerPost() | " +
						"Aggregation returned empty result");
				return null;
			}

			// Convert results and return
			List<ResultAverageCommentPerPost> resultList = new ArrayList<>();
			for (Document doc : documentList) {
				resultList.add(
					new ResultAverageCommentPerPost(
						doc.getEmbedded(List.of("_id", "community_id"), ObjectId.class),
						doc.getEmbedded(List.of("_id", "community_name"), String.class),
						doc.getDouble("avg_comments_per_post")
					)
				);
			}

			return resultList;

		}catch (Exception e){
			LOGGER.error("averageNumberOfCommentsPerPost() | " +
					"Error while retrieving comments, message: " + e);
			return null;
		}
	}


	/**
	 * Compute the average number of comments per post for each user.
	 *
	 * @param skip how many users to skip
	 * @param limit how many users to return
	 * @return the results of the aggregation on success, null on failure
	 */
	@Override
	public List<ResultAverageCommentPerUser> averageNumberOfCommentsPerUser (int skip, int limit){
		try (MongoConnection connection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("avgCommentsPerAuthorPosts() | AVG number of comments per post for each author");
			return averageNumberOfCommentsPerUser(connection, skip, limit);

		} catch (Exception e) {
			LOGGER.error("avgCommentsPerAuthorPosts() | " +
					"Connection to MongoDB failed: " + e);
		}
		return null;
	}

	/**
	 * Compute the average number of comments per post for each user.
	 *
	 * @param connection an already opened MongoDB connection
	 * @param skip how many users to skip
	 * @param limit how many users to return
	 * @return the results of the aggregation on success, null on failure
	 */
	@Override
	public List<ResultAverageCommentPerUser> averageNumberOfCommentsPerUser (
						MongoConnection connection, int skip, int limit
	) {
		if(connection == null){
			LOGGER.fatal("averageNumberOfCommentsPerUser() | MongoDB connection cannot be null!");
			throw new IllegalArgumentException("MongoDB connection cannot be null!");
		}

		// Get comments collection
		MongoCollection<Document> comments = connection.getCollection(GameflowsCollection.comment);

		// Stage 1: group (count of post in a community)
		Bson groupSum = new Document("$group",
				new Document("_id", new Document("post_id","$post.post_id")
						.append("author","$post.author"))
						.append("comments_per_author", new Document("$count", new Document())));

		// Stage 2: group (avg comments per post of a community)
		Bson groupAvg = new Document("$group",
				new Document("_id", new Document("author","$_id.author"))
						.append("avg_comments_per_author", new Document("$avg","$comments_per_author")));

		// Stage 3: sort (desc by avg)
		Bson groupSort = sort(descending("avg_comments_per_author"));

		try {
			// Aggregation: AVG number of comments per post for each author (disc sorted)
			List<Document> documentList =
					comments.aggregate(
							Arrays.asList(
									groupSum,
									groupAvg,
									groupSort,
									skip(skip),
									limit(limit)
							)
					).into(new ArrayList<>());

			if (documentList.isEmpty()) {
				LOGGER.error("averageNumberOfCommentsPerUser() | " +
						"Aggregation returned empty result");
				return null;
			}

			// Convert MongoDB result to Java objects
			List<ResultAverageCommentPerUser> resultList = new ArrayList<>();

			for (Document doc : documentList) {
				resultList.add(
					new ResultAverageCommentPerUser(
						doc.getEmbedded(List.of("_id", "author"), String.class),
						doc.getDouble("avg_comments_per_author")
					)
				);
			}

			return resultList;


		} catch (Exception e){
			LOGGER.error("avgCommentsPerAuthorPosts() | Connection to MongoDB failed: " + e);
			return null;
		}
	}


	/**
	 * Get all the comments liked by a given user that reply to a specified post
	 * @param username username of user
	 * @param postId id of post
	 * @return set with the id of the liked comments on success, null on error
	 */
	Set<ObjectId> getLikedCommentsOfPost (@NotNull String username, @NotNull ObjectId postId) {
		try (Neo4jConnection neo4jConnection = PersistenceFactory.getNeo4jConnection()) {
			LOGGER.info("getLikedCommentsOfPost() | " +
					"user: " + username + ", post " + postId);
			return getLikedCommentsOfPost(neo4jConnection, username, postId);

		} catch (Exception e) {
			LOGGER.error("getLikedCommentsOfPost() | " +
					"Unable to close Neo4jConnection instance " +
					"due to error: " + e);
		}
		return null;
	}


	/**
	 * Get all the comments liked by a given user that reply to a specified post
	 * @param neo4jConnection an already opened neo4j connection
	 * @param username username of user
	 * @param postId id of post
	 * @return set with the id of the liked comments on success, null on error
	 */
	Set<ObjectId> getLikedCommentsOfPost (@NotNull Neo4jConnection neo4jConnection,
	                                      @NotNull String username,
	                                      @NotNull ObjectId postId)
	{
		try (Session session = neo4jConnection.getSession()) {
			Set<ObjectId> commentList = session.readTransaction(
					tx -> {
						Result result = matchLikedComments(tx, username, postId);

						Set<ObjectId> list = new HashSet<>();
						while (result.hasNext()) {
							Record record = result.next();
							list.add(
									new ObjectId(
											record.get("comment").asString()
									)
							);
						}
						return list;
					}
			);

			LOGGER.info("getLikedCommentsOfPost() | " +
					"Successfully got list of liked comments: " + commentList.size());
			return commentList;

		} catch (Neo4jException ex) {
			LOGGER.error("getLikedCommentsOfPost() |" +
					"Unable to read from neo4j database due to error: " + ex);
			return null;
		}
	}

}
