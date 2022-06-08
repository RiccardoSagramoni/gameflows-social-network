package it.unipi.dii.inginf.lsdb.gameflows.videogamecommunity;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;

public class VideogameCommunityMockup extends VideogameCommunity {

	public VideogameCommunityMockup () {
		this(null);
	}

	public VideogameCommunityMockup (ObjectId id) {
		super(
				id,
				"Mockup",
				"Summary",
				new ArrayList<>(){{add("1"); add("2");}},
				new ArrayList<>(){{add("3"); add("4");}},
				"cover",
				new ArrayList<>(){{add("5"); add("6");}},
				"collection",
				90.1,
				new Date(),
				new ArrayList<>(){{add("7"); add("8");}},
				new ArrayList<>(){{add("9"); add("10");}}
		);
	}

}
