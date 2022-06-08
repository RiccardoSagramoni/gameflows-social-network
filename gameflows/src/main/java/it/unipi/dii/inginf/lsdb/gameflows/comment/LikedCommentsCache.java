package it.unipi.dii.inginf.lsdb.gameflows.comment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Class which implements a cache system for the comments liked
 * by the user under a specified post.
 * <br><br>
 * The class handles both the CRUD queries on the remote databases and
 * the CRUD on the corresponding local copy.<br>
 * In this way, we will reduce the number of requests to the databases.
 */
public class LikedCommentsCache {
	// Logger
	private static final Logger LOGGER = LogManager.getLogger(LikedCommentsCache.class);

	// Set containing the posts liked by the user
	private final Set<ObjectId> set;

	/**
	 * Instantiate the local copy.
	 * @param username user's username
	 * @param post id of the post
	 */
	public LikedCommentsCache(String username, ObjectId post) {
		set = CommentServiceFactory
				.getImplementation()
				.getLikedCommentsOfPost(username, post);

		LOGGER.info("LikedCommentsCache instantiated for user " + username + ", post " + post);
	}

	/**
	 * Check if the user likes a specified comment.
	 * @param commentId id of the comment
	 * @return true if the user likes the comment, false otherwise
	 */
	public boolean isCommentLiked (ObjectId commentId) {
		LOGGER.debug("isCommentLiked(): commentId " + commentId);
		return set.contains(commentId);
	}

	/**
	 * Add or remove a "like" to a specified comment.
	 * <br><br>
	 * In order to successfully add a "like", a :LIKES relationship between user and comment
	 * can't already exist. <br>
	 * In order to successfully remove a "like", a :LIKES relationship between user and comment
	 * must already exist
	 * @param commentId id of the comment
	 * @param user username of the user who wants to add/remove a like
	 * @param like true to add a like, false to remove the like
	 * @return true on success, false on failure
	 */
	public boolean likeComment (@NotNull ObjectId commentId, @NotNull String user, boolean like) {

		LOGGER.info("likeComment() | commentId " + commentId + ", user " + user + ", like " + like);
		// Update remote database
		boolean ret = CommentServiceFactory
				.getImplementation()
				.likeComment(commentId, user, like);

		// If successful, update the local copy
		if (ret) {
			if (like) {
				set.add(commentId);
			}
			else {
				set.remove(commentId);
			}
		}

		return ret;
	}

}