package it.unipi.dii.inginf.lsdb.gameflows.post;

/**
 * Factory which correctly generate a new PostService object
 */
public class PostServiceFactory {
	private static final PostService service = new PostServiceImpl();

	public static PostService getService(){
		return service;
	}

	static PostServiceImpl getImplementation () {
		return (PostServiceImpl) service;
	}
}
