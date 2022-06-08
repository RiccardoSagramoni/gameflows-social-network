import com.api.igdb.apicalypse.*;
import com.api.igdb.exceptions.*;
import com.api.igdb.request.*;
import com.api.igdb.utils.*;
import org.json.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class VideogameExtractor {

	private final String CLIENT_ID;
	private final String CLIENT_SECRET;
	private final String ACCESS_TOKEN;

	private static final int QUERY_LIMIT = 500;

	// AUTHENTICATE ON TWITCH
	private final TwitchAuthenticator twitchAuth = TwitchAuthenticator.INSTANCE;
	private final IGDBWrapper wrapper = IGDBWrapper.INSTANCE;

	public VideogameExtractor () {
		this(   "igl1i1bn2h18gtj61ssj2li9edmd37",
				"zhu4d7t6jof54mrk3nrjjnedh1695r",
				"wzajzic6mii5jctus2i952s8257lck"
			);
	}

	public VideogameExtractor (String clientId, String clientSecret, String accessToken) {
		CLIENT_ID = clientId;
		CLIENT_SECRET = clientSecret;
		ACCESS_TOKEN = accessToken;
	}

	private TwitchToken getTwitchToken () {
		// The instance stores the token in the object until a new one is requested
		TwitchToken token = twitchAuth.getTwitchToken();

		if (token == null) {
			token = twitchAuth.requestTwitchToken(CLIENT_ID, CLIENT_SECRET);
		}

		System.out.println("Token: " + token.toString());

		return token;
	}

	private void igdbAuthenticate () {

		wrapper.setCredentials(CLIENT_ID, ACCESS_TOKEN);
	}

	private void igdbAuthenticate (TwitchToken token) {

		wrapper.setCredentials(CLIENT_ID, token.getAccess_token());
	}

	private APICalypse getVideogameQuery (int offset, int limit) {
		return new APICalypse()
				.fields("name, " +
						"age_ratings.category, age_ratings.rating," +
						"aggregated_rating," +
						"collection.name," +
						"cover.url," +
						"first_release_date," +
						"game_modes.name," +
						"genres.name," +
						"involved_companies.company.name, involved_companies.developer, " +
						"involved_companies.publisher," +
						"platforms.name," +
						"status," +
						"summary," +
						"websites.category, websites.url")
				.where("websites.category = 14 & " +
						"(age_ratings.category = null | age_ratings.category = 1) &" +
						"(involved_companies.developer = true | involved_companies.publisher = true)")

				.sort("id", Sort.ASCENDING)
				.offset(offset)
				.limit(limit);
	}

	private JSONArray getDataset () {
		JSONArray jsonArray = new JSONArray();

		int i = 0;
		while (i < 6) {
			try {
				jsonArray.putAll(
						new JSONArray(
								wrapper.apiJsonRequest(
										Endpoints.GAMES,
										getVideogameQuery(i * QUERY_LIMIT, QUERY_LIMIT).buildQuery()
								)
						)
				);

			} catch (RequestException ex) {
				break;
			}

			// Sleep for 0.25 seconds (due to the limit of 4 queries per second)
			try {
				System.out.print(".");
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			i++;
		}

		return jsonArray;
	}

	public void start () {
		igdbAuthenticate();
		JSONArray jsonArray;

		jsonArray = getDataset();

		//System.out.println(jsonArray.toString(2));

		try {
			Files.write(Paths.get("games.json"), jsonArray.toString(2).getBytes());

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
