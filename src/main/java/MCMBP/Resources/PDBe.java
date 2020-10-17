package MCMBP.Resources;

import java.io.IOException;
import java.util.HashMap;

import org.json.simple.parser.ParseException;

import MCMBP.Utilities.Downloder;
import MCMBP.Utilities.JSONReader;

public class PDBe {

	public HashMap<String, HashMap<String, String>> UpdateDOI(HashMap<String, HashMap<String, String>> pdbbank,
			String PDB) throws IOException, ParseException {

		String pdbe = new Downloder().GetHttpRequste("https://www.ebi.ac.uk/pdbe/api/pdb/entry/publications/" + PDB);
		if (pdbe.trim().length() != 0) {
			HashMap<String, String> PDBe = new JSONReader().JSONToHashMap(pdbe);

			String pubmed_id = pdbbank.get(PDB).get("pubmedId");
			if (pdbbank.get(PDB).get("doi").trim().length() == 0)
				pdbbank.get(PDB).put("doi", PDBe.get(pubmed_id));

		}
		return pdbbank;
	}
}
