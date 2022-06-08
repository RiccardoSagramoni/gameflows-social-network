package it.unipi.dii.inginf.lsdb.gameflows.persistence;

class DatabaseConfigurationMockup implements DatabaseConfiguration {

	/**
	 * Get the URI for MongoDB. URI contains the IPs and ports of the MongoDB servers.
	 *
	 * @return URI for MongoDB
	 */
	@Override
	public String getMongoUri() {
		return "mongodb://localhost:27018,localhost:27019,localhost:2720";
	}

	/**
	 * Get the name of the MongoDB database
	 *
	 * @return MongoDB database name
	 */
	@Override
	public String getMongoDatabaseName() {
		return "gameflows";
	}

	/**
	 * Get the URI for Neo4J. URI contains the IPs and ports of the NEO4J servers.
	 *
	 * @return URI for Neo4J
	 */
	@Override
	public String getNeo4jUri() {
		return "bolt://localhost:7687";
	}

	@Override
	public String getNeo4jUsername() {
		return "neo4j";
	}

	@Override
	public String getNeo4jPassword() {
		return "password";
	}

	@Override
	public int getNeo4jConnectionAcquisitionTimeout() {
		return 30;
	}

	@Override
	public int getNeo4jConnectionTimeout() {
		return 20;
	}
}
