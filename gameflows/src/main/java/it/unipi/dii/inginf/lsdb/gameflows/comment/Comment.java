package it.unipi.dii.inginf.lsdb.gameflows.comment;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * Bean class that represent a comment
 */
public class Comment {
	private ObjectId id;
	private String text;
	private int likes;
	private Date timestamp;
	private String author;
	private InfoPost post;

	private Comment (ObjectId id, String text, int likes, Date timestamp,
	         String author, InfoPost post)
	{
		this.id = id;
		this.text = text;
		this.likes = likes;
		this.timestamp = timestamp;
		this.author = author;
		this.post = post;
	}

	public Comment (String text, String author, InfoPost post) {
		this(null, text, 0, new Date(), author, post);
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public InfoPost getPost() {
		return post;
	}

	public void setPost(InfoPost post) {
		this.post = post;
	}

	//convert document
	public static Comment fromDocument (Document doc){
		return new Comment(
			doc.getObjectId("_id"),
			doc.getString("text"),
			doc.getInteger("likes"),
			doc.getDate("timestamp"),
			doc.getString("author"),
			InfoPost.fromDocument(doc.getEmbedded(List.of("post"), Document.class))
		);
	}

	public Document toDocument () {
		return new Document()
			.append("text", text)
			.append("likes", likes)
			.append("author", author)
			.append("timestamp", timestamp)
			.append("post", post.toDocument());
	}

	@Override
	public String toString() {
		return "Comment{" +
			"id=" + id +
			", text='" + text + '\'' +
			", likes=" + likes +
			", timestamp=" + timestamp +
			", author='" + author + '\'' +
			", post=" + post +
			'}';
	}
}







