import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

public class UrlExtractor {
	public static void main (String[] args) {
		// Read from file
		JSONArray jsonArray;
		try {
			jsonArray = new JSONArray(Files.readString(Paths.get("games.json")));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		Set<String> set = new HashSet<>();

		// Extract reddit url from json
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject json = jsonArray.getJSONObject(i);
			JSONArray websitesJson = json.getJSONArray("websites");

			for (int jj = 0; jj < websitesJson.length(); jj++) {
				JSONObject elementWebsiteJson = websitesJson.getJSONObject(jj);
				if (elementWebsiteJson.getInt("category") == 14) {
					set.add(elementWebsiteJson.getString("url"));
				}
			}
		}

		// Build the string
		StringBuilder builder = new StringBuilder();
		for (String s: set) {
			builder.append(s).append("\n");
		}

		// Write on file
		try {
			Files.write(Paths.get("reddit.txt"), builder.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
