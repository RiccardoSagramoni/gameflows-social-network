package it.unipi.dii.inginf.lsdb.gameflows.admin;

/**
 * Factory which correctly allocate AdminService objects
 */
public class AdminServiceFactory {
	private static AdminService instance = null;

	public static AdminService getInstance () {
		if (instance == null) {
			instance = new AdminServiceImpl();
		}
		return instance;
	}
}
