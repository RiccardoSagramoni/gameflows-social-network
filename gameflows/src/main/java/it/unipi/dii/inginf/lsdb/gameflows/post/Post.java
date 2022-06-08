package it.unipi.dii.inginf.lsdb.gameflows.post;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.util.Date;


/**
 * Bean class which represent a post
 */
public class Post {
	private ObjectId id;
	private String title;
	private int likes;
	private String author;
	private String text;
	private Date timestamp;
	private InfoVideogameCommunity videogameCommunity;
	private boolean isAuthorInfluencer;

	private Post (ObjectId id, String title, int likes,
	             String author, String text, Date timestamp,
	             InfoVideogameCommunity videogameCommunity,
	             Boolean isAuthorInfluencer)
	{
		this.id = id;
		this.title = title;
		this.likes = likes;
		this.author = author;
		this.text = text;
		this.timestamp = timestamp;
		this.videogameCommunity = videogameCommunity;
		this.isAuthorInfluencer = (isAuthorInfluencer != null) && isAuthorInfluencer;
	}

	public Post (String title, String author, String text,
	             InfoVideogameCommunity videogameCommunity,
	             Boolean isAuthorInfluencer)
	{
		this(new ObjectId(), title, 0, author, text,
				new Date(), videogameCommunity, isAuthorInfluencer);
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public InfoVideogameCommunity getVideogameCommunity() {
		return videogameCommunity;
	}

	public void setVideogameCommunity(InfoVideogameCommunity videogameCommunity) {
		this.videogameCommunity = videogameCommunity;
	}

	public boolean isAuthorInfluencer() {
		return isAuthorInfluencer;
	}

	public void setAuthorInfluencer(boolean authorInfluencer) {
		isAuthorInfluencer = authorInfluencer;
	}

	@Override
	public String toString() {
		return "Post{" +
			"id=" + id +
			", title='" + title + '\'' +
			", likes=" + likes +
			", author='" + author + '\'' +
			", text='" + text + '\'' +
			", timestamp=" + timestamp +
			", videogameCommunity=" + videogameCommunity +
			", isAuthorInfluencer=" + isAuthorInfluencer +
			'}';
	}

	public static Post fromDocument (@NotNull Document doc){
		return new Post(
			doc.getObjectId("_id"),
			doc.getString("title"),
			doc.getInteger("likes"),
			doc.getString("author"),
			doc.getString("text"),
			doc.getDate("timestamp"),
			InfoVideogameCommunity.fromDocument(doc.get("community", Document.class)),
			doc.getBoolean("isAuthorInfluencer")
		);
	}

	public Document toDocument () {
		Document doc = new Document()
			.append("title", title)
			.append("likes", likes)
			.append("author", author)
			.append("text", text)
			.append("timestamp", timestamp)
			.append("community", videogameCommunity.toDocument());

		if (isAuthorInfluencer) {
			doc.append("isAuthorInfluencer", true);
		}
		return doc;
	}
}
