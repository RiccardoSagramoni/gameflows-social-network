package it.unipi.dii.inginf.lsdb.gameflows.user;

import it.unipi.dii.inginf.lsdb.gameflows.admin.AdminServiceFactory;
import it.unipi.dii.inginf.lsdb.gameflows.persistence.MongoConnection;
import it.unipi.dii.inginf.lsdb.gameflows.persistence.Neo4jConnection;
import it.unipi.dii.inginf.lsdb.gameflows.persistence.PersistenceFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.neo4j.driver.*;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.driver.summary.SummaryCounters;

import java.net.MalformedURLException;
import java.util.List;

class UserServiceImplTest {
	private UserServiceImpl sut;

	@BeforeEach
	void init () {
		sut = new UserServiceImpl();
	}

	@Test
	void TEST_login () {
		Assertions.assertTrue(sut.login("whitefish666", "fergie"));
	}

	@Test
	void TEST_insertUser () throws MalformedURLException {
		Assertions.assertNotNull(sut.insertUser(UserMockup.generateUser()));
	}

	@Test
	void WHEN_insert_same_user_twice_THEN_return_false () throws MalformedURLException {
		User user = UserMockup.generateUser();
		sut.insertUser(user);
		Assertions.assertNull(sut.insertUser(user));
	}

	private Neo4jConnection mockNeo4jConnection (SummaryCounters mockCounters) {
		// Mock neo4j connection
		Neo4jConnection mockNeo4jConnection = Mockito.mock(Neo4jConnection.class);
		Session mockSession = Mockito.mock(Session.class);
		ResultSummary mockResultSummary = Mockito.mock(ResultSummary.class);

		Mockito.when(mockNeo4jConnection.verifyConnectivity()).thenReturn(true);
		Mockito.when(mockNeo4jConnection.getSession()).thenReturn(mockSession);
		Mockito.when(mockSession.writeTransaction(Mockito.any())).thenReturn(mockResultSummary);
		Mockito.when(mockResultSummary.counters()).thenReturn(mockCounters);
		Mockito.when(mockCounters.nodesCreated()).thenReturn(1);

		return mockNeo4jConnection;
	}

	@Test
	void GIVEN_user_is_blocked_THEN_isUserBlocked_returns_true () throws Exception {
		try (MongoConnection connection = PersistenceFactory.getMongoConnection()) {
			// Mock neo4j connection
			SummaryCounters mockCounters = Mockito.mock(SummaryCounters.class);
			Mockito.when(mockCounters.nodesCreated()).thenReturn(1);
			Neo4jConnection mockNeo4jConnection = mockNeo4jConnection(mockCounters);

			// GIVEN
			User user = UserMockup.generateUser();
			Assertions.assertNotNull(sut.insertUser(connection, mockNeo4jConnection, user));
			Assertions.assertTrue(AdminServiceFactory.getInstance().blockUser(user.getUsername(), true));

			// THEN
			Assertions.assertTrue(sut.isUserBlocked(user.getUsername()));
		}
	}

	@Test
	void TEST_browse () {
		List<User> list = sut.browse(0, 10);
		Assertions.assertNotNull(list);
		list.forEach((u) -> System.out.println(u.getUsername()));
	}

	@Test
	void TEST_find () {
		String username = "whitefish666";
		User user = sut.find(username);
		Assertions.assertNotNull(user);
		Assertions.assertEquals(user.getUsername(), username);
	}

	@Test
	void TEST_search () {
		String s = "white";
		List<User> list = sut.search(s, 10, 0);
		Assertions.assertNotNull(list);
		list.forEach(System.out::println);
	}
}
