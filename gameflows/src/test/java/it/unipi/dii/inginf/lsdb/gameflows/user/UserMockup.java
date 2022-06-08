package it.unipi.dii.inginf.lsdb.gameflows.user;

import org.bson.Document;
import org.jetbrains.annotations.NotNull;

public class UserMockup {

	private UserMockup () {}

	public static @NotNull User generateUser (String username) {
		if (username == null) {
			username = "mockup" + System.currentTimeMillis();
		}
		return User.fromDocument(
				Document.parse("{\"username\":\"" + username +"\",\"email\":\"maurijn.wijdeven@example.com\"," +
						"\"password\":{\"sha256\":\"e0c1a8d336d7447960f78655d91adf7d13075c7be3947b4c9df1cc0784e6805a\",\"salt\":\"XFYVyr8i\"},\"name\":\"Maurijn\",\"surname\":\"Wijdeven\",\"gender\":\"male\",\"date-of-birth\":{\"$date\":\"1954-09-18T00:00:00.000Z\"},\"nationality\":\"NL\",\"picture\":{\"large\":\"https://randomuser.me/api/portraits/men/15.jpg\",\"medium\":\"https://randomuser.me/api/portraits/med/men/15.jpg\",\"thumbnail\":\"https://randomuser.me/api/portraits/thumb/men/15.jpg\"}}")
		);
	}

	public static @NotNull User generateUser () {
		return generateUser(null);
	}

}
