package it.unipi.dii.inginf.lsdb.gameflows.comment;

public class CommentMockup extends Comment {

	public CommentMockup () {
		this("whitefish666");
	}

	public CommentMockup (String username) {
		super("mockup", username, new InfoPostMockup());
	}

}
