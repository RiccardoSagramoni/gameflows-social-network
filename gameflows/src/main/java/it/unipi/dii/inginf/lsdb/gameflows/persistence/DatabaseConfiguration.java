package it.unipi.dii.inginf.lsdb.gameflows.persistence;

interface DatabaseConfiguration {
	String getMongoUri();

	String getMongoDatabaseName();

	String getNeo4jUri();

	String getNeo4jUsername();

	String getNeo4jPassword();

	int getNeo4jConnectionAcquisitionTimeout();

	int getNeo4jConnectionTimeout();
}
