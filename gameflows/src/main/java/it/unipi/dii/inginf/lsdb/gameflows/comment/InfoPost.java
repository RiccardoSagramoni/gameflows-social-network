package it.unipi.dii.inginf.lsdb.gameflows.comment;

import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Redundant information of the post to which a comment reply
 */
public class InfoPost {
	private ObjectId postId;
	private String author;
	private ObjectId communityId;
	private String communityName;


	public InfoPost(ObjectId postId, String author, ObjectId communityId, String communityName) {
		this.postId = postId;
		this.author = author;
		this.communityId = communityId;
		this.communityName = communityName;
	}

	public ObjectId getPostId() {
		return postId;
	}

	public void setPostId(ObjectId postId) {
		this.postId = postId;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public ObjectId getCommunityId() {
		return communityId;
	}

	public void setCommunityId(ObjectId communityId) {
		this.communityId = communityId;
	}

	public String getCommunityName() {
		return communityName;
	}

	public void setCommunityName(String communityName) {
		this.communityName = communityName;
	}

	@Override
	public String toString() {
		return "InfoPost{" +
				"post_id=" + postId +
				", author='" + author + '\'' +
				", community_id=" + communityId +
				", community_name='" + communityName + '\'' +
				'}';
	}

	public Document toDocument () {
		return new Document()
				.append("post_id", postId)
				.append("author", author)
				.append("community_id", communityId)
				.append("community_name", communityName);
	}

	public static InfoPost fromDocument (Document post) {
		return new InfoPost(
			post.getObjectId("post_id"),
			post.getString("author"),
			post.getObjectId("community_id"),
			post.getString("community_name")
		);
	}
}
