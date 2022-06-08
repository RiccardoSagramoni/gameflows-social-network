package it.unipi.dii.inginf.lsdb.gameflows.post;

import org.bson.types.ObjectId;

import java.util.List;

public class InfoVideogameCommunityMockup extends InfoVideogameCommunity {

	public InfoVideogameCommunityMockup () {
		super(new ObjectId("61dbfbbe92a81a85adeaf00f"),
				"Fallout",
				List.of("Role-playing (RPG)")
		);
	}
}
