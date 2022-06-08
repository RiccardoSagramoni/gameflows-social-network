package it.unipi.dii.inginf.lsdb.gameflows.post;

import org.jetbrains.annotations.NotNull;

/**
 * Contains the result of the "best users by number of posts" aggregation
 */
public class ResultBestUserByPostAggregation {
	private final String username;
	private final int numPost;

	public ResultBestUserByPostAggregation(@NotNull String username, int numPost) {
		this.username = username;
		this.numPost = numPost;
	}

	public String getUsername() {
		return username;
	}

	public int getNumPost() {
		return numPost;
	}

	@Override
	public String toString() {
		return "ResultBestUserByPostAggregation{" +
			"username='" + username + '\'' +
			", numPost=" + numPost +
			'}';
	}
}
