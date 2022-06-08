package it.unipi.dii.inginf.lsdb.gameflows.post;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;

class PostServiceImplTest {
	private PostServiceImpl sut;

	@BeforeEach
	void init () {
		sut = new PostServiceImpl();
	}

	@Test
	void TEST_insertPost () {
		Assertions.assertNotNull(sut.insertPost(new PostMockup()));
	}

	@Test
	void TEST_browsePostByLike () {
		List<Post> list = sut.browsePosts(
							new ObjectId("61dbfbbe92a81a85adeaf035"),
							PostFilter.like,
							false,
							0,
							10
		);
		Assertions.assertNotNull(list);
		list.forEach(System.out::println);
		System.out.println(list.size());
	}

	@Test
	void TEST_browsePostByDate () {
		List<Post> list = sut.browsePosts(
				new ObjectId("61dbfbbe92a81a85adeaf035"),
				PostFilter.date,
				false,
				0,
				5
		);
		Assertions.assertNotNull(list);
		list.forEach(System.out::println);
	}

	@Test
	void TEST_browsePostInfluencer () {
		List<Post> list = sut.browsePosts(
				new ObjectId("61dbfbbe92a81a85adeaf00f"),
				PostFilter.date,
				true,
				0,
				5
		);
		Assertions.assertNotNull(list);
		list.forEach(System.out::println);
	}

	@Test
	void TEST_find () {
		// GIVEN post exists in DB
		ObjectId id = sut.insertPost(new PostMockup());
		Assertions.assertNotNull(id);

		// THEN find post is successful
		Post post = sut.find(id);
		Assertions.assertNotNull(post);
		System.out.println(post);
	}

	@Test
	void TEST_likePost () {
		// GIVEN post exists
		ObjectId postId = sut.insertPost(new PostMockup());
		Assertions.assertNotNull(postId);

		// THEN like return true
		Assertions.assertTrue(
				sut.likePost(postId, "whitefish666", true)
		);
	}

	@Test
	void TEST_dislikePost () {
		// GIVEN post exists and is liked
		ObjectId postId = sut.insertPost(new PostMockup());
		Assertions.assertNotNull(postId);

		Assertions.assertTrue(
				sut.likePost(postId, "whitefish666", true)
		);

		// THEN dislike return true
		Assertions.assertTrue(
				sut.likePost(postId, "whitefish666", false)
		);
	}

	@Test
	void GIVEN_post_is_liked_THEN_like_post_fails () {
		// GIVEN
		ObjectId postId = sut.insertPost(new PostMockup());
		Assertions.assertNotNull(postId);

		Assertions.assertTrue(
				sut.likePost(postId,"whitefish666",true)
		);

		// THEN
		Assertions.assertFalse(
				sut.likePost(postId, "whitefish666", true)
		);
	}

	@Test
	void GIVEN_post_is_not_liked_THEN_dislike_post_fails () {
		// GIVEN
		ObjectId postId = sut.insertPost(new PostMockup());
		Assertions.assertNotNull(postId);

		// THEN
		Assertions.assertFalse(
				sut.likePost(postId, "whitefish666", false)
		);
	}

	@Test
	void TEST_getLikedPostsOfVideogameCommunity () {
		Set<ObjectId> set = sut.getLikedPostsOfVideogameCommunity(
				"whitefish666", new ObjectId("61dbfbbe92a81a85adeaf022")
		);
		Assertions.assertNotNull(set);
		System.out.println(set.size());
	}


	@Test
	void GIVEN_post_exists_THEN_deletePostById_returns_true () {
		// GIVEN post exists
		ObjectId postId = sut.insertPost(new PostMockup());
		Assertions.assertNotNull(postId);

		// THEN delete is successful
		Assertions.assertTrue(
				sut.deletePostById(postId)
		);
	}

	@Test
	void GIVEN_post_not_exist_THEN_deletePostById_returns_false () {
		Assertions.assertFalse(
				sut.deletePostById(new ObjectId())
		);
	}

	@Test
	void TEST_deletePostByVideogameCommunity_MongoDB () {
		// GIVEN posts exist
		Assertions.assertNotNull(sut.insertPost(new PostMockup()));

		// THEN return true
		Assertions.assertTrue(
				sut.deletePostByVideogameCommunity_MongoDB(
						new PostMockup().getVideogameCommunity().getVideogameCommunityId()
				)
		);
	}

	@Test
	void TEST_bestUsersByNumberOfPosts () {
		List<ResultBestUserByPostAggregation> list =
				sut.bestUsersByNumberOfPosts(new ObjectId("61dbfbbe92a81a85adeaf035"), 5);
		Assertions.assertNotNull(list);
		list.forEach(System.out::println);
	}

	@Test
	void TEST_bestVideogameCommunities () {
		List<ResultBestVideogameCommunityAggregation> list =
				sut.bestVideogameCommunities(
						Date.from(
								LocalDate.of(2010,1,1)
										.atStartOfDay()
										.atZone(ZoneId.systemDefault())
										.toInstant()
						),
						new Date(), 5);

		Assertions.assertNotNull(list);
		list.forEach(System.out::println);
	}


}
