package MCMBP.Utilities;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.json.simple.JSONObject;
/*
 * Reading JSON into HashMap
 */
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JSONReader {

	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
		// TODO Auto-generated method stub

		new JSONReader().JSONToHashMap("");

	}

	public HashMap<String, String> JSONToHashMap(String JSONPath)
			throws FileNotFoundException, IOException, ParseException {
		
		Vector<String> JsonRegx=new Vector<String>();
		JsonRegx.add("$..pubmed_id");
		JsonRegx.add("$..doi");
		HashMap<String, String> pubmedidAnddoi=new HashMap<String, String>();
		
		List<String> pubmed_id = com.jayway.jsonpath.JsonPath.parse(JSONPath).read(JsonRegx.get(0));
		List<String> doi = com.jayway.jsonpath.JsonPath.parse(JSONPath).read(JsonRegx.get(1));

		for(int i=0 ; i < pubmed_id.size();++i) {
			pubmedidAnddoi.put(pubmed_id.get(i), doi.get(i));
		}
		
		
		return pubmedidAnddoi;
		
	}
}
