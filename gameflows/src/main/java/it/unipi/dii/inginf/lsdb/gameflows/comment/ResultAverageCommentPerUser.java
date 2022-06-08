package it.unipi.dii.inginf.lsdb.gameflows.comment;

/**
 * Class that stores the result of the aggregation which
 * computes the average number of comments per user
 */
public class ResultAverageCommentPerUser {
	private final String username;
	private final Double average;

	public ResultAverageCommentPerUser(String username, Double average) {
		this.username = username;
		this.average = average;
	}

	public String getUsername() {
		return username;
	}

	public Double getAverage() {
		return average;
	}

	@Override
	public String toString() {
		return "ResultAverageCommentPerUser{" +
			"username='" + username + '\'' +
			", average=" + average +
			'}';
	}
}
