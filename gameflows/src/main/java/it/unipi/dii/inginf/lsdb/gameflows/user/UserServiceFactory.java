package it.unipi.dii.inginf.lsdb.gameflows.user;

/**
 * Factory which correctly allocates a UserService object
 */
public class UserServiceFactory {

	private static UserService service = null;

	public static UserService getService () {
		if (service == null) {
			service = new UserServiceImpl();
		}
		return service;
	}
}
