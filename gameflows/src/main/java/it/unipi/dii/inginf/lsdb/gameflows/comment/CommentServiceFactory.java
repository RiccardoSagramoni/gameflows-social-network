package it.unipi.dii.inginf.lsdb.gameflows.comment;

/**
 * Factory which create a singleton instance of CommentService
 */
public class CommentServiceFactory {

	private static final CommentService service = new CommentServiceImpl();

	public static CommentService getService(){
		return service;
	}

	static CommentServiceImpl getImplementation () {
		return (CommentServiceImpl) service;
	}
}
