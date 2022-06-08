package it.unipi.dii.inginf.lsdb.gameflows.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PasswordTest {

	@Test
	void WHEN_using_the_correct_password_THEN_checkPassword_is_true() {
		String password = "password";
		Password p1 = new Password(password);
		Assertions.assertTrue(p1.checkPassword(password));
	}

	@Test
	void WHEN_using_different_passwords_THEN_checkPassword_is_false () {
		Assertions.assertFalse(
				new Password("password").checkPassword("wrong")
		);
	}

	@Test
	void WHEN_create_two_instances_from_same_password_THEN_salts_are_not_equals () {
		String password = "password";
		Password p1 = new Password(password);
		Password p2 = new Password(password);
		Assertions.assertNotEquals(p1.getSalt(), p2.getSalt());
	}

	@Test
	void WHEN_create_two_instances_from_same_password_THEN_hash_are_not_equals () {
		String password = "password";
		Password p1 = new Password(password);
		Password p2 = new Password(password);
		Assertions.assertNotEquals(p1.getHashPassword(), p2.getHashPassword());
	}
}
