package it.unipi.dii.inginf.lsdb.gameflows.comment;

import org.bson.types.ObjectId;

/**
 * Class that stores the result of the aggregation which
 * computes the average number of comments per post
 */
public class ResultAverageCommentPerPost {
	private final ObjectId videogameId;
	private final String videogameName;
	private final Double average;

	public ResultAverageCommentPerPost(ObjectId videogameId, String videogameName, Double average) {
		this.videogameId = videogameId;
		this.videogameName = videogameName;
		this.average = average;
	}

	public ObjectId getVideogameId() {
		return videogameId;
	}

	public String getVideogameName() {
		return videogameName;
	}

	public Double getAverage() {
		return average;
	}

	@Override
	public String toString() {
		return "ResultAverageCommentPerPost{" +
			"videogameId=" + videogameId +
			", videogameName='" + videogameName + '\'' +
			", average=" + average +
			'}';
	}
}
