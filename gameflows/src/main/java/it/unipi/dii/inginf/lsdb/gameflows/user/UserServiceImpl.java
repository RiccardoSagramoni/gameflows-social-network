package it.unipi.dii.inginf.lsdb.gameflows.user;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.InsertOneResult;
import it.unipi.dii.inginf.lsdb.gameflows.persistence.*;
import it.unipi.dii.inginf.lsdb.gameflows.util.Password;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.summary.ResultSummary;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.orderBy;
import static org.neo4j.driver.Values.parameters;

class UserServiceImpl implements UserService {
	// Logger
	private static final Logger LOGGER = LogManager.getLogger(UserServiceImpl.class);

	/**
	 * Create a User node in neo4j database
	 * @param tx Neo4j Transaction object
	 * @param username username
	 * @return result of the transaction
	 */
	private static Result createUserNode(@NotNull Transaction tx, @NotNull String username) {
		return tx.run("MERGE (u: User {username: $username})",
				parameters("username", username));
	}


	/**
	 * Check if typed username and password are correct, i.e. exists an already
	 * registered user with the specified username and password.
	 * @param username username
	 * @param password password
	 * @return true on success, false otherwise
	 */
	@Override
	public boolean login (String username, String password) {
		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("login() | Login user " + username);
			return login(mongoConnection, username, password);

		} catch (Exception e) {
			LOGGER.error("login() | " +
					"Unable to handle MongoConnection due to error: " + e);
		}

		return false;
	}


	/**
	 * Check if typed username and password are correct, i.e. exists an already
	 * registered user with the specified username and password.
	 * @param connection an already opened MongoDB connection
	 * @param username username
	 * @param password password
	 * @return true on success, false otherwise
	 */
	@Override
	public boolean login (MongoConnection connection, String username, String password) {
		if(connection == null) {
			LOGGER.fatal("login() | MongoConnection parameter cannot be null");
			throw new IllegalArgumentException("MongoConnection cannot be null");
		}

		// Get user mongo collection
		MongoCollection<Document> users = connection.getCollection(GameflowsCollection.user);
		Bson filter = and(
				eq("username", username),
				or(
						not(exists("isBlocked")),
						eq("isBlocked", false)
				)
		);
		Bson project = fields(include("password"), excludeId());
		Password dbPassword = null;

		// Get user's credentials from database
		try (MongoCursor<Document> cursor =
				     users.find(filter).projection(project).iterator()) {
			if(cursor.hasNext()) {
				dbPassword = Password.fromDocument(cursor.next());
			}
		} catch (MongoException ex) {
			LOGGER.error("login() | " +
					"Unable to read from MongoDB due to error: " + ex);
		}

		if(dbPassword == null) {
			LOGGER.error("login() | There is no user called " + username);
			return false;
		}

		// Check if the password is the right one
		return dbPassword.checkPassword(password);
	}


	/**
	 * Check if a user is blocked. A blocked user cannot access in any way to the social network
	 * @param username user to block
	 * @return true if user is blocked, false if user is NOT blocked, null on error
	 */
	@Override
	public Boolean isUserBlocked (String username) {
		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("isUserBlocked() | Check if user " + username + " is blocked");
			return isUserBlocked(mongoConnection, username);

		} catch (Exception e) {
			LOGGER.error("isUserBlocked() | " +
					"Unable to handle MongoConnection due to error: " + e);
		}

		return false;
	}


	/**
	 * Check if a user is blocked. A blocked user cannot access in any way to the social network
	 * @param connection an already opened connection to MongoDB
	 * @param username user to block
	 * @return true if user is blocked, false if user is NOT blocked, null on error
	 */
	@Override
	public Boolean isUserBlocked (MongoConnection connection, String username) {
		if(connection == null) {
			LOGGER.fatal("isUserBlocked() | MongoConnection parameter cannot be null");
			throw new IllegalArgumentException("MongoConnection cannot be null");
		}

		// Get user mongo collection
		MongoCollection<Document> users = connection.getCollection(GameflowsCollection.user);
		Bson filter = and(
				eq("username", username),
				eq("isBlocked", true)
		);
		Bson project = fields(include("_id"));

		// Look if user is blocked
		try (MongoCursor<Document> cursor =
				     users.find(filter).projection(project).iterator()) {
			return cursor.hasNext();

		} catch (MongoException ex) {
			LOGGER.error("isUserBlocked() | " +
					"Unable to read from MongoDB due to error: " + ex);
			return null;
		}
	}


	/**
	 * Register a user in a system, by adding its data in the databases
	 * @param user user to insert
	 * @return true on success, false otherwise
	 */
	@Override
	public ObjectId insertUser(User user) {
		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection();
		     Neo4jConnection neo4jConnection = PersistenceFactory.getNeo4jConnection())
		{
			LOGGER.info("insertUser() | Insert user " + user.getUsername());
			return insertUser(mongoConnection, neo4jConnection, user);

		} catch (Exception e) {
			LOGGER.error("insertUser() | " +
					"Unable to handle MongoConnection due to error: " + e);
		}

		return null;
	}


	/**
	 * Register a user in a system, by adding its data in the databases
	 * @param mongoConnection an already opened MongoDB connection
	 * @param neo4jConnection an already opened Neo4j connection
	 * @param user user to insert
	 * @return true on success, false otherwise
	 */
	@Override
	public ObjectId insertUser(MongoConnection mongoConnection,
	                           Neo4jConnection neo4jConnection,
	                           User user)
	{
		// Check arguments
		if(mongoConnection == null) {
			LOGGER.fatal("insertUser() | MongoConnection parameter cannot be null");
			throw new IllegalArgumentException("MongoConnection cannot be null");
		}
		if(neo4jConnection == null) {
			LOGGER.fatal("insertUser() | Neo4jConnection parameter cannot be null");
			throw new IllegalArgumentException("Neo4jConnection cannot be null");
		}

		// Check neo4j connectivity (reduce need of rollback)
		if (!neo4jConnection.verifyConnectivity()) {
			LOGGER.error("insertUser() | Neo4j connection is down");
			return null;
		}

		// Get MongoDB collection
		MongoCollection<Document> users = mongoConnection.getCollection(GameflowsCollection.user);
		ObjectId insertedUserId;

		// Insert in MongoDB
		try {
			InsertOneResult result = users.insertOne(user.toDocument());

			// Check if query was successful
			if(result.getInsertedId() == null) {
				LOGGER.error("insertUser() | Couldn't retrieve inserted ObjectId");
				return null;
			}

			// Extract the id of the inserted user
			insertedUserId = result.getInsertedId().asObjectId().getValue();
			LOGGER.info("insertUser() | " +
					"Inserted new user in MongoDB, with id " + insertedUserId.toString());

		} catch (MongoException ex) {
			LOGGER.error("insertUser() | Insertion of a user failed: "
					+ ex.getMessage());
			return null;
		}

		// Insert in Neo4j
		try (Session session = neo4jConnection.getSession()) {
			ResultSummary resultSummary = session.writeTransaction(
					tx -> createUserNode(tx, user.getUsername()).consume()
			);

			if (resultSummary.counters().nodesCreated() == 0) {
				LOGGER.error("insertUser() | Creation of Neo4j node failed");
				LOGGER.info("insertUser() | Rollback insert user in MongoDB");
				rollbackMongoInsertUser(mongoConnection, insertedUserId);
				return null;
			}

			LOGGER.info("insertUser() | User node created");
			return insertedUserId;

		} catch (Neo4jException ex) {
			LOGGER.error("insertUser() | Creation of Neo4j node failed: " + ex);
			LOGGER.info("insertUser() | Rollback insert user in MongoDB");
			rollbackMongoInsertUser(mongoConnection, insertedUserId);
			return null;
		}
	}


	/**
	 * Rollback an insert query of a user. It deletes the specified MongoDB object from the user collection
	 * @param connection an already opened MongoDB connection
	 * @param id id of the object to remove
	 */
	private void rollbackMongoInsertUser (@NotNull MongoConnection connection, @NotNull ObjectId id) {
		MongoCollection<Document> users = connection.getCollection(GameflowsCollection.user);
		try {
			users.deleteOne(eq("_id", id));
			LOGGER.info("rollbackMongoInsertUser() | User " + id + " has been removed");

		} catch (MongoException ex) {
			LOGGER.error("rollbackMongoInsertUser() | Cannot remove user from DB");
		}
	}


	/**
	 * Browse the users of the videogame social network, sorted by username
	 * @param skip how many document to skip
	 * @param limit how many document to fetch from the database
	 * @return a list of users on success, null on error
	 */
	@Override
	public List<User> browse (int skip, int limit) {
		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("browseUsers()");
			return browse(mongoConnection, skip, limit);

		} catch (Exception e) {
			LOGGER.error("browseUsers() | " +
					"Unable to handle MongoConnection due to error: " + e);
		}
		return null;
	}

	/**
	 * Browse the users of the videogame social network, sorted by username
	 * @param connection an already opened connection to MongoDB
	 * @param skip how many document to skip
	 * @param limit how many document to fetch from the database
	 * @return a list of users on success, null on error
	 */
	@Override
	public List<User> browse (@NotNull MongoConnection connection, int skip, int limit) {
		// Get access to user collection
		MongoCollection<Document> users = connection.getCollection(GameflowsCollection.user);
		List<User> list = new ArrayList<>();

		// Read the users' data from database
		try (MongoCursor<Document> cursor =
				     users.find()
						     .projection(exclude("password"))
						     .sort(orderBy(ascending("username")))
						     .skip(skip)
						     .limit(limit)
						     .cursor()
		) {
			// Fetch the BSON documents to User object
			while (cursor.hasNext()) {
				list.add(User.fromDocument(cursor.next()));
			}
		} catch (MongoException ex) {
			LOGGER.error("browseUsers() | " +
					"Unable to read user's data from MongoDB due to errors: " + ex);
			return null;
		}

		return list;
	}


	/**
	 * Find a user, given their username
	 * @param username username to find
	 * @return a User object on success, null on error
	 */
	@Override
	public User find (@NotNull String username) {
		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("find() | Find user " + username);
			return find(mongoConnection, username);

		} catch (Exception e) {
			LOGGER.error("find() | " +
					"Unable to handle MongoConnection due to error: " + e);
		}
		return null;
	}


	/**
	 * Find a user, given their username
	 * @param connection an already opened MongoDB connection
	 * @param username username to find
	 * @return a User object on success, null on error
	 */
	@Override
	public User find (@NotNull MongoConnection connection, @NotNull String username) {
		// Get access to user collection
		MongoCollection<Document> users = connection.getCollection(GameflowsCollection.user);

		// Find user from username
		try (MongoCursor<Document> cursor =
				     users.find(eq("username", username))
						     .cursor()
		){
			// Check if the query returned a document
			if (cursor.hasNext()) {
				return User.fromDocument(cursor.next());
			}
			else {
				LOGGER.error("find() | User " + username + " doesn't exist");
				return null;
			}
		} catch (MongoException ex) {
			LOGGER.error("find() | " +
					"Unable to read user data from MongoDB due to errors: " + ex);
			return null;
		}
	}

	/**
	 * Search a user by username (regex).
	 * The string to match is case-insensitive and doesn't have to match the entire word.
	 *
	 * @param match name of the user to match
	 * @param skip how many result to skip
	 * @param limit how many result to return
	 * @return list of the users that match the regex filter
	 */
	@Override
	public List<User> search (@NotNull String match, int skip, int limit) {

		try (MongoConnection connection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("search() | Search User by regex name");
			return search(connection, match, skip, limit);

		} catch (Exception ex) {
			LOGGER.error("search() | Failed to handle MongoConnection instance: " + ex);
		}

		return null;
	}


	
	/**
	 * Search a user by username (regex).
	 * The string to match is case-insensitive and doesn't have to match the entire word.
	 *
	 * @param connection opened connection to MongoDB
	 * @param match name of the user (regex)
	 * @param skip how many result to skip
	 * @param limit how many result to return
	 * @return list of users which match the regex filter
	 */
	@Override
	public List<User> search (@NotNull MongoConnection connection, @NotNull String match,
	                          int skip, int limit)
	{
		List<User> list = new ArrayList<>();

		// Get videogame collection and prepare filter
		MongoCollection<Document> users = connection.getCollection(GameflowsCollection.user);
		Bson filter = regex("username", match, "i");

		// Query the database and convert the result to a list of VideogameCommunity objects
		try (MongoCursor<Document> cursor =
				     users.find(filter)
						     .projection(exclude("password"))
						     .sort(orderBy(ascending("username")))
						     .skip(skip)
						     .limit(limit)
						     .iterator())
		{
			while (cursor.hasNext()) {
				try {
					list.add(User.fromDocument(cursor.next()));
				}
				catch (ClassCastException ex) {
					LOGGER.error("search() | " +
							"Unable to create user object due to errors: " + ex);
				}
			}
		}

		return list;
	}

}