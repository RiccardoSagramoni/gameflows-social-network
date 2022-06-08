package it.unipi.dii.inginf.lsdb.gameflows.admin;

import it.unipi.dii.inginf.lsdb.gameflows.util.Password;

public class AdminMockup extends Admin {
	public static final String CLEARTEXT_PASSWORD = "admin";

	public AdminMockup () {
		this("mockup" + System.currentTimeMillis());
	}

	public AdminMockup (String username) {
		super(username, new Password(CLEARTEXT_PASSWORD), "mockup");
	}
}
