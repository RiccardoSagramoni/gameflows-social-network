package it.unipi.dii.inginf.lsdb.gameflows.post;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Class that stores the redundant information of the videogame community,
 * inside a document from Post collection
 */
public class InfoVideogameCommunity {
	private ObjectId videogameCommunityId;
	private String videogameCommunityName;
	private List<String> genre;

	public InfoVideogameCommunity(ObjectId videogameCommunityId, String videogameCommunityName, List<String> genre) {
		this.videogameCommunityId = videogameCommunityId;
		this.videogameCommunityName = videogameCommunityName;
		this.genre = genre;
	}

	public ObjectId getVideogameCommunityId() {
		return videogameCommunityId;
	}

	public void setVideogameCommunityId(ObjectId videogameCommunityId) {
		this.videogameCommunityId = videogameCommunityId;
	}

	public String getVideogameCommunityName() {
		return videogameCommunityName;
	}

	public void setVideogameCommunityName(String videogameCommunityName) {
		this.videogameCommunityName = videogameCommunityName;
	}

	public List<String> getGenre() {
		return genre;
	}

	public void setGenre(List<String> genre) {
		this.genre = genre;
	}

	public static InfoVideogameCommunity fromDocument(@NotNull Document doc) {
		return new InfoVideogameCommunity(
			doc.getObjectId("community_id"),
			doc.getString("community_name"),
			doc.getList("community_genre", String.class)
		);
	}

	public Document toDocument () {
		return new Document()
			.append("community_id", videogameCommunityId)
			.append("community_name", videogameCommunityName)
			.append("genre", genre);
	}

	@Override
	public String toString() {
		return "InfoVideogameCommunity{" +
				"videogameCommunityId=" + videogameCommunityId +
				", videogameCommunityName='" + videogameCommunityName + '\'' +
				", genre=" + genre +
				'}';
	}
}
