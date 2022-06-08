package it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity;

import it.unipi.dii.inginf.lsdb.gameflows.persistence.MongoConnection;
import it.unipi.dii.inginf.lsdb.gameflows.persistence.Neo4jConnection;
import it.unipi.dii.inginf.lsdb.gameflows.user.UserMockup;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

class VideogameCommunityServiceImplTest {
	VideogameCommunityServiceImpl sut;

	@BeforeEach
	void init () {
		sut = new VideogameCommunityServiceImpl();
	}

	@Test
	void WHEN_MongoConnection_is_null_THEN_insertVideogameCommunity_throws_IllegalArgumentException () {
		Assertions.assertThrows(IllegalArgumentException.class, () ->
				sut.insertVideogameCommunity(
						null,
						Mockito.mock(Neo4jConnection.class),
						new VideogameCommunityMockup()
				)
		);
	}

	@Test
	void WHEN_Neo4jConnection_is_null_THEN_insertVideogameCommunity_throws_IllegalArgumentException () {
		Assertions.assertThrows(IllegalArgumentException.class, () ->
				sut.insertVideogameCommunity(
						Mockito.mock(MongoConnection.class),
						null,
						new VideogameCommunityMockup()
				)
		);
	}

	@Test
	void WHEN_MongoConnection_is_null_THEN_deleteVideogameCommunity_throws_IllegalArgumentException () {
		Assertions.assertThrows(IllegalArgumentException.class, () ->
				sut.deleteVideogameCommunity(
						null,
						Mockito.mock(Neo4jConnection.class),
						new VideogameCommunityMockup().getId()
				)
		);
	}

	@Test
	void WHEN_Neo4jConnection_is_null_THEN_deleteVideogameCommunity_throws_IllegalArgumentException () {
		Assertions.assertThrows(IllegalArgumentException.class, () ->
				sut.deleteVideogameCommunity(
						Mockito.mock(MongoConnection.class),
						null,
						new VideogameCommunityMockup().getId()
				)
		);
	}

	@Test
	void WHEN_MongoConnection_is_null_THEN_view_throws_IllegalArgumentException () {
		Assertions.assertThrows(IllegalArgumentException.class, () ->
				sut.browse(null,1,1)
		);
	}

	@Test
	void WHEN_MongoConnection_is_null_THEN_search_throws_IllegalArgumentException () {
		Assertions.assertThrows(IllegalArgumentException.class, () ->
				sut.search(null,"",1,1)
		);
	}

	@Test
	void TEST_search () {
		List<VideogameCommunity> list = sut.search("battle", 0, 3);
		Assertions.assertNotNull(list);
		list.forEach(System.out::println);
	}

	@Test
	void TEST_browse () {
		List<VideogameCommunity> list = sut.browse(0, 10);
		Assertions.assertNotNull(list);
		//list.forEach(System.out::println);
		System.out.println(list.size());
	}

	@Test
	void TEST_find () {
		VideogameCommunity v = sut.find(new ObjectId("61dbfbbe92a81a85adeaf00f"));
		System.out.println(v);
		Assertions.assertNotNull(v);
	}

	@Test
	void TEST_insertVideogameCommunity () {
		Assertions.assertNotNull(sut.insertVideogameCommunity(new VideogameCommunityMockup()));
	}

	@Test
	void TEST_deleteVideogameCommunity () {
		VideogameCommunity videogame = new VideogameCommunityMockup();
		// Insert
		ObjectId id = sut.insertVideogameCommunity(videogame);
		// Delete the previously inserted videogame
		Assertions.assertTrue(sut.deleteVideogameCommunity(id));
	}

	@Test
	void WHEN_delete_not_existing_videogame_THEN_deleteVideogameCommunity_returns_false () {
		Assertions.assertFalse(
				sut.deleteVideogameCommunity(new ObjectId())
		);
	}

	@Test
	void TEST_followVideogameCommunity () {
		// Follow
		Assertions.assertTrue(
				sut.followVideogameCommunity(
						new VideogameCommunityMockup(new ObjectId("61dbfbbe92a81a85adeaf00f")),
						UserMockup.generateUser("whitefish666"),
						true
				)
		);

		// Unfollow
		Assertions.assertTrue(
				sut.followVideogameCommunity(
						new VideogameCommunityMockup(new ObjectId("61dbfbbe92a81a85adeaf00f")),
						UserMockup.generateUser("whitefish666"),
						false
				)
		);
	}

	@Test
	void TEST_viewFollowedVideogames () {
		FollowedVideogames videogames = sut.viewFollowedVideogames(
				UserMockup.generateUser("whitefish666")
		);
		Assertions.assertNotNull(videogames);

		videogames.getList().forEach((v) -> System.out.println(v.getName()));
	}

	@Test
	void TEST_viewSuggestedVideogameCommunities () {
		List<VideogameCommunity> list = sut.viewSuggestedVideogameCommunities(
				UserMockup.generateUser("whitefish666"),
				5
		);
		Assertions.assertNotNull(list);
		list.forEach((v) -> System.out.println(v.getName()));
	}

}
