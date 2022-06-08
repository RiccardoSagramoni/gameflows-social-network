package it.unipi.dii.inginf.lsdb.gameflows.comment;

import org.bson.types.ObjectId;
import it.unipi.dii.inginf.lsdb.gameflows.persistence.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Interface which offers methods to handle the comment entity in the databases
 */
public interface CommentService {

	ObjectId addComment(@NotNull Comment comment);
	ObjectId addComment(MongoConnection mongoConnection,
	                    Neo4jConnection neo4jConnection,
	                    @NotNull Comment comment);

	Comment find(ObjectId id);
	Comment find(MongoConnection connection, ObjectId id);

	List<Comment> browseByPost(ObjectId postId, int skip, int limit);
	List<Comment> browseByPost(MongoConnection connection, ObjectId postId, int skip, int limit);

	boolean deleteCommentById(ObjectId id);
	boolean deleteCommentById(MongoConnection mongoConnection,
	                          Neo4jConnection neo4jConnection,
	                          ObjectId id);

	Boolean deleteCommentsByPostId_MongoDB(ObjectId postId);
	Boolean deleteCommentsByPostId_MongoDB(MongoConnection mongoConnection,
	                                       ObjectId postId);

	Boolean deleteCommentsByVideogameCommunity_MongoDB(ObjectId videogameCommunityId);
	Boolean deleteCommentsByVideogameCommunity_MongoDB(MongoConnection mongoConnection,
	                                                   ObjectId communityId);



	//AGGREGATION 1: AVG number of comments per post in a community (disc sorted)
	List<ResultAverageCommentPerPost> averageNumberOfCommentsPerPost(int skip, int limit);

	List<ResultAverageCommentPerPost> averageNumberOfCommentsPerPost(MongoConnection connection,
	                                                                 int skip,
	                                                                 int limit);

	//AGGREGATION 2: //AVG number of comments per post for each author (disc sorted)
	List<ResultAverageCommentPerUser> averageNumberOfCommentsPerUser();
	List<ResultAverageCommentPerUser> averageNumberOfCommentsPerUser(MongoConnection connection);

}
