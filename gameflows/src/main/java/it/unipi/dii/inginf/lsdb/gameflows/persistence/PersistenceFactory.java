package it.unipi.dii.inginf.lsdb.gameflows.persistence;

/**
 * Factory which correctly allocates the instances of MongoConnection,
 * Neo4jConnection and DatabaseConfiguration.
 * DatabaseConfiguration is a singleton instance.
 */
public class PersistenceFactory {
	private static DatabaseConfiguration databaseConfiguration = null;

	public static MongoConnectionImpl getMongoConnection () {
		return new MongoConnectionImpl();
	}

	public static Neo4jConnectionImpl getNeo4jConnection() {
		return new Neo4jConnectionImpl();
	}

	static DatabaseConfiguration getDatabaseConfiguration() {
		if (databaseConfiguration == null) {
			databaseConfiguration = new DatabaseConfigurationImpl();
		}
		return databaseConfiguration;
	}
}
