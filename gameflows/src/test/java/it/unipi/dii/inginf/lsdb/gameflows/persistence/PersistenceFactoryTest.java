package it.unipi.dii.inginf.lsdb.gameflows.persistence;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PersistenceFactoryTest {
	@Test
	public void WHEN_getDatabaseConfiguration_invoked_twice_THEN_same_instance_returned () {
		Assertions.assertEquals(
				PersistenceFactory.getDatabaseConfiguration(),
				PersistenceFactory.getDatabaseConfiguration()
		);
	}
}
