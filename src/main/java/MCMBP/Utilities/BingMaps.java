package MCMBP.Utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BingMaps {

	public Set<String> OrganizationLocation(String Organization, String apikey) throws IOException {

		String url = "http://dev.virtualearth.net/REST/v1/Locations/" + Organization.replaceAll(" ", "%20")
				+ "?o=json&&key=" + apikey;

		Set<String> CountiesList = new LinkedHashSet<String>();
		List<String> country = new ArrayList<String>();
		String Reply = new Downloder().GetHttpRequste(url);
		if (Reply.trim().length() == 0)
			return CountiesList;
		country = com.jayway.jsonpath.JsonPath.parse(Reply).read("$..countryRegion");

		List<String> confidence = new ArrayList<String>();
		confidence = com.jayway.jsonpath.JsonPath.parse(Reply).read("$..confidence");

		for (int con = 0; con < country.size(); ++con) {
			if (confidence.get(con).equals("High") || confidence.get(con).equals("Medium")) {
				CountiesList.add(country.get(con));
			}
		}

		return CountiesList;
	}
}
