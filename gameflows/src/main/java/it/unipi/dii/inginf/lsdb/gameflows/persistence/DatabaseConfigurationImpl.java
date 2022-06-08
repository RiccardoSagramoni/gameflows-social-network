package it.unipi.dii.inginf.lsdb.gameflows.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class which retrieves configuration values for the databases.
 */
class DatabaseConfigurationImpl implements DatabaseConfiguration {
	// Logger
	private static final Logger LOGGER = LogManager.getLogger(DatabaseConfigurationImpl.class);

	// Name of the configuration file
	private static final String FILENAME = "dbconfig.properties";

	// Properties
	private final String mongoUri;
	private final String mongoDatabaseName;

	private final String neo4jUri;
	private final String neo4jUsername;
	private final String neo4jPassword;
	private final int neo4jConnectionAcquisitionTimeout;
	private final int neo4jConnectionTimeout;


	/**
	 * Allocate the class and read the configuration file
	 */
	DatabaseConfigurationImpl() {
		Properties properties = new Properties();

		// Get the configuration file from the resources of our application
		try (InputStream inputStream =
				    DatabaseConfigurationImpl.class.getClassLoader().getResourceAsStream(FILENAME)
		) {
			// Read the file and load the property fields
			properties.load(inputStream);

		} catch (IOException e) {
			LOGGER.fatal("Database configuration not loaded");
			throw new RuntimeException("Database configuration not loaded");
		}

		// Read the fields
		mongoUri = properties.getProperty("mongoUri");
		mongoDatabaseName = properties.getProperty("mongoDatabaseName");

		neo4jUri = properties.getProperty("neo4jUri");
		neo4jUsername = properties.getProperty("neo4jUsername");
		neo4jPassword = properties.getProperty("neo4jPassword");
		neo4jConnectionAcquisitionTimeout =
				Integer.parseInt(properties.getProperty("neo4jConnectionAcquisitionTimeout"));
		neo4jConnectionTimeout = Integer.parseInt(properties.getProperty("neo4jConnectionTimeout"));

		LOGGER.info("Database configuration loaded");
	}


	/**
	 * Get the URI for MongoDB. URI contains the IPs and ports of the MongoDB servers.
	 *
	 * @return URI for MongoDB
	 */
	@Override
	public String getMongoUri() {
		return mongoUri;
	}

	/**
	 * Get the name of the MongoDB database
	 *
	 * @return MongoDB database name
	 */
	@Override
	public String getMongoDatabaseName() {
		return mongoDatabaseName;
	}

	/**
	 * Get the URI for Neo4J. URI contains the IPs and ports of the NEO4J servers.
	 *
	 * @return URI for Neo4J
	 */
	@Override
	public String getNeo4jUri() {
		return neo4jUri;
	}

	@Override
	public String getNeo4jUsername() {
		return neo4jUsername;
	}

	@Override
	public String getNeo4jPassword() {
		return neo4jPassword;
	}

	@Override
	public int getNeo4jConnectionAcquisitionTimeout() {
		return neo4jConnectionAcquisitionTimeout;
	}

	@Override
	public int getNeo4jConnectionTimeout() {
		return neo4jConnectionTimeout;
	}

	@Override
	public String toString() {
		return "DatabaseConfigurationImpl{" +
				"mongoUri='" + mongoUri + '\'' +
				", mongoDatabaseName='" + mongoDatabaseName + '\'' +
				", neo4jUri='" + neo4jUri + '\'' +
				", neo4jUsername='" + neo4jUsername + '\'' +
				", neo4jPassword='" + neo4jPassword + '\'' +
				", neo4jConnectionAcquisitionTimeout=" + neo4jConnectionAcquisitionTimeout +
				", neo4jConnectionTimeout=" + neo4jConnectionTimeout +
				'}';
	}
}
