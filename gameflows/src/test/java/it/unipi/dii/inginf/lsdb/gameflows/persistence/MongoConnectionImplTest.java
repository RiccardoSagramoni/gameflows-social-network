package it.unipi.dii.inginf.lsdb.gameflows.persistence;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MongoConnectionImplTest {
	private MongoConnectionImpl sut;

	@BeforeEach
	public void init() {
		sut = new MongoConnectionImpl(new DatabaseConfigurationMockup());
	}

	@Test
	public void TEST_getCollection () {
		Assertions.assertNotNull(sut.getCollection(GameflowsCollection.videogame));
		Assertions.assertNotNull(sut.getCollection(GameflowsCollection.post));
		Assertions.assertNotNull(sut.getCollection(GameflowsCollection.comment));
		Assertions.assertNotNull(sut.getCollection(GameflowsCollection.user));
		Assertions.assertNotNull(sut.getCollection(GameflowsCollection.admin));
	}

	@Test
	void TEST_verifyConnectivity () {
		Assertions.assertTrue(sut.verifyConnectivity());
	}
}
