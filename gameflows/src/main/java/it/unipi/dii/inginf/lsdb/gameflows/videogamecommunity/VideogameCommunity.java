package it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * Bean class that represents a videogame community
 */
public class VideogameCommunity {

	private ObjectId id;
	private String name;
	private String summary;
	private List<String> gameMode;
	private List<String> platform;
	private String cover;
	private List<String> genre;
	private String collection;
	private Double aggregatedRating;
	private Date releaseDate;
	private List<String> developer;
	private List<String> publisher;

	public VideogameCommunity (
			ObjectId id, String name, String summary, List<String> gameMode, List<String> platform,
			String cover, List<String> genre, String collection, Double aggregatedRating,
			Date releaseDate, List<String> developer, List<String> publisher
	) {
		this.id = id;
		this.name = name;
		this.summary = summary;
		this.gameMode = gameMode;
		this.platform = platform;
		this.cover = cover;
		this.genre = genre;
		this.collection = collection;
		this.aggregatedRating = aggregatedRating;
		this.releaseDate = releaseDate;
		this.developer = developer;
		this.publisher = publisher;
	}

	public VideogameCommunity (
			String name, String summary, List<String> gameMode, List<String> platform,
			String cover, List<String> genre, String collection, Double aggregatedRating,
			Date releaseDate, List<String> developer, List<String> publisher
	) {
		this.id = null;
		this.name = name;
		this.summary = summary;
		this.gameMode = gameMode;
		this.platform = platform;
		this.cover = cover;
		this.genre = genre;
		this.collection = collection;
		this.aggregatedRating = aggregatedRating;
		this.releaseDate = releaseDate;
		this.developer = developer;
		this.publisher = publisher;
	}


	public VideogameCommunity (ObjectId id, String name) {
		this.id = id;
		this.name = name;
	}

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public List<String> getGameMode() {
		return gameMode;
	}

	public void setGameMode(List<String> gameMode) {
		this.gameMode = gameMode;
	}

	public List<String> getPlatform() {
		return platform;
	}

	public void setPlatform(List<String> platform) {
		this.platform = platform;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public List<String> getGenre() {
		return genre;
	}

	public void setGenre(List<String> genre) {
		this.genre = genre;
	}

	public String getCollection() {
		return collection;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public Double getAggregatedRating() {
		return aggregatedRating;
	}

	public void setAggregatedRating(Double aggregatedRating) {
		this.aggregatedRating = aggregatedRating;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public List<String> getDeveloper() {
		return developer;
	}

	public void setDeveloper(List<String> developer) {
		this.developer = developer;
	}

	public List<String> getPublisher() {
		return publisher;
	}

	public void setPublisher(List<String> publisher) {
		this.publisher = publisher;
	}

	/**
	 * Given a BSON document, convert it to a VideogameCommunity object
	 * @param doc BSON document
	 * @return converted object
	 */
	public static VideogameCommunity fromDocument (Document doc) throws ClassCastException {
		return new VideogameCommunity(
				doc.getObjectId("_id"),
				doc.getString("name"),
				doc.getString("summary"),
				doc.getList("game_modes", String.class),
				doc.getList("platforms", String.class),
				doc.getString("cover"),
				doc.getList("genres", String.class),
				doc.getString("collection"),
				doc.getDouble("aggregated_rating"),
				doc.get("release_date", Date.class),
				doc.getList("developers", String.class),
				doc.getList("publishers", String.class)
		);
	}

	/**
	 * Convert VideogameCommunity to BSON document
	 * @return BSON document
	 */
	public Document toDocument () {
		return new Document()
			.append("name", name)
			.append("summary", summary)
			.append("game_modes", gameMode)
			.append("platforms", platform)
			.append("cover", cover)
			.append("genres", genre)
			.append("collection", collection)
			.append("aggregated_rating", aggregatedRating)
			.append("release_date", releaseDate)
			.append("developers", developer)
			.append("publishers", publisher);
	}

	@Override
	public String toString() {
		return "VideogameCommunity{" +
				"id=" + id +
				", name='" + name + '\'' +
				", summary='" + summary + '\'' +
				", gameMode=" + gameMode +
				", platform=" + platform +
				", cover='" + cover + '\'' +
				", genre=" + genre +
				", collection='" + collection + '\'' +
				", aggregatedRating=" + aggregatedRating +
				", releaseDate=" + releaseDate +
				", developer=" + developer +
				", publisher=" + publisher +
				'}';
	}
}
