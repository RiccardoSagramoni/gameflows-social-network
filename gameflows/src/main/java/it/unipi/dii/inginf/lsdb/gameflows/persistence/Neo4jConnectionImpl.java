package it.unipi.dii.inginf.lsdb.gameflows.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;

import java.util.concurrent.TimeUnit;

class Neo4jConnectionImpl implements Neo4jConnection {
	private static final Logger LOGGER = LogManager.getLogger(Neo4jConnectionImpl.class);

	private final Driver driver;

	Neo4jConnectionImpl() {
		this(PersistenceFactory.getDatabaseConfiguration());
	}

	Neo4jConnectionImpl(DatabaseConfiguration conf) {
		if(conf == null) {
			LOGGER.fatal("database configuration is null");
			throw new IllegalArgumentException("DatabaseConfiguration parameter cannot be null");
		}

		// Create a connection pool
		try {
			Config config = Config.builder()
					.withConnectionAcquisitionTimeout(
							conf.getNeo4jConnectionAcquisitionTimeout(), TimeUnit.SECONDS)
					.withConnectionTimeout(
							conf.getNeo4jConnectionTimeout(), TimeUnit.SECONDS)
					.build();

			driver = GraphDatabase.driver(
					conf.getNeo4jUri(),
					AuthTokens.basic(conf.getNeo4jUsername(), conf.getNeo4jPassword()),
					config
			);
		} catch (Neo4jException ex) {
			LOGGER.fatal("cannot connect to Neo4J database");
			throw ex;
		}

		LOGGER.info("connection to Neo4J acquired");
	}

	@Override
	public void close() {
		driver.close();
		LOGGER.info("Neo4j connection has been closed");
	}

	@Override
	public Session getSession() {
		LOGGER.info("get new neo4j session");
		return driver.session();
	}

	/**
	 * Verify if the application can connect to the server
	 * @return true if it can connect
	 */
	@Override
	public boolean verifyConnectivity() {
		try {
			driver.verifyConnectivity();
			return true;

		} catch (Neo4jException ex) {
			LOGGER.error("verifyConnectivity() | Unable to connect to neo4j server: " + ex);
			return false;
		}
	}

}
