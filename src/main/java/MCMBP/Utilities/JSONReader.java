package MCMBP.Utilities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.json.simple.parser.ParseException;

public class JSONReader {

	public HashMap<String, String> JSONToHashMap(String JSONPath)
			throws FileNotFoundException, IOException, ParseException {

		Vector<String> JsonRegx = new Vector<String>();
		JsonRegx.add("$..pubmed_id");
		JsonRegx.add("$..doi");
		HashMap<String, String> pubmedidAnddoi = new HashMap<String, String>();

		List<String> pubmed_id = com.jayway.jsonpath.JsonPath.parse(JSONPath).read(JsonRegx.get(0));
		List<String> doi = com.jayway.jsonpath.JsonPath.parse(JSONPath).read(JsonRegx.get(1));

		for (int i = 0; i < pubmed_id.size(); ++i) {
			pubmedidAnddoi.put(pubmed_id.get(i), doi.get(i));
		}

		return pubmedidAnddoi;

	}
}
