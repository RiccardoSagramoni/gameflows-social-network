package it.unipi.dii.inginf.lsdb.gameflows.admin;

import it.unipi.dii.inginf.lsdb.gameflows.util.Password;
import org.bson.Document;

import java.util.List;

/**
 * Bean class that represents an admin of the social network
 */
public class Admin {
	private String username;
	private Password password;
	private String name;

	public Admin (String username, Password password, String name) {
		this.username = username;
		this.password = password;
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Password getPassword() {
		return password;
	}

	public void setPassword(Password password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	/*
	@Override
	public String toString() {
		return "Admin{" +
				"username='" + username + '\'' +
				", password=" + password +
				", name='" + name + '\'' +
				'}';
	}

	*/

	public static Admin fromDocument (Document document) {
		return new Admin(
				document.getString("username"),
				new Password(
						document.getEmbedded(List.of("password", "sha256"), String.class),
						document.getEmbedded(List.of("password", "salt"), String.class)
				),
				document.getString("name")
		);
	}


	public Document toDocument() {
		return new Document()
				.append("username", username)
				.append("password", password.toDocument())
				.append("name", name);
	}


}
