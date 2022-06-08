package it.unipi.dii.inginf.lsdb.gameflows.admin;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

class AdminServiceImplTest {
	AdminServiceImpl sut;

	@BeforeEach
	void init () {
		sut = new AdminServiceImpl();
	}

	@Test
	void TEST_createAdminAccount () {
		Assertions.assertTrue(sut.createAdminAccount(new AdminMockup()));
	}

	@Test
	void TEST_login () {
		Assertions.assertTrue(sut.login("admin", "admin"));
	}

	@Test
	void GIVEN_account_created_WHEN_login_with_correct_password_THEN_login_return_true () {
		AdminMockup admin = new AdminMockup();
		sut.createAdminAccount(admin);
		Assertions.assertTrue(sut.login(admin.getUsername(), AdminMockup.CLEARTEXT_PASSWORD));
	}

	@Test
	void GIVEN_account_created_WHEN_login_with_wrong_password_THEN_login_return_false () {
		AdminMockup admin = new AdminMockup();
		sut.createAdminAccount(admin);
		Assertions.assertFalse(sut.login(admin.getUsername(), "wrong"));
	}

	@Test
	void GIVEN_account_doesnt_exist_WHEN_login_THEN_login_return_false () {
		Assertions.assertFalse(sut.login(new AdminMockup().getUsername(), AdminMockup.CLEARTEXT_PASSWORD));
	}

	@Test
	void TEST_block_user () {
		Assertions.assertTrue(sut.blockUser("whitefish666", true));
	}

	@Test
	void TEST_viewInfluencers () {
		var list = sut.viewInfluencerRanking(
				Date.from(
					LocalDate.of(2021,12,1)
							.atStartOfDay()
							.atZone(ZoneId.systemDefault())
							.toInstant()
				),
				new Date(),
				2);
		Assertions.assertNotNull(list);
		list.forEach(System.out::println);
	}

	@Test
	void TEST_updateInfluencers () {
		Assertions.assertTrue(sut.updateInfluencers(
				List.of("whitefish666", "blacktiger342")
		));
	}


}
