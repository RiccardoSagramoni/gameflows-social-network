package it.unipi.dii.inginf.lsdb.gameflows.persistence;

import com.mongodb.ConnectionString;
import com.mongodb.MongoException;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;

class MongoConnectionImpl implements MongoConnection {
	private static final Logger LOGGER = LogManager.getLogger(MongoConnectionImpl.class);

	private final MongoClient client;
	private final MongoDatabase database;

	MongoConnectionImpl() {
		this(PersistenceFactory.getDatabaseConfiguration());
	}

	MongoConnectionImpl (DatabaseConfiguration conf) {
		if (conf == null) {
			LOGGER.fatal("database configuration is null");
			throw new IllegalArgumentException("DatabaseConfiguration parameter cannot be null");
		}

		String databaseName = conf.getMongoDatabaseName();
		ConnectionString uri = new ConnectionString(conf.getMongoUri());

		try {
			this.client = MongoClients.create(uri);
			this.database = client.getDatabase(databaseName);

		} catch (MongoException ex) {
			LOGGER.fatal(
					"Failed to open a connection with MongoDB server " +
					uri.getConnectionString() + " on database " + databaseName
			);
			throw ex;
		}

		LOGGER.info("MongoDB connection created with uri " + uri.getConnectionString());
	}

	/**
	 * Get a connection to a collection of MongoDB,
	 * with write concern "W1" and read preference "nearest"
	 * @param collection collection to connect with
	 * @return connection to collection (null on error)
	 */
	@Override
	public MongoCollection<Document> getCollection(GameflowsCollection collection) {
		return getCollection(collection, WriteConcern.W1, ReadPreference.nearest());
	}

	/**
	 * Get a connection to a collection of MongoDB,
	 * with write concern "W1" and read preference "nearest".
	 * @param collection collection to connect with
	 * @return connection to collection
	 * @throws MongoException on error
	 */
	@Override
	public MongoCollection<Document> getCollection(GameflowsCollection collection,
	                                               WriteConcern writeConcern,
	                                               ReadPreference readPreference) {
		if (collection == null) {
			LOGGER.fatal("getCollection(...) argument is null");
			throw new IllegalArgumentException("collection argument is null");
		}

		MongoCollection<Document> mongoCollection;

		// Get access to a collection from the database
		try {
			mongoCollection = database.getCollection(collection.name())
					.withWriteConcern(writeConcern)
					.withReadPreference(readPreference);

		} catch (MongoException ex) {
			LOGGER.fatal(
				"Failed to find " + collection.name() + " collection in database " + database.getName()
			);
			throw ex;
		}

		LOGGER.info("retrieved MongoCollection object for " + collection.name());
		return mongoCollection;
	}

	@Override
	public void close() {
		client.close();
		LOGGER.info("MongoDB connection has been closed");
	}

	/**
	 * Ping the MongoDB cluster (or server) to check if the connectivity is up
	 * @return true if the application is able to reach the MongoDB cluster (or server), false otherwise
	 */
	@Override
	public boolean verifyConnectivity () {
		try {
			Bson command = new BsonDocument("ping", new BsonInt64(1));
			database.runCommand(command);
			return true;
		} catch (MongoException me) {
			return false;
		}
	}
}
