package it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity;

import it.unipi.dii.inginf.lsdb.gameflows.persistence.MongoConnection;
import it.unipi.dii.inginf.lsdb.gameflows.persistence.Neo4jConnection;
import it.unipi.dii.inginf.lsdb.gameflows.user.User;
import org.bson.types.ObjectId;

import java.util.List;

public interface VideogameCommunityService {

	ObjectId insertVideogameCommunity (VideogameCommunity videogameCommunity);
	ObjectId insertVideogameCommunity (MongoConnection mongoConnection,
	                                   Neo4jConnection neo4jConnection,
	                                   VideogameCommunity videogameCommunity);

	Boolean deleteVideogameCommunity (ObjectId videogameCommunityId);
	Boolean deleteVideogameCommunity (MongoConnection mongoConnection,
	                                  Neo4jConnection neo4jConnection,
	                                  ObjectId videogameCommunityId);

	VideogameCommunity find (ObjectId id);
	VideogameCommunity find (MongoConnection connection, ObjectId id);

	List<VideogameCommunity> browse (int skip, int limit);
	List<VideogameCommunity> browse (MongoConnection connection, int skip, int limit);

	List<VideogameCommunity> search (String match, int skip, int limit);
	List<VideogameCommunity> search (MongoConnection connection,
	                                 String match, int skip, int limit);

	List<VideogameCommunity> viewSuggestedVideogameCommunities (User user, int limit);
	List<VideogameCommunity> viewSuggestedVideogameCommunities (Neo4jConnection connection,
	                                                            User user, int limit);
}
