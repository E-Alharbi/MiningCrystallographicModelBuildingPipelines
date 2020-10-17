package MCMBP.Utilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Vector;

public class GeoInfo {

	public String country(String location, String ApplicationIdBack4app, String APIKeyBack4app)
			throws UnsupportedEncodingException, MalformedURLException {
		// TODO Auto-generated method stub

		String city = URLEncoder.encode("{" + "    \"name\": \"" + location + "\"" + "}", "utf-8");
		String Provinces = URLEncoder.encode("{" + "    \"Subdivision_Name\": \"" + location + "\"" + "}", "utf-8");
		URL cityurl = new URL(
				"https://parseapi.back4app.com/classes/Continentscountriescities_City?limit=10&include=country&keys=name,country,country.name,cityId&where="
						+ city);
		URL Provincesurl = new URL(
				"https://parseapi.back4app.com/classes/Continentscountriescities_Subdivisions_States_Provinces?limit=10&include=country&keys=country,country.name&where="
						+ Provinces);

		// try by city name
		String Val = SendRequest(cityurl, ApplicationIdBack4app, APIKeyBack4app);
		if (Val == null)
			Val = SendRequest(Provincesurl, ApplicationIdBack4app, APIKeyBack4app);

		return Val;

	}

	String SendRequest(URL url, String ApplicationIdBack4app, String APIKeyBack4app) {
		try {

			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestProperty("X-Parse-Application-Id", ApplicationIdBack4app); // This is your app's
																								// application id
			urlConnection.setRequestProperty("X-Parse-REST-API-Key", APIKeyBack4app); // This is your app's REST API key

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
				StringBuilder stringBuilder = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					stringBuilder.append(line);
				}

				return new GeoInfo().Getcountry(stringBuilder.toString());
			} finally {
				urlConnection.disconnect();
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.toString());
		}
		return null;
	}

	String Getcountry(String json) {

		Vector<String> JsonRegx = new Vector<String>();
		JsonRegx.add("$..country..name");

		for (int i = 0; i < JsonRegx.size(); i++) {
			List<String> country = com.jayway.jsonpath.JsonPath.parse(json).read(JsonRegx.get(i));
			if (country.size() != 0) {
				// Check if all the same to avoid
				boolean same = true;
				for (String con : country) {

					if (!con.equals(country.get(0)))
						same = false;
				}
				if (same == true)
					return country.get(0).replaceAll(",", "");
			}

		}

		return null;

	}

}
