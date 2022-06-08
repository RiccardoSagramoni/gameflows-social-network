package it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity;

import it.unipi.dii.inginf.lsdb.gameflows.user.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.List;

/**
 * Singleton class which implements a cache system for the followed communities of the user.
 * The class handles both the CRUD queries on the remote databases and
 * the CRUD on the corresponding local copy.
 * In this way, we will reduce the number of requests to the databases.
 */
public class FollowedVideogameCache {
	// Logger
	private static final Logger LOGGER = LogManager.getLogger(FollowedVideogameCache.class);

	// Instance of the singleton object
	private static FollowedVideogameCache instance = null;
	// User who follows the listed communities
	private static User user = null;

	// List of followed communities
	private final FollowedVideogames followedVideogames;

	/**
	 * Return a singleton instance of the class
	 * @param currentUser current logged user
	 * @return instance of FollowedVideogameCache
	 */
	public static FollowedVideogameCache getInstance (@NotNull User currentUser) {
		// Check if instance for the current user has already been allocated
		if (instance == null ||
			user == null ||
			!currentUser.getUsername().equals(user.getUsername())
		){
			LOGGER.info("getInstance() | Generate FollowedVideogameCache instance");
			user = currentUser;
			instance = new FollowedVideogameCache(currentUser);
		}

		return instance;
	}

	/**
	 * Private constructor: it fetches data from remote databases
	 * @param user current user
	 */
	@TestOnly
	FollowedVideogameCache (@NotNull User user) {
		followedVideogames = VideogameCommunityServiceFactory
				.getImplementation()
				.viewFollowedVideogames(user);
	}

	/**
	 * Check if a videogame community is followed by a user
	 * @param id id of the videogame community
	 * @return true if the community is followed, false otherwise
	 */
	public boolean isVideogameFollowed (@NotNull ObjectId id) {
		LOGGER.info("isVideogameFollowed()");
		return followedVideogames.isVideogameFollowed (id);
	}

	/**
	 * Get the followed videogame communities as a List.
	 * Only the id and name field are set, the others are null.
	 * @return the list of followed communities
	 */
	public List<VideogameCommunity> getList () {
		LOGGER.info("getList()");
		return followedVideogames.getList();
	}

	/**
	 * Follow/unfollow a videogame community.
	 * @param videogameCommunity id of the videogame community to follow
	 * @param follow true if the user wants to follow, false otherwise
	 * @return true on success, false on failure
	 */
	public boolean followVideogameCommunity (@NotNull VideogameCommunity videogameCommunity,
	                                         boolean follow)
	{
		LOGGER.info("followVideogameCommunity()");

		// Update the remote databases first (original copy)
		boolean ret = VideogameCommunityServiceFactory
				.getImplementation()
				.followVideogameCommunity(
						videogameCommunity,
						user,
						follow
				);

		// If the database query was successful, update the local copy
		if (ret) {
			if (follow) {
				followedVideogames.add(videogameCommunity.getId(), videogameCommunity.getName());
			}
			else {
				followedVideogames.remove(videogameCommunity.getId());
			}
		}

		// Return the result of the remote query
		return ret;
	}

}
