package MCMBP.Utilities;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;



/*
 * Reading CSV into HashMap
 */
public class CSVReader {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String PDBBank= new TxtFiles().readFileAsString("PDBBank.csv");
	System.out.println(new CSVReader().ReadIntoHashMap(PDBBank, "structureId").size());
	}

	
	

	public HashMap<String, HashMap<String, String>> ReadIntoHashMap(String CSV, String IDHeader)
			throws IOException {
		Reader in;
		
		in=new StringReader(CSV);
		CSVParser csvParser = CSVFormat.DEFAULT.withHeader().parse(in);
		List<String> headers = csvParser.getHeaderNames();
		
		in=new StringReader(CSV);
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(in);
		
		
	
		//System.out.println(headers);
		HashMap<String,HashMap<String, String>> CSVINHashmap = new HashMap<String,HashMap<String, String>>();
	
			for (CSVRecord record : records) {
				HashMap<String, String> temp = new HashMap<String, String>();
				String ID="";
				
			for(String h : headers) {
				
				
				if(h.equals(IDHeader)) {
					CSVINHashmap.put(record.get(IDHeader), null);
					ID=record.get(IDHeader);
					
				}
				else {
					temp.put(h, record.get(h));
				}
			//System.out.println(record.get("structureId"));
			
		}
			CSVINHashmap.put(ID, temp);
		}
			//System.out.println(CSVINHashmap);
		return CSVINHashmap;
	}

	
}
