package it.unipi.dii.inginf.lsdb.gameflows.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DatabaseConfigurationImplTest {
	private DatabaseConfigurationImpl sut;

	@BeforeEach
	public void init() {
		sut = new DatabaseConfigurationImpl();
	}

	@Test
	public void TEST() {
		System.out.println(sut.toString());
	}
}
