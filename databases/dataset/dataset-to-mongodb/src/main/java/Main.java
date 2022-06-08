import com.mongodb.client.*;
import com.mongodb.client.result.*;
import org.bson.*;
import org.bson.types.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class Main {

	private MongoCollection<Document> comments;
	private MongoCollection<Document> posts;
	private MongoCollection<Document> videogames;

	public void run () {
		MongoClient client = MongoClients.create("mongodb://localhost:27017");
		MongoDatabase database = client.getDatabase("gameflows");

		comments = database.getCollection("comment");
		posts = database.getCollection("post");
		videogames = database.getCollection("videogame");

//		updatePostId();
//		updateVideogameId();
//		updateCommentId();
//		removeUnnecessaryFields();
//		insertRedundancyVideogameName();
//		convertAllTimestamp();

		client.close();
	}

	private void updatePostId() {
		System.out.println("updatePostId");
		int i = 1;

		// Replace id from post
		try (MongoCursor<Document> cursor = posts.find().noCursorTimeout(true).cursor()) {
			while (cursor.hasNext()) {
				Document thisPost = cursor.next();

				ObjectId id = (ObjectId)thisPost.get("_id");
				String redditId = (String)thisPost.get("id");

				comments.updateMany(
						eq("post.post_id", redditId),
						set("post.post_id", id)
				);

				if (i%100 == 0) {
					System.out.println(i);
				}
				i++;
			}
		}
	}

	private void updateVideogameId () {
		System.out.println("updateVideogameId");
		int i = 1;

		// Replace id from videogame in posts and comments
		try (MongoCursor<Document> cursor = videogames.find().noCursorTimeout(true).cursor()) {
			while (cursor.hasNext()) {
				Document thisVideogame = cursor.next();

				ObjectId id = (ObjectId)thisVideogame.get("_id");
				String redditId = Integer.toString((Integer)thisVideogame.get("id"));

				comments.updateMany(
						eq("post.community_id", redditId),
						set("post.community_id", id));

				posts.updateMany(
						eq("community.community_id", redditId),
						set("community.community_id", id)
				);

				if (i % 100 == 0) {
					System.out.println(i);
				}
				i++;
			}
		}
	}

	private void updateCommentId() {
		System.out.println("updateCommentId");
		int i = 1;

		// Replace id from videogame in posts and comments
		try (MongoCursor<Document> cursor = comments.find().cursor()) {
			while (cursor.hasNext()) {
				Document thisComment = cursor.next();

				ObjectId id = (ObjectId)thisComment.get("_id");
				String redditId = (String)thisComment.get("id");

				posts.updateMany(
						eq("comments.id", redditId),
						set("comments.$.id", id)
				);

				if (i % 100 == 0) {
					System.out.println(i);
				}
				i++;
			}
		}
	}

	private void removeUnnecessaryFields () {
		UpdateResult r;

		// Remove videogame id
		r = videogames.updateMany(
				exists("id"),
				unset("id")
		);
		System.out.println("Videogame: " + r);

		// Remove post id
		r = posts.updateMany(
				exists("id"),
				unset("id")
		);
		System.out.println("Posts: " + r);

		// Remove comment id
		r = comments.updateMany(
				exists("id"),
				unset("id")
		);
		System.out.println("Comments: " + r);

		// Remove subcomments id
		StringBuilder subcomment_elem = new StringBuilder("subcomments");
		StringBuilder subcomment_name = new StringBuilder("subcomments");

		do {
			r = comments.updateMany(
					and(
						exists(subcomment_name.toString()),
						not(size(subcomment_name.toString(), 0))
					),
					unset(subcomment_elem + ".$[].id")
			);
			System.out.println(r);

			subcomment_elem.append(".$[].subcomments");
			subcomment_name.append(".subcomments");
		}
		while(r.getMatchedCount() > 0);
	}

	private void insertRedundancyVideogameName () {
		System.out.println("insertRedundancyVideogameName");
		int i = 1;

		// Replace id from videogame in posts and comments
		try (MongoCursor<Document> cursor = videogames.find().noCursorTimeout(true).cursor()) {
			while (cursor.hasNext()) {
				Document thisVideogame = cursor.next();

				ObjectId id = (ObjectId)thisVideogame.get("_id");
				String name = (String)thisVideogame.get("name");

				comments.updateMany(
						eq("post.community_id", id),
						set("post.community_name", name)
				);

				if (i % 100 == 0) {
					System.out.println(i);
				}
				i++;
			}
		}
	}

	private void convertTimestampCollection (
		MongoCollection<Document> collection, String fieldIn, String fieldOut
	) {

		try (MongoCursor<Document> cursor = collection.find().noCursorTimeout(true).cursor()) {
			int i = 0;
			while (cursor.hasNext()) {
				Document doc = cursor.next();

				ObjectId id;
				String timestamp;

				try {
					id = (ObjectId)doc.get("_id");
					timestamp = (String)doc.get(fieldIn);
				} catch (ClassCastException ex) {
					System.err.println(ex.getMessage());
					continue;
				}

				if (id == null || timestamp == null) {
					continue;
				}

				collection.updateOne(
						eq("_id", id),
						set(fieldOut, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timestamp))
				);

				if (i % 100 == 0) {
					System.out.println(i);
				}
				i++;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (!fieldIn.equals(fieldOut)) {
			collection.updateMany(
					new Document(),
					unset(fieldIn)
			);
		}
	}

	private void convertAllTimestamp () {
		System.out.println("convertAllTimestamp: comments");
		convertTimestampCollection(comments, "date", "timestamp");

		System.out.println("convertAllTimestamp: posts");
		convertTimestampCollection(posts, "date", "timestamp");

		System.out.println("convertAllTimestamp: videogames");
		convertTimestampCollection(videogames, "release_date", "release_date");
	}

	public static void main (String[] args) {
		Main m = new Main();
		m.run();
	}
}
