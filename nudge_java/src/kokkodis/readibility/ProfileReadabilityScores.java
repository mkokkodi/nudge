package kokkodis.readibility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import java.util.ArrayList;

public class ProfileReadabilityScores {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * String jsonStr = curl(
		 * "http://dbs04.odesk.com:9000/odr/profile/modified_since/20120701000000.json"
		 * ); ArrayList<Integer> ids = getIds(jsonStr); for (Integer id : ids) {
		 * String profile = curl("http://dbs04.odesk.com:9000/odr/profile/f/" +
		 * id + ".json");
		 */
		String profile = "";
		try {
			BufferedReader input = new BufferedReader(new FileReader(
					"/Users/mkokkodi/git/nudge/nudge_java/temp/test.json"));
			while ((profile = input.readLine()) != null) {
				profile += profile;
			}
		} catch (IOException e) {
		}

		String text = getTextFromProfile(profile);

		if (text != null) {
			System.out.println(text);
			// break;
		}
		// }

	}

	private static String getTextFromProfile(String profile) {
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonObject jo = parser.parse(profile).getAsJsonObject();
		try {
			jo = jo.getAsJsonObject("dev_blurb");
			return jo.getAsString();
		} catch (ClassCastException e) {
			System.out.println("Null bLurb.");
			return null;
		}
	}

	private static ArrayList<Integer> getIds(String jsonStr) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonObject jo = parser.parse(jsonStr).getAsJsonObject();
		JsonArray jel = jo.getAsJsonArray("rid");
		for (JsonElement e : jel) {
			ids.add(gson.fromJson(e, Integer.TYPE));
		}
		return ids;

	}

	public static String curl(String link) {

		Runtime r = Runtime.getRuntime();
		Process p;

		System.out.println("curl " + link);
		try {
			p = r.exec("curl " + link);

			InputStreamReader isr = new InputStreamReader(p.getInputStream());
			BufferedReader br = new BufferedReader(isr);

			String line;
			String result = "";
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				result += line;
			}
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("exitCode:" + p.exitValue());
			return result;

		} catch (IOException e1) {
			e1.printStackTrace();
			return null;
		}

	}

}
