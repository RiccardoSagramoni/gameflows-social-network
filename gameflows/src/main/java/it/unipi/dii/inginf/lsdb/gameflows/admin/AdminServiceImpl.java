package it.unipi.dii.inginf.lsdb.gameflows.admin;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.inginf.lsdb.gameflows.persistence.*;
import it.unipi.dii.inginf.lsdb.gameflows.util.Password;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.exceptions.Neo4jException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Updates.*;
import static org.neo4j.driver.Values.parameters;

class AdminServiceImpl implements AdminService {
	// Logger
	private static final Logger LOGGER = LogManager.getLogger(AdminServiceImpl.class);


	/**
	 * Execute the query that computes the new influencers by analyzing
	 * the :LIKE and :REPLY relationship to their posts in a given period of time.
	 * @param tx neo4j transaction object
	 * @param fromDate beginning of the time period for the evaluation
	 * @param toDate ending of the time period for the evaluation
	 * @param limit how many user to return
	 * @return the result of the query on success, null on error
	 */
	private static Result neo4jInfluencerQuery (@NotNull Transaction tx,
	                                            @NotNull Date fromDate,
	                                            @NotNull Date toDate,
	                                            int limit)
	{
		LocalDate localFromDate = fromDate.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();

		LocalDate localToDate = toDate.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDate();

		return tx.run("MATCH (influencer: User)-[write:WRITES]->(posts:Post)\n" +
				"MATCH (user_who_liked: User)-[liked:LIKES]->(posts:Post)\n" +
				"WHERE date(liked.timestamp) >= $from AND date(liked.timestamp) <= $to\n" +
				"OPTIONAL MATCH (all_comments: Comment)-[commented:REPLY]->(posts:Post)\n" +
				"WHERE date(commented.timestamp) >= $from " +
				"AND date(commented.timestamp) <= $to\n" +
				"RETURN DISTINCT \n" +
				"        influencer.username AS INFLUENCER, \n" +
				"        (count(DISTINCT user_who_liked) + count(DISTINCT all_comments) ) AS GRADE\n" +
				"ORDER BY GRADE DESC\n" +
				"LIMIT $limit",
				parameters("limit", limit, "from", localFromDate, "to", localToDate));
	}



	/**
	 * Check if typed username and password are correct, i.e. exists an already
	 * registered user with the specified username and password.
	 * @param username username
	 * @param password password
	 * @return true on success, false otherwise
	 */
	@Override
	public boolean login (@NotNull String username, @NotNull String password) {
		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("login() | Login admin " + username);
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
	public boolean login (@NotNull MongoConnection connection,
	                      @NotNull String username,
	                      @NotNull String password)
	{
		// Get admin mongo collection
		MongoCollection<Document> admins = connection.getCollection(GameflowsCollection.admin);
		Bson filter = eq("username", username);
		Bson project = fields(include("password"), excludeId());
		Password dbPassword = null;

		// Get admin's credentials from database
		try (MongoCursor<Document> cursor =
				     admins.find(filter).projection(project).iterator()) {
			if(cursor.hasNext()) {
				dbPassword = Password.fromDocument(cursor.next());
			}
		} catch (MongoException ex) {
			LOGGER.error("login() | " +
					"Unable to read from MongoDB due to error: " + ex);
		}

		if(dbPassword == null) {
			LOGGER.error("login() | There is no admin called " + username);
			return false;
		}

		// Check if the password is the right one
		return dbPassword.checkPassword(password);
	}


	/**
	 * Create an admin account in the databases
	 * @param admin admin to insert
	 * @return true on success, false on failure
	 */
	@Override
	public boolean createAdminAccount (@NotNull Admin admin) {
		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("createAdminAccount() | Create admin account for " + admin.getUsername());
			return createAdminAccount(mongoConnection, admin);

		} catch (Exception e) {
			LOGGER.error("createAdminAccount() | " +
					"Unable to handle MongoConnection due to error: " + e);
		}

		return false;
	}

	/**
	 * Create an admin account in the databases
	 * @param connection an already opened connection to MongoDB
	 * @param admin admin to insert
	 * @return true on success, false on failure
	 */
	@Override
	public boolean createAdminAccount (@NotNull MongoConnection connection, @NotNull Admin admin) {
		// Get admin collection
		MongoCollection<Document> admins = connection.getCollection(GameflowsCollection.admin);

		try {
			InsertOneResult result = admins.insertOne(admin.toDocument());

			if (result.getInsertedId() == null) {
				LOGGER.error("createAdminAccount() | Inserted id is null");
				return false;
			}
			return true;

		} catch (MongoException ex) {
			LOGGER.error("createAdminAccount() | Unable to insert new admin data in MongoDB: " + ex);
			return false;
		}
	}




	/**
	 * Compute a list of the new influencer of the social network, by
	 * evaluating how many likes and comments they received in the specified
	 * period of time.
	 *
	 * @param fromDate start of the period of time
	 * @param toDate   end of the period of time
	 * @param limit    how many influencers to return
	 * @return a list of pairs with the username of the new influencers
	 * and the grade on success, null on error
	 */
	@Override
	public List<Pair<String, Integer>> viewInfluencerRanking (@NotNull Date fromDate,
	                                                          @NotNull Date toDate, int limit)
	{
		try (Neo4jConnection connection = PersistenceFactory.getNeo4jConnection()) {
			LOGGER.info("viewInfluencerRanking() | View influencers: " + limit);
			return viewInfluencerRanking(connection, fromDate, toDate, limit);

		} catch (Exception e) {
			LOGGER.error("viewInfluencerRanking() | " +
					"Unable to handle Neo4jConnection due to error: " + e);
			return null;
		}
	}

	/**
	 * Compute a list of the new influencer of the social network, by
	 * evaluating how many likes and comments (grade) they received in the specified
	 * period of time.
	 *
	 * @param connection an already opened neo4j connection
	 * @param fromDate   start of the period of time
	 * @param toDate     end of the period of time
	 * @param limit      how many influencers to return
	 * @return a list of pairs with the username of the new influencers
	 * and the grade on success, null on error
	 */
	@Override
	public List<Pair<String, Integer>> viewInfluencerRanking(@NotNull Neo4jConnection connection,
	                                                         @NotNull Date fromDate,
	                                                         @NotNull Date toDate, int limit)
	{
		// Open a neo4j session
		try (Session session = connection.getSession()) {
			List<Pair<String, Integer>> list = new ArrayList<>();

			// Execute the read transaction
			session.readTransaction(
					tx -> {
						Result result = neo4jInfluencerQuery(tx, fromDate, toDate, limit);

						// Parse the results
						while (result.hasNext()) {
							Record record = result.next();
							list.add(
									new Pair<>(
											record.get("INFLUENCER").asString(),
											record.get("GRADE").asInt()
									)
							);
						}
						return 1;
					}
			);

			LOGGER.info("viewInfluencerRanking() | Successfully computed new list of " +
					list.size() + " influencers");

			return list;

		} catch (Neo4jException ex) {
			LOGGER.error("viewInfluencerRanking() | Neo4j query failed: " + ex);
			return null;
		}
	}





	/**
	 * Update the users which holds the influencer tag
	 * @param influencers list of the new influencers' username (retrieved by calling viewInfluencerRanking)
	 * @return true on success, false otherwise
	 */
	@Override
	public boolean updateInfluencers(@NotNull List<String> influencers)
	{
		try (MongoConnection connection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("updateInfluencers() | New influencers: " + influencers.size());
			return updateInfluencers(connection, influencers);

		} catch (Exception e) {
			LOGGER.error("updateInfluencers() | " +
					"Unable to handle MongoConnection due to error: " + e);
			return false;
		}
	}

	/**
	 * Update the users which holds the influencer tag
	 * @param connection an already opened connection to MongoDB
	 * @param influencers list of the new influencers' username (retrieved by calling viewInfluencerRanking)
	 * @return true on success, false otherwise
	 */
	@Override
	public boolean updateInfluencers(@NotNull MongoConnection connection,
	                                 @NotNull List<String> influencers)
	{
		// Get user collection
		MongoCollection<Document> users = connection.getCollection(GameflowsCollection.user);

		try {
			// Remove all the currently assigned influencer tags
			UpdateResult result = users.updateMany(
					eq("isInfluencer", true),
					set("isInfluencer", false)
			);

			LOGGER.info("updateInfluencers() | " +
					"Removal of influencer flags: modified " +
					result.getModifiedCount() + " documents");

			// Set the influencer tag
			result = users.updateMany(
					in("username", influencers),
					set("isInfluencer", true)
			);

			LOGGER.info("updateInfluencers() | " +
					"Update of the influencers: modified " +
					result.getModifiedCount() + " documents");

			return true;

		} catch (MongoException ex) {
			LOGGER.error("updateInfluencers() | " +
					"Unable to update influencers: " + ex);
			return false;
		}
	}




	/**
	 * Block/unblock a user. A blocked user cannot use the social network anymore
	 * @param username username of the user to block
	 * @param block true if the admin wants to block the user, false if they want to unblock the user
	 * @return true on success, false otherwise
	 */
	@Override
	public boolean blockUser (@NotNull String username, boolean block) {
		// Open MongoDB connection
		try (MongoConnection connection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("blockUser() | user: " + username + "; block: " + block);
			return blockUser(connection, username, block);

		} catch (Exception e) {
			LOGGER.error("blockUser() | " +
					"Unable to handle MongoConnection due to error: " + e);
			return false;
		}
	}

	/**
	 * Block/unblock a user. A blocked user cannot use the social network anymore
	 * @param connection an already opened connection to MongoDB
	 * @param username username of the user to block
	 * @param block true if the admin wants to block the user, false if they want to unblock the user
	 * @return true on success, false otherwise
	 */
	@Override
	public boolean blockUser (@NotNull MongoConnection connection, @NotNull String username, boolean block) {
		MongoCollection<Document> users = connection.getCollection(GameflowsCollection.user);

		try {
			// Update flab isBlocked of given user
			UpdateResult result = users.updateOne(
					eq("username", username),
					set("isBlocked", block)
			);

			// Check results of query
			if (result.getModifiedCount() == 0) {
				LOGGER.error("blockUser() | Unable to " + (block ? "block" : "unblock") +
						" user " + username + ". Maybe user doesn't exist");
				return false;
			}

			LOGGER.info("blockUser() | Successfully " + (block ? "blocked" : "unblocked") +
					" user " + username);
			return true;

		} catch (MongoException ex) {
			LOGGER.error("blockUser() | Unable to " + (block ? "block" : "unblock") +
					" user " + username + " due to errors: " + ex);
			return false;
		}
	}

	/**
	 * Find an admin, given their username
	 * @param username username to find
	 * @return an Admin object on success, null on error
	 */
	//@Override
	public Admin find (@NotNull String username) {
		try (MongoConnection mongoConnection = PersistenceFactory.getMongoConnection()) {
			LOGGER.info("find() | Find admin " + username);
			return find(mongoConnection, username);

		} catch (Exception e) {
			LOGGER.error("find() | " +
					"Unable to handle MongoConnection due to error: " + e);
		}
		return null;
	}


	/**
	 * Find an Admin, given their username
	 * @param connection an already opened MongoDB connection
	 * @param username username to find
	 * @return an Admin object on success, null on error
	 */
	//@Override
	public Admin find(@NotNull MongoConnection connection, @NotNull String username) {
		// Get access to user collection
		MongoCollection<Document> admins = connection.getCollection(GameflowsCollection.admin);

		// Find user from username
		try (MongoCursor<Document> cursor =
					 admins.find(eq("username", username))
							 .cursor()
		){
			// Check if the query returned a document
			if (cursor.hasNext()) {
				return Admin.fromDocument(cursor.next());
			}
			else {
				LOGGER.error("find() | Admin " + username + " doesn't exist");
				return null;
			}
		} catch (MongoException ex) {
			LOGGER.error("find() | " +
					"Unable to read admin data from MongoDB due to errors: " + ex);
			return null;
		}
	}
}
