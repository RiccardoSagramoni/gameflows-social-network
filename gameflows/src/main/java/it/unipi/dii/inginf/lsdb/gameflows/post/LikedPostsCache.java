package it.unipi.dii.inginf.lsdb.gameflows.post;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Class which implements a cache system for the posts liked
 * by the user in a given videogame community.
 * <br><br>
 * The class handles both the CRUD queries on the remote databases and
 * the CRUD on the corresponding local copy.<br>
 * In this way, we will reduce the number of requests to the databases.
 */
public class LikedPostsCache {
	// Logger
	private static final Logger LOGGER = LogManager.getLogger(LikedPostsCache.class);

	// Set containing the posts liked by the user
	private final Set<ObjectId> set;

	/**
	 * Instantiate the local copy.
	 * @param username user's username
	 * @param videogame id of the videogame community
	 */
	public LikedPostsCache (String username, ObjectId videogame) {
		set = PostServiceFactory
				.getImplementation()
				.getLikedPostsOfVideogameCommunity(username, videogame);

		LOGGER.info("LikedPostsCache instantiated for user " + username + ", videogame " + videogame);
	}

	/**
	 * Check if the user likes a specified post.
	 * @param postId id of the post
	 * @return true if the user likes the post, false otherwise
	 */
	public boolean isPostLiked (ObjectId postId) {
		LOGGER.debug("isPostLiked(): postId " + postId);
		return set.contains(postId);
	}

	/**
	 * Add or remove a "like" to a specified post.
	 * <br><br>
	 * In order to successfully add a "like", a :LIKES relationship between user and post
	 * can't already exist. <br>
	 * In order to successfully remove a "like", a :LIKES relationship between user and post
	 * must already exist
	 * @param postId id of the post
	 * @param user username of the user who wants to add/remove a like
	 * @param like true to add a like, false to remove the like
	 * @return true on success, false on failure
	 */
	public boolean likePost (@NotNull ObjectId postId, @NotNull String user, boolean like) {

		LOGGER.info("likePost() | postId " + postId + ", user " + user + ", like " + like);
		// Update remote database
		boolean ret = PostServiceFactory
				.getImplementation()
				.likePost(postId, user, like);

		// If successful, update the local copy
		if (ret) {
			if (like) {
				set.add(postId);
			}
			else {
				set.remove(postId);
			}
		}

		return ret;
	}

}
