package it.unipi.dii.inginf.lsdb.gameflows.post;

import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

/**
 * Contains a result of the aggregation "best videogame communities by amount of likes"
 */
public class ResultBestVideogameCommunityAggregation {
	private final ObjectId videogameCommunityId;
	private final String videogameCommunityName;
	private final int likes;

	public ResultBestVideogameCommunityAggregation (@NotNull ObjectId videogameCommunityId,
	                                                @NotNull String videogameCommunityName,
	                                                int likes)
	{
		this.videogameCommunityId = videogameCommunityId;
		this.videogameCommunityName = videogameCommunityName;
		this.likes = likes;
	}

	public ObjectId getVideogameCommunityId() {
		return videogameCommunityId;
	}

	public String getVideogameCommunityName() {
		return videogameCommunityName;
	}

	public int getLikes() {
		return likes;
	}

	@Override
	public String toString() {
		return "ResultBestVideogameCommunityAggregation{" +
				"videogameCommunityId=" + videogameCommunityId +
				", videogameCommunityName='" + videogameCommunityName + '\'' +
				", likes=" + likes +
				'}';
	}
}
