package it.unipi.dii.inginf.lsdb.gameflows.persistence;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Session;

class Neo4jConnectionImplTest {
	Neo4jConnection neo4j;

	@BeforeEach
	void init () {
		neo4j = new Neo4jConnectionImpl(new DatabaseConfigurationMockup());
	}

	@Test
	void WHEN_Neo4jConnection_is_created_THEN_application_can_connect_to_neo4j () {
		Assertions.assertTrue(neo4j.verifyConnectivity());
	}

	@Test
	void WHEN_getSession_called_THEN_session_is_open () {
		try (Session session = neo4j.getSession()) {
			Assertions.assertTrue(session.isOpen());
		}
	}
	
}
