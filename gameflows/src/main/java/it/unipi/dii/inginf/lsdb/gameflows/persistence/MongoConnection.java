package it.unipi.dii.inginf.lsdb.gameflows.persistence;

import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

/**
 * Interface which correctly set up the connection instances to the MongoDB cluster (or server)
 */
public interface MongoConnection extends AutoCloseable {
	MongoCollection<Document> getCollection(GameflowsCollection collection);

	MongoCollection<Document> getCollection(GameflowsCollection collection,
	                                        WriteConcern writeConcern,
	                                        ReadPreference readPreference);

	boolean verifyConnectivity();
}
