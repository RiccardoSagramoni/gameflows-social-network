package it.unipi.dii.inginf.lsdb.gameflows.user;

import it.unipi.dii.inginf.lsdb.gameflows.util.Password;
import org.bson.Document;

import java.util.Date;
import java.util.List;

/**
 * Bean class that represent an user of the social network
 */
public class User {
	private String username;
	private String email;
	private Password password;
	private String name;
	private String surname;
	private String gender;
	private Date dateOfBirth;
	private String nationality;
	private boolean isInfluencer;
	private boolean isBlocked;

	User (String username, String email, Password password, String name,
	             String surname, String gender, Date dateOfBirth, String nationality,
	             Boolean isInfluencer, Boolean isBlocked) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.name = name;
		this.surname = surname;
		this.gender = gender;
		this.dateOfBirth = dateOfBirth;
		this.nationality = nationality;
		this.isInfluencer = (isInfluencer != null) && isInfluencer;
		this.isBlocked = (isBlocked != null) && isBlocked;
	}

	public User (String username, String email, Password password, String name,
	             String surname, String gender, Date dateOfBirth, String nationality)
	{
		this.username = username;
		this.email = email;
		this.password = password;
		this.name = name;
		this.surname = surname;
		this.gender = gender;
		this.dateOfBirth = dateOfBirth;
		this.nationality = nationality;
		this.isInfluencer = false;
		this.isBlocked = false;
	}

	public User (String username) {
		this.username = username;
	}


	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getNationality() {
		return nationality;
	}

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public boolean isInfluencer() {
		return isInfluencer;
	}

	public void setInfluencer(boolean influencer) {
		isInfluencer = influencer;
	}

	public boolean isBlocked() {
		return isBlocked;
	}

	public void setBlocked(boolean blocked) {
		isBlocked = blocked;
	}

	@Override
	public String toString() {
		return "User{" +
				"username='" + username + '\'' +
				", email='" + email + '\'' +
				", password=" + password +
				", name='" + name + '\'' +
				", surname='" + surname + '\'' +
				", gender='" + gender + '\'' +
				", dateOfBirth=" + dateOfBirth +
				", nationality='" + nationality + '\'' +
				", isInfluencer=" + isInfluencer +
				", isBlocked=" + isBlocked +
				'}';
	}

	public static User fromDocument (Document document) {
		return new User(
				document.getString("username"),
				document.getString("email"),
				new Password(
						document.getEmbedded(List.of("password", "sha256"), String.class),
						document.getEmbedded(List.of("password", "salt"), String.class)
				),
				document.getString("name"),
				document.getString("surname"),
				document.getString("gender"),
				document.getDate("date-of-birth"),
				document.getString("nationality"),
				document.getBoolean("isInfluencer"),
				document.getBoolean("isBlocked")
		);
	}

	public Document toDocument () {
		return new Document()
				.append("username", username)
				.append("email", email)
				.append("password", password.toDocument())
				.append("name", name)
				.append("surname", surname)
				.append("gender", gender)
				.append("date-of-birth", dateOfBirth)
				.append("nationality", nationality)
				.append("isInfluencer", isInfluencer)
				.append("isBlocked", isBlocked);
	}
}
