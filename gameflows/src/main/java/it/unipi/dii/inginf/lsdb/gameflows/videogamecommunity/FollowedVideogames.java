package it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity;

import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintains a local copy of the followed videogame communities
 * by the user of the application.
 * It offers methods to keep the local copy updated.
 */
class FollowedVideogames {

	// Two data structure used to organize the videogame communities
	// both sequentially (list) and by id (map)
	private final Map<ObjectId, String> map = new HashMap<>();
	private final List<VideogameCommunity> list = new ArrayList<>();

	/**
	 * Check if a videogame community is followed by the user
	 * @param videogameId id of the videogame
	 * @return true if the user follows the community, false otherwise
	 */
	boolean isVideogameFollowed (@NotNull ObjectId videogameId) {
		return (map.get(videogameId) != null);
	}

	/**
	 * Get the followed videogame communities as a List.
	 * Only the id and name field are set, the others are null.
	 * @return the list of followed communities
	 */
	List<VideogameCommunity> getList() {
		return list;
	}

	/**
	 * Add a followed community to the local copy
	 * @param id id of the followed community
	 * @param name name of the followed community
	 */
	void add (@NotNull ObjectId id, @NotNull String name) {
		map.put(id, name);
		list.add(new VideogameCommunity(id, name));
	}

	/**
	 * Add all videogames of a given list
	 * @param inputList list of videogames to insert
	 */
	void addAll (@NotNull List<VideogameCommunity> inputList) {
		inputList.forEach(v -> map.put(v.getId(), v.getName()));
		list.addAll(inputList);
	}

	/**
	 * Remove a followed community from the local copy
	 * @param id id of the followed community
	 */
	void remove (@NotNull ObjectId id) {
		// Remove from the map
		if (map.remove(id) == null) {
			return; // Exit if community isn't followed (optimization to avoid to iterate the list)
		}

		// Remove from the list
		int indexToDelete;
		for (indexToDelete = 0; indexToDelete < list.size(); indexToDelete++) {
			if (list.get(indexToDelete).getId().equals(id)) {
				break;
			}
		}

		// Check for out of bound
		if (indexToDelete < list.size()) {
			list.remove(indexToDelete);
		}
	}
}
