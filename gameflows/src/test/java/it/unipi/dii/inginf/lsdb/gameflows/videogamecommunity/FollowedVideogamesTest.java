package it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class FollowedVideogamesTest {

	FollowedVideogames sut;

	@BeforeEach
	void init() {
		sut = new FollowedVideogames();
	}

	@Test
	void GIVEN_added_list_THEN_stored_list_is_equal () {
		List<VideogameCommunity> list = new ArrayList<>(5);

		for (int i = 0; i < 5; i++) {
			list.add(new VideogameCommunityMockup(new ObjectId()));
		}

		sut.addAll(list);
		List<VideogameCommunity> storedList = sut.getList();

		Assertions.assertNotNull(storedList);
		Assertions.assertEquals(list, storedList);
	}

	@Test
	void GIVEN_added_videogame_THEN_isVideogameFollowed_returns_true () {
		// GIVEN
		VideogameCommunity v = new VideogameCommunityMockup(new ObjectId());
		sut.add(v.getId(), v.getName());

		// THEN
		Assertions.assertTrue(sut.isVideogameFollowed(v.getId()));
	}

	@Test
	void GIVEN_videogame_not_added_THEN_isVideogameFollowed_returns_false () {
		Assertions.assertFalse(sut.isVideogameFollowed(new ObjectId()));
	}

	@Test
	void TEST_remove () {
		VideogameCommunity v = new VideogameCommunityMockup(new ObjectId());

		sut.add(v.getId(), v.getName());
		Assertions.assertTrue(sut.isVideogameFollowed(v.getId()));

		sut.remove(v.getId());
		Assertions.assertFalse(sut.isVideogameFollowed(v.getId()));
	}
}
