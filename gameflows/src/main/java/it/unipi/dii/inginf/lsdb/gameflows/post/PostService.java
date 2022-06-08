package it.unipi.dii.inginf.lsdb.gameflows.post;

import it.unipi.dii.inginf.lsdb.gameflows.persistence.MongoConnection;
import it.unipi.dii.inginf.lsdb.gameflows.persistence.Neo4jConnection;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

/**
 * Interface which offers methods to manipulate Post entity in the databases
 * or to run analytics on the posts
 */
public interface PostService {
	ObjectId insertPost(@NotNull Post post);
	ObjectId insertPost (@NotNull MongoConnection mongoConnection,
	                     @NotNull Neo4jConnection neo4jConnection,
	                     @NotNull Post post);

	List<Post> browsePosts(@NotNull ObjectId videogameId,
	                       @NotNull PostFilter filter, boolean influencerPost,
	                       int skip, int limit);
	List<Post> browsePosts(@NotNull MongoConnection connection,
	                       @NotNull ObjectId videogameId,
	                       @NotNull PostFilter filter, boolean influencerPost,
	                       int skip, int limit);

	Post find(@NotNull ObjectId id);
	Post find(@NotNull MongoConnection connection, @NotNull ObjectId id);


	boolean deletePostById(@NotNull ObjectId id);
	boolean deletePostById(@NotNull MongoConnection mongoConnection,
	                       @NotNull Neo4jConnection neo4jConnection,
	                       @NotNull ObjectId id);

	Boolean deletePostByVideogameCommunity_MongoDB(ObjectId videogameCommunityId);
	Boolean deletePostByVideogameCommunity_MongoDB(@NotNull MongoConnection connection,
	                                               ObjectId videogameCommunityId);

	List<ResultBestUserByPostAggregation> bestUsersByNumberOfPosts(@NotNull ObjectId videogameCommunityId,
	                                                               int limit);
	List<ResultBestUserByPostAggregation> bestUsersByNumberOfPosts(@NotNull MongoConnection connection,
	                                                               @NotNull ObjectId videogameCommunityId,
	                                                               int limit);

	List<ResultBestVideogameCommunityAggregation> bestVideogameCommunities(
			@NotNull Date fromDate, @NotNull Date toDate, int limit);
	List<ResultBestVideogameCommunityAggregation> bestVideogameCommunities(
			@NotNull MongoConnection connection, @NotNull Date fromDate,
			@NotNull Date toDate, int limit);

}
