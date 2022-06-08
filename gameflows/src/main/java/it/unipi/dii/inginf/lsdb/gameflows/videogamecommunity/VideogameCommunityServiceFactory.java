package it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity;

/**
 * Factory which correctly allocates an VideoGameService object
 */
public class VideogameCommunityServiceFactory {
	private static final VideogameCommunityService service = new VideogameCommunityServiceImpl();

	public static VideogameCommunityService getService() {
		return service;
	}

	static VideogameCommunityServiceImpl getImplementation () {
		return (VideogameCommunityServiceImpl) getService();
	}
}
