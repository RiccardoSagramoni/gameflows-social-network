package it.unipi.dii.inginf.lsdb.gameflows.comment;

import org.bson.types.ObjectId;

public class InfoPostMockup extends InfoPost {
	public InfoPostMockup() {
		super(
			new ObjectId("61dbfbd392a81a85adeaf095"),
			"tinywolf599",
			new ObjectId("61dbfbbe92a81a85adeaf04c"),
			"Blasphemous"
		);
	}
}
