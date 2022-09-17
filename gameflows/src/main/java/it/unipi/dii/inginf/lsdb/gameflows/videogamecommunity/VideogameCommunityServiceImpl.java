package it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import it.unipi.dii.inginf.lsdb.gameflows.comment.CommentServiceFactory;
import it.unipi.dii.inginf.lsdb.gameflows.persistence.*;
import it.unipi.dii.inginf.lsdb.gameflows.post.PostServiceFactory;
import it.unipi.dii.inginf.lsdb.gameflows.user.User;
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

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.*;
import static org.neo4j.driver.Values.parameters;

class VideogameCommunityServiceImpl implements VideogameCommunityService {
	// Logger
	private static final Logger LOGGER = LogManager.getLogger(VideogameCommunityServiceImpl.class);


	//////////////////////////////////////////////////////
	//////              NEO4J QUERY                  /////
	//////////////////////////////////////////////////////
	/**
	 * Create a Videogame node into the Neo4j database
	 * @param tx transaction object
	 * @param id MongoDB inserted id
	 * @param name name of the videogame community
	 * @return the result of the operation
	 */
	private static Result createVideogameNode (@NotNull Transaction tx,
	                                           @NotNull ObjectId id,
	                                           @NotNull String name)
	{
		return tx.run("MERGE (v: Videogame {videogame_id: $id, name: $name})",
				parameters("id", id.toString(), "name", name));
	}

	/**
	 * Delete a Videogame node from the Neo4j database.
	 * It also removes all the related comments and posts.
	 * @param tx transaction object
	 * @param id MongoDB id
	 * @return the result of the query which delete the videogame node
	 */
	private static Result deleteVideogameNode (@NotNull Transaction tx,
	                                           @NotNull ObjectId id)
	{
		tx.run("MATCH (v:Videogame)<-[:BELONGS]-(:Post)<-[:REPLY]-(c:Comment) " +
						"where v.videogame_id = $id detach delete c ",
				parameters("id", id.toString()));
		tx.run("MATCH (v:Videogame)<-[:BELONGS]-(p:Post)" +
						"where v.videogame_id = $id detach delete p ",
				parameters("id", id.toString()));
		return tx.run("MATCH (v:Videogame)" +
						"where v.videogame_id = $id detach delete v",
				parameters("id", id.toString()));
	}

	/**
	 * Create a FOLLOWS relationship between a Videogame node and a User node
	 * @param tx neo4j transaction object
	 * @param videogame id of the videogame
	 * @param username username
	 * @return result of the query
	 */
	private static Result createFollowsRelationship (@NotNull Transaction tx,
	                                                 @NotNull ObjectId videogame,
	                                                 @NotNull String username)
	{
		return tx.run("MATCH (v: Videogame {videogame_id: $videogame}) " +
						"MATCH (u:User {username: $user}) " +
						"MERGE (u)-[:FOLLOWS]->(v)",
				parameters("videogame", videogame.toString(), "user", username));
	}

	/**
	 * Delete a FOLLOWS relationship between a Videogame node and a User node
	 * @param tx neo4j transaction object
	 * @param videogame id of the videogame
	 * @param username username
	 * @return result of the query
	 */
	private static Result deleteFollowsRelationship (@NotNull Transaction tx,
	                                                 @NotNull ObjectId videogame,
	                                                 @NotNull String username)
	{
		return tx.run("MATCH (u:User {username: $user})" +
						"-[f:FOLLOWS]->" +
						"(v:Videogame {videogame_id: $videogame}) " +
						"DELETE f",
				parameters("videogame", videogame.toString(), "user", username));
	}

	/**
	 * Find all the videogames followed by a given user
	 *
	 * @param tx       neo4j transaction object
	 * @param username username
	 * @return result of the query
	 */
	private static Result matchFollowedVideogames (@NotNull Transaction tx,
	                                               @NotNull String username)
	{
		return tx.run("MATCH (:User {username: $username})-[:FOLLOWS]->(v:Videogame) " +
						"RETURN v as videogame",
				parameters("username", username));
	}

	private static Result neo4jSuggestedVideogames (@NotNull Transaction tx,
	                                                @NotNull String username,
	                                                int limit)
	{
		return tx.run("MATCH (target_user: User)-[tg:FOLLOWS]->(his_videogame:Videogame) " +
						"WHERE target_user.username = $username " +
						"WITH COLLECT(his_videogame.name) AS game_not_to_suggest " +
						"MATCH (his_videogame:Videogame)<-[ou:FOLLOWS]-(other_user:User)-[og:FOLLOWS]->" +
						"(other_videogame:Videogame) " +
						"WHERE (his_videogame.name IN game_not_to_suggest " +
						"AND NOT other_videogame.name IN game_not_to_suggest) " +
						"RETURN DISTINCT other_videogame AS VIDEOGAME, count(og) AS number_of_following " +
						"ORDER BY number_of_following DESC " +
						"LIMIT $limit",
				parameters("username", username, "limit", limit));
	}




	//////////////////////////////////////////////////////
	//////                METHODS                    /////
	//////////////////////////////////////////////////////
	/**
	 * Insert a videogame community into the database
	 *
	 * @param videogameCommunity object to insert
	 * @return id of the inserted videogame on success, null on failure
	 */
	@Override
	public ObjectId insertVideogameCommunity (@NotNull VideogameCommunity videogameCommunity) {

		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection();
		     Neo4jConnection neo4jConnection = PersistenceFactory.getNeo4jConnection()
		) {
			LOGGER.info("insertVideogameCommunity() | Insert videogame community");
			return insertVideogameCommunity(mongoConnection, neo4jConnection, videogameCommunity);

		} catch (Exception e) {
			LOGGER.error("insertVideogameCommunity() | " +
					"Unable to close MongoConnection or Neo4jConnection instance " +
					"due to error: " + e);
		}

		return null;
	}


	/**
	 * Insert a videogame community into the database
	 *
	 * @param mongoConnection connection to MongoDB
	 * @param neo4jConnection connection to neo4j
	 * @param videogameCommunity object to insert
	 * @return id of the inserted videogame on success, null on failure
	 */
	@Override
	public ObjectId insertVideogameCommunity (@NotNull MongoConnection mongoConnection,
	                                          @NotNull Neo4jConnection neo4jConnection,
	                                          @NotNull VideogameCommunity videogameCommunity)
	{
		// Check neo4j connectivity (reduce need of rollback)
		if (!neo4jConnection.verifyConnectivity()) {
			LOGGER.error("insertVideogameCommunity() | Neo4j connection is down");
			return null;
		}


		// Get videogame collection in MongoDB
		MongoCollection<Document> videogames = mongoConnection.getCollection(GameflowsCollection.videogame);
		ObjectId insertedVideogameId;

		// Insert in MongoDB
		try {
			InsertOneResult result = videogames.insertOne(videogameCommunity.toDocument());

			if (result.getInsertedId() == null) {
				LOGGER.error("insertVideogameCommunity() | Couldn't retrieve inserted ObjectId");
				return null;
			}

			insertedVideogameId = result.getInsertedId().asObjectId().getValue();
			LOGGER.info("insertVideogameCommunity() | " +
					"Inserted new videogame in MongoDB, with id " + insertedVideogameId.toString());

		} catch (MongoException ex) {
			LOGGER.error("insertVideogameCommunity() | Insertion of a videogame community failed: "
					+ ex.getMessage());
			return null;
		}

		// Insert in Neo4j
		try (Session session = neo4jConnection.getSession()) {
			ResultSummary resultSummary = session.writeTransaction(
					tx -> createVideogameNode(tx, insertedVideogameId, videogameCommunity.getName()).consume()
			);

			if (resultSummary.counters().nodesCreated() == 0) {
				LOGGER.error("insertVideogameCommunity() | Unable to create videogame node");
				rollbackMongoInsertVideogame(mongoConnection, insertedVideogameId);
				return null;
			}

			LOGGER.info("insertVideogameCommunity() | Videogame node created");

		} catch (Neo4jException ex) {
			LOGGER.error("insertVideogameCommunity() | Creation of Neo4j node failed: " + ex);
			rollbackMongoInsertVideogame(mongoConnection, insertedVideogameId);
			return null;
		}

		return insertedVideogameId;
	}


	private void rollbackMongoInsertVideogame (@NotNull MongoConnection connection, @NotNull ObjectId id) {
		MongoCollection<Document> videogames = connection.getCollection(GameflowsCollection.videogame);
		try {
			videogames.deleteOne(eq("_id", id));
			LOGGER.info("rollbackMongoInsertVideogame() | Videogame " + id + " has been removed");

		} catch (MongoException ex) {
			LOGGER.error("rollbackMongoInsertVideogame() | Cannot remove videogame from DB");
		}
	}



	/**
	 * Delete a videogame community from the databases
	 * @param videogameCommunityId id of the videogame community to delete
	 * @return true on success, false on failure
	 */
	@Override
	public Boolean deleteVideogameCommunity (ObjectId videogameCommunityId) {

		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection();
		     Neo4jConnection neo4jConnection = PersistenceFactory.getNeo4jConnection()
		) {
			LOGGER.info("deleteVideogameCommunity() | Delete videogame community");
			return deleteVideogameCommunity(mongoConnection, neo4jConnection, videogameCommunityId);

		} catch (Exception e) {
			LOGGER.error("deleteVideogameCommunity() | " +
					"Failed to handle MongoConnection instance: " + e);
		}

		return null;
	}


	/**
	 * Delete a videogame community from the databases
	 * @param mongoConnection an already opened MongoDB connection
	 * @param neo4jConnection an already opened Neo4j connection
	 * @param videogameCommunityId id of the videogame community to delete
	 * @return true on success, false on failure
	 */
	@Override
	public Boolean deleteVideogameCommunity (
			MongoConnection mongoConnection,
			Neo4jConnection neo4jConnection,
			ObjectId videogameCommunityId
	) {
		// Check arguments
		if (mongoConnection == null) {
			LOGGER.fatal("deleteVideogameCommunity() | MongoConnection parameter cannot be null");
			throw new IllegalArgumentException("MongoConnection cannot be null");
		}
		if (neo4jConnection == null) {
			LOGGER.fatal("deleteVideogameCommunity() | Neo4jConnection parameter cannot be null");
			throw new IllegalArgumentException("Neo4jConnection cannot be null");
		}

		// Check neo4j connectivity (reduce need of rollback)
		if (!neo4jConnection.verifyConnectivity()) {
			LOGGER.error("deleteVideogameCommunity() | Neo4j connection is down");
			return false;
		}

		// Get videogame collection in MongoDB
		MongoCollection<Document> videogames = mongoConnection.getCollection(GameflowsCollection.videogame);

		try {
			DeleteResult result = videogames.deleteOne(eq("_id", videogameCommunityId));
			if (result.getDeletedCount() == 0) {
				// Specified videogame doesn't exist in MongoDB.
				LOGGER.error("deleteVideogameCommunity() | " +
						"Trying to delete videogame community that doesn't exist");
				return false;
			}
			else {
				LOGGER.info("deleteVideogameCommunity() | " +
						"Videogame community " + videogameCommunityId + " deleted");
			}

			// Delete posts
			PostServiceFactory.getService()
					.deletePostByVideogameCommunity_MongoDB(mongoConnection, videogameCommunityId);
			// Delete comments
			CommentServiceFactory.getService()
					.deleteCommentsByVideogameCommunity_MongoDB(mongoConnection, videogameCommunityId);

		} catch (MongoException ex) {
			LOGGER.error("deleteVideogameCommunity() | " +
					"Unable to delete MongoDB videogame community due to an error: " + ex);
			return null;
		}

		// Delete from Neo4j
		try (Session session = neo4jConnection.getSession()) {
			ResultSummary resultSummary = session.writeTransaction(
					tx -> deleteVideogameNode(tx, videogameCommunityId).consume()
			);

			if (resultSummary.counters().nodesDeleted() == 0) {
				LOGGER.error("deleteVideogameCommunity() | Unable to delete Neo4j videogame community: " +
						"videogame doesn't exist");
				return false;
			}
			else {
				LOGGER.info("deleteVideogameCommunity() | Neo4j transaction was successful");
				return true;
			}

		} catch (Neo4jException ex) {
			LOGGER.error("deleteVideogameCommunity() | " +
					"Unable to delete Neo4j videogame community due to an error: " + ex);
			return null;
		}


	}



	/**
	 * Find a videogame community by id
	 * @param id id of the videogame community
	 * @return the requested videogame community on success, null otherwise
	 */
	@Override
	public VideogameCommunity find(ObjectId id) {
		try (MongoConnection connection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("find() | Find a videogame");
			return find(connection, id);

		} catch (Exception ex) {
			LOGGER.error("find() | Failed to handle MongoConnection instance: " + ex);
		}

		return null;
	}



	/**
	 * Find a videogame community by id
	 * @param connection an already opened MongoDB connection
	 * @param id id of the videogame community
	 * @return the requested videogame community on success, null otherwise
	 */
	@Override
	public VideogameCommunity find (MongoConnection connection, ObjectId id) {
		// Check arguments
		if (connection == null) {
			LOGGER.fatal("find() | MongoConnection parameter cannot be null");
			throw new IllegalArgumentException("MongoConnection cannot be null");
		}

		MongoCollection<Document> videogames = connection.getCollection(GameflowsCollection.videogame);
		VideogameCommunity result = null;

		// Query the database and convert the result to a list of VideogameCommunity objects
		try (MongoCursor<Document> cursor =
				     videogames.find(eq("_id", id)).iterator()
		) {
			if (cursor.hasNext()) {
				result = VideogameCommunity.fromDocument(cursor.next());
			}
		}

		return result;
	}




	/**
	 * Read from the database the data of all the videogame communities
	 * @param skip how many videogame communities to skip
	 * @param limit how many videogame communities must this method return (at most)
	 * @return a list of videogame communities
	 */
	@Override
	public List<VideogameCommunity> browse(int skip, int limit) {
		try (MongoConnection connection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("view() | View all videogames");
			return browse(connection, skip, limit);

		} catch (Exception ex) {
			LOGGER.error("view() | Failed to handle MongoConnection instance: " + ex);
		}

		return null;
	}




	/**
	 * Read from the database the data of all the videogame communities
	 * @param connection an already opened MongoDB connection
	 * @param skip how many videogame communities to skip
	 * @param limit how many videogame communities must this method return (at most)
	 * @return a list of videogame communities
	 */
	@Override
	public List<VideogameCommunity> browse(MongoConnection connection, int skip, int limit) {
		// Check arguments
		if (connection == null) {
			LOGGER.fatal("view() | MongoConnection parameter cannot be null");
			throw new IllegalArgumentException("MongoConnection cannot be null");
		}

		MongoCollection<Document> videogames = connection.getCollection(GameflowsCollection.videogame);
		List<VideogameCommunity> list = new ArrayList<>();

		// Query the database and convert the result to a list of VideogameCommunity objects
		try (MongoCursor<Document> cursor =
				     videogames.find()
						     .sort(ascending("_id"))
						     .skip(skip)
						     .limit(limit)
						     .iterator()
		) {
			while (cursor.hasNext()) {
				list.add(VideogameCommunity.fromDocument(cursor.next()));
			}
		}

		return list;
	}




	/**
	 * Search a videogame community by name.
	 * The string to match is case-insensitive and doesn't have to match the entire word
	 * @param match name of the videogame (regex)
	 * @param skip how many result to skip
	 * @param limit how many result to return
	 * @return list of videogame community which match the regex filter
	 */
	@Override
	public List<VideogameCommunity> search (String match, int skip, int limit) {

		try (MongoConnection connection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("search() | Search videogame community by regex name");
			return search(connection, match, skip, limit);

		} catch (Exception ex) {
			LOGGER.error("search() | Failed to handle MongoConnection instance: " + ex);
		}

		return null;
	}



	/**
	 * Search a videogame community by name.
	 * The string to match is case-insensitive and doesn't have to match the entire word
	 * @param connection opened connection to MongoDB
	 * @param match name of the videogame (regex)
	 * @param skip how many result to skip
	 * @param limit how many result to return
	 * @return list of videogame community which match the regex filter
	 */
	@Override
	public List<VideogameCommunity> search (MongoConnection connection,
	                                        String match, int skip, int limit)
	{
		if (connection == null) {
			LOGGER.fatal("search() | MongoConnection parameter cannot be null");
			throw new IllegalArgumentException("MongoConnection cannot be null");
		}

		List<VideogameCommunity> list = new ArrayList<>();

		// Get videogame collection and prepare filter
		MongoCollection<Document> videogames = connection.getCollection(GameflowsCollection.videogame);
		Bson filter = regex("name", match, "i");

		// Query the database and convert the result to a list of VideogameCommunity objects
		try (MongoCursor<Document> cursor =
					     videogames.find(filter)
							     .skip(skip)
							     .limit(limit)
							     .iterator()
		) {
			while (cursor.hasNext()) {
				try {
					list.add(VideogameCommunity.fromDocument(cursor.next()));
				} catch (ClassCastException ex) {
					LOGGER.error("search() | " +
							"Unable to create VideogameCommunity object due to errors: " + ex);
				}
			}
		}

		return list;
	}




	/**
	 * Let a user follow an existing videogame community
	 * @param videogameCommunity videogame community to follow
	 * @param user user who wants to follow the community
	 * @return true on success, false otherwise
	 */
	boolean followVideogameCommunity (VideogameCommunity videogameCommunity, User user, boolean follow) {

		try (Neo4jConnection connection = PersistenceFactory.getNeo4jConnection()) {
			if (follow) {
				LOGGER.info("followVideogameCommunity() | " +
						"Follow videogame " + videogameCommunity.getName());
			}
			else {
				LOGGER.info("followVideogameCommunity() | " +
						"Unfollow videogame " + videogameCommunity.getName());
			}
			return followVideogameCommunity(connection, videogameCommunity, user, follow);

		} catch (Exception ex) {
			LOGGER.error("followVideogameCommunity() | " +
					"Failed to handle Neo4jConnection instance: " + ex);
		}

		return false;
	}




	/**
	 * Let a user follow an existing videogame community
	 * @param connection an already opened MongoDB connection
	 * @param videogameCommunity videogame community to follow
	 * @param user user who wants to follow the community
	 * @return true on success, false otherwise
	 */
	boolean followVideogameCommunity (Neo4jConnection connection,
	                                         VideogameCommunity videogameCommunity,
	                                         User user,
	                                         boolean follow)
	{
		if (connection == null) {
			LOGGER.fatal("followVideogameCommunity() | Neo4jConnection parameter cannot be null");
			throw new IllegalArgumentException("Neo4jConnection cannot be null");
		}

		try (Session session = connection.getSession()) {
			ResultSummary resultSummary = session.writeTransaction(
				tx -> {
					Result result;
					if (follow) {
						result = createFollowsRelationship(tx, videogameCommunity.getId(), user.getUsername());
					}
					else {
						result = deleteFollowsRelationship(tx, videogameCommunity.getId(), user.getUsername());
					}

					return result.consume();
				}
			);

			if ((follow && resultSummary.counters().relationshipsCreated() == 0)
				|| (!follow && resultSummary.counters().relationshipsDeleted() == 0)) {
				LOGGER.error("followVideogameCommunity() | " +
						"Unable to follow/unfollow specified videogame " + videogameCommunity.getName() +
						" by user " + user.getUsername());
				return false;
			}

			LOGGER.info("followVideogameCommunity() | Neo4j write transaction was successful");
			return true;

		} catch (Neo4jException ex) {
			LOGGER.error("followVideogameCommunity() | " +
					"Neo4j transaction failed due to errors: " + ex);
		}

		return false;
	}


	/**
	 * View the list of followed videogame communities by a specified user
	 *
	 * @param user  username
	 * @return list of VideogameCommunity (only id and name fields are set) on success, null otherwise
	 */
	FollowedVideogames viewFollowedVideogames (User user) {
		try (Neo4jConnection neo4jConnection = PersistenceFactory.getNeo4jConnection()) {
			LOGGER.info("viewFollowedVideogamesCommunities() | View followed videogames communities");
			return viewFollowedVideogames(neo4jConnection, user);

		} catch (Exception e) {
			LOGGER.error("viewFollowedVideogamesCommunities() | " +
					"Failed to handle Neo4jConnection instance: " + e);
		}

		return null;
	}


	/**
	 * View the list of followed videogame communities by a specified user
	 *
	 * @param connection an already opened neo4j connection
	 * @param user       username
	 * @return list of VideogameCommunity (only id and name fields are set) on success, null otherwise
	 */
	FollowedVideogames viewFollowedVideogames (Neo4jConnection connection, User user)
	{
		// Check arguments
		if (connection == null) {
			LOGGER.fatal("viewFollowedVideogames() | Neo4jConnection parameter cannot be null");
			throw new IllegalArgumentException("Neo4jConnection cannot be null");
		}

		// Open neo4j session
		try (Session session = connection.getSession()) {
			FollowedVideogames followedVideogames = new FollowedVideogames();

			// Execute transaction
			session.readTransaction(
				tx -> {
					Result result = matchFollowedVideogames(tx, user.getUsername());

					// Iterate the obtained data and build a list of videogames to return
					while (result.hasNext()) {
						Record record = result.next();
						followedVideogames.add(
										new ObjectId(record.get("videogame").get("videogame_id").asString()),
										record.get("videogame").get("name").asString()
						);
					}
					return 1;
				}
			);

			LOGGER.info("viewFollowedVideogames() | " +
					"Returned " + followedVideogames.getList().size() + " videogame communities");

			return followedVideogames;

		} catch (Neo4jException ex) {
			LOGGER.error("viewFollowedVideogames() | " +
					"Neo4j transaction failed due to errors: " + ex);
			return null;
		}

	}



	/**
	 * View a list of suggested videogame communities based on common interests
	 * with the other users.
	 * @param user user who want to see the limit
	 * @param limit how many videogames to return
	 * @return list of videogame (only id and name are set) on success, null otherwise
	 */
	@Override
	public List<VideogameCommunity> viewSuggestedVideogameCommunities(User user, int limit) {
		try (Neo4jConnection neo4jConnection = PersistenceFactory.getNeo4jConnection()) {
			LOGGER.info("viewSuggestedVideogameCommunities() | View suggested videogame communities");
			return viewSuggestedVideogameCommunities(neo4jConnection, user, limit);

		} catch (Exception e) {
			LOGGER.error("viewSuggestedVideogameCommunities() | " +
					"Failed to handle Neo4jConnection instance: " + e);
		}

		return null;
	}

	/**
	 * View a list of suggested videogame communities based on common interests
	 * with the other users.
	 * @param connection an already opened neo4j connection
	 * @param user user who want to see the limit
	 * @param limit how many videogames to return
	 * @return list of videogame (only id and name are set) on success, null otherwise
	 */
	@Override
	public List<VideogameCommunity> viewSuggestedVideogameCommunities (Neo4jConnection connection,
	                                                                   User user, int limit)
	{
		// Check arguments
		if (connection == null) {
			LOGGER.fatal("viewSuggestedVideogameCommunities() | Neo4jConnection parameter cannot be null");
			throw new IllegalArgumentException("Neo4jConnection cannot be null");
		}

		ArrayList<VideogameCommunity> list = new ArrayList<>();

		// Get a new neo4j session
		try (Session session = connection.getSession()) {
			session.readTransaction(
					tx -> {
						// Read from neo4j db
						Result result = neo4jSuggestedVideogames (tx, user.getUsername(), limit);

						// Iterate the obtained data and build a list of videogames to return
						while (result.hasNext()) {
							Record record = result.next();
							list.add(
								new VideogameCommunity(
									new ObjectId(record.get("VIDEOGAME").get("videogame_id").asString()),
									record.get("VIDEOGAME").get("name").asString()
								)
							);
						}

						return 1;
					}
			);

			LOGGER.info("viewSuggestedVideogameCommunities() | " +
					"Returned " + list.size() + " videogame communities");

			return list;

		} catch (Neo4jException ex) {
			LOGGER.error("viewSuggestedVideogameCommunities() | " +
					"Neo4j transaction failed due to errors: " + ex);
			return null;
		}
	}

}
