package MCMBP.Pipelines.Mining;

import java.io.IOException;
import java.util.HashMap;

import org.json.simple.parser.ParseException;

import MCMBP.Utilities.CSVReader;
import MCMBP.Utilities.Downloder;
import MCMBP.Utilities.JSONReader;

public class PDBe {

	public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		
		

	}

	
	public HashMap<String, HashMap<String, String>> UpdateDOI(HashMap<String, HashMap<String, String>> pdbbank, String PDB) throws IOException, ParseException {
		String pdbe=new Downloder().GetHttpRequste("https://www.ebi.ac.uk/pdbe/api/pdb/entry/publications/"+PDB);
		HashMap<String, String> PDBe= new JSONReader().JSONToHashMap(pdbe);
		
		for(String pdb : pdbbank.keySet()) {
			String pubmed_id=pdbbank.get(pdb).get("pubmedId");
			if(pdbbank.get(pdb).get("doi").trim().length()==0)
			pdbbank.get(pdb).put("doi", PDBe.get(pubmed_id));
		}
		
		return pdbbank;
	}
}
