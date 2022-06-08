package it.unipi.dii.inginf.lsdb.gameflows.comment;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

class CommentServiceImplTest {
	private CommentServiceImpl sut;

	@BeforeEach
	void init(){
		sut = new CommentServiceImpl();
	}

	@Test
	void TEST_addComment () {
		Assertions.assertNotNull(sut.addComment(new CommentMockup()));
	}

	@Test
	void TEST_find () {
		ObjectId id = sut.addComment(new CommentMockup());
		Comment comment = sut.find(id);
		Assertions.assertNotNull(comment);
		Assertions.assertEquals(comment.getId(), id);
		System.out.println(comment);
	}

	@Test
	void TEST_browse () {
		List<Comment> list = sut.browseByPost(
				new ObjectId("61dbfbd392a81a85adeaf096"), 0, 100
		);
		Assertions.assertNotNull(list);
		System.out.println(list.size());
	}


	@Test
	void TEST_likeComment () {
		ObjectId id = sut.addComment(new CommentMockup());
		Assertions.assertTrue(sut.likeComment(id, "whitefish666", true));
		System.out.println(id);
	}

	@Test
	void GIVEN_comment_already_liked_THEN_like_fails () {
		// GIVEN
		ObjectId id = sut.addComment(new CommentMockup());
		Assertions.assertNotNull(id);
		Assertions.assertTrue(sut.likeComment(id, "whitefish666", true));

		// THEN
		Assertions.assertFalse(sut.likeComment(id, "whitefish666", true));
		System.out.println(id);
	}

	@Test
	void TEST_dislikeComment () {
		// GIVEN exist comment and like relationship
		ObjectId id = sut.addComment(new CommentMockup());
		Assertions.assertNotNull(id);
		Assertions.assertTrue(sut.likeComment(id, "whitefish666", true));

		// THEN delete is successful
		Assertions.assertTrue(sut.likeComment(id, "whitefish666", false));
		System.out.println(id);
	}

	@Test
	void TEST_deleteCommentById () {
		ObjectId id = sut.addComment(new CommentMockup());
		Assertions.assertNotNull(id);
		Assertions.assertTrue(sut.deleteCommentById(id));
	}

	@Test
	void TEST_deleteCommentsByPostId_MongoDB () {
		Assertions.assertTrue(
				sut.deleteCommentsByPostId_MongoDB(
						new ObjectId("61dbfbd392a81a85adeaf096")
				));

	}

	@Test
	void TEST_deleteCommentsByCommunityId () {
		Assertions.assertTrue(
			sut.deleteCommentsByVideogameCommunity_MongoDB(
				new ObjectId("61dbfbbe92a81a85adeaf00f")
			));
	}

	@Test
	void TEST_averageNumberOfCommentsPerPost () {
		List<ResultAverageCommentPerPost> list = sut.averageNumberOfCommentsPerPost(0, 10);
		Assertions.assertNotNull(list);
		System.out.println(list.get(0));
	}

	@Test
	void TEST_averageNumberOfCommentsPerUser () {
		List<ResultAverageCommentPerUser> list = sut.averageNumberOfCommentsPerUser();
		Assertions.assertNotNull(list);
		System.out.println(list.get(0));
	}

	@Test
	void TEST_getLikedCommentsOfPost() {
		Set<ObjectId> set = sut.getLikedCommentsOfPost(
				"whitefish666",
				new ObjectId("61dbfbe992a81a85adebe685")
		);
		Assertions.assertNotNull(set);
		System.out.println(set.size());
	}

}
