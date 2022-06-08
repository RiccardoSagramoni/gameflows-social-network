package it.unipi.dii.inginf.lsdb.gameflows.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

/**
 * Represent a password of the gameflows database.
 * A Password object is composed by the SHA-256 hash of the real password
 * and a randomly generated salt (in order to prevent rainbow table attacks)
 */
public class Password {
	private static final Logger LOGGER = LogManager.getLogger(Password.class);

	private static final String SALT_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final int SALT_SIZE = 8;

	private final String hashPassword;
	private final String salt;

	public Password (String hashPassword, String salt) {
		this.hashPassword = hashPassword;
		this.salt = salt;
	}

	/**
	 * Generate salt and hash of a given password
	 * @param password cleartext password
	 */
	public Password (@NotNull String password) {
		try {
			// Generate alphanumeric string as salt
			salt = SecureRandom.getInstanceStrong()
					.ints(
						SALT_SIZE,
						0,
						SALT_CHARS.length())
					.mapToObj(SALT_CHARS::charAt)
					.collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
					.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		// Compute hash
		hashPassword = DigestUtils.sha256Hex(password + salt);
	}

	public String getHashPassword() {
		return hashPassword;
	}

	public String getSalt() {
		return salt;
	}

	/**
	 * Check it the specified password's hash is the same of the current object
	 * @param password cleartext password
	 * @return true is password is correct, false otherwise
	 */
	public boolean checkPassword (@NotNull String password) {
		boolean result = DigestUtils.sha256Hex(password + salt).equals(hashPassword);
		if (!result) {
			LOGGER.error("checkPassword() | Password doesn't match");
		}
		return result;
	}

	public static Password fromDocument (@NotNull Document document) {
		return new Password(
				document.getEmbedded(List.of("password", "sha256"), String.class),
				document.getEmbedded(List.of("password", "salt"), String.class)
		);
	}

	public Document toDocument () {
		return new Document()
				.append("sha256", hashPassword)
				.append("salt", salt);
	}

	@Override
	public String toString() {
		return "Password{" +
				"hashPassword='" + hashPassword + '\'' +
				", salt='" + salt + '\'' +
				'}';
	}
}
