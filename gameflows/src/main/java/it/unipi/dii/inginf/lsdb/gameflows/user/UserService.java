package it.unipi.dii.inginf.lsdb.gameflows.user;

import it.unipi.dii.inginf.lsdb.gameflows.persistence.MongoConnection;
import it.unipi.dii.inginf.lsdb.gameflows.persistence.Neo4jConnection;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface which offer a set of methods which allows the application
 * to handle a User entity
 */
public interface UserService {

	boolean login (String username, String password);
	boolean login (MongoConnection connection, String username, String password);

	Boolean isUserBlocked (String username);
	Boolean isUserBlocked (MongoConnection connection, String username);

	ObjectId insertUser (User user);
	ObjectId insertUser (MongoConnection mongoConnection,
	                     Neo4jConnection neo4jConnection,
	                     User user);

	List<User> browse (int skip, int limit);
	List<User> browse (@NotNull MongoConnection connection, int skip, int limit);

	User find (@NotNull String username);
	User find (@NotNull MongoConnection connection, @NotNull String username);

	List<User> search (@NotNull String match, int skip, int limit);
	List<User> search (@NotNull MongoConnection connection, @NotNull String match, int skip, int limit);
}
