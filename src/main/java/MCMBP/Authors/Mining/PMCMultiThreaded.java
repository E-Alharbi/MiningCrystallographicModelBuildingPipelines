package MCMBP.Authors.Mining;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import MCMBP.Utilities.CSVFilter;
import MCMBP.Utilities.Downloder;
import MCMBP.Utilities.TxtFiles;



public class PMCMultiThreaded implements Runnable {

	
	public static HashMap<String,HashMap<String,String>> PMCAndPDB = new HashMap<String,HashMap<String,String>> ();
	static int PMCidsIndex=0;
	static Vector<String> CSVRecords=  new Vector<String>();
	static HashMap<String,String> Tools= new HashMap<String,String>();
	static synchronized Map.Entry<String,HashMap<String,String>> PickPMCID() {
		if(!PMCAndPDB.isEmpty()) {
			Map.Entry<String,HashMap<String,String>> entry = PMCAndPDB.entrySet().iterator().next();
			PMCAndPDB.remove(entry.getKey());
			System.out.println("Remaining papers "+PMCAndPDB.size());
			return entry;
		}
		return null;
	}
	static synchronized void AddCSVRecord(String Record) {
		CSVRecords.add(Record);
		System.out.println("Number of CSVRecords "+CSVRecords.size());
	}
	
	public void PrepareDatasets(String FilterBy) throws IOException {
		HashMap<String,Vector<String>> FilterByKeywordsAndValues= new HashMap<String,Vector<String>>();
		if(FilterBy.trim().length()!=0) {
		String []FilterByKeywords=FilterBy.split("\\]");
		for(String keyword : FilterByKeywords) {
			String [] val =  keyword.split(":");
			
			FilterByKeywordsAndValues.put(val[0].replaceAll("\\[",""), new CSVFilter().SplitKeywordVal(val[1]));
			
		}
		}
		System.out.println("FilterBy "+FilterBy);
		new PMCMultiThreaded().LoadDataFromPDB();
		CSVParser parser = new CSVParser(new FileReader("PDBBank.csv"), CSVFormat.DEFAULT);
		List<CSVRecord> list = parser.getRecords();
		
		String [] Headers=list.get(0).toString().split("values=\\[")[1].replaceAll("\\]\\]", "").split(",");
		HashMap<String,Integer> HeadersWithIndex= new HashMap<String,Integer>();
		
		int index=0;
		for(String H : Headers) {
			HeadersWithIndex.put(H.trim(), index);
			index++;
		}
		
		
		
		for(String FilterKeywords : FilterByKeywordsAndValues.keySet()) {
			
			list=new CSVFilter().FilterCSV(FilterByKeywordsAndValues.get(FilterKeywords),HeadersWithIndex,list,FilterKeywords);	
		}
		
		
		System.out.println("Number of PDB after filtering "+list.size());
		
		
		for (CSVRecord record : list) {
			if(record.get(HeadersWithIndex.get("pubmedId")).length()!=0) {
				HashMap<String,String> RequiredCol=new HashMap<String,String>();
				RequiredCol.put("pubmedId", record.get(HeadersWithIndex.get("pubmedId")));
				RequiredCol.put("publicationYear", record.get(HeadersWithIndex.get("publicationYear")));
				RequiredCol.put("resolution", record.get(HeadersWithIndex.get("resolution")));

				
				PMCAndPDB.put(record.get(HeadersWithIndex.get("structureId")), RequiredCol);
			}
		}
		
		System.out.println("Number of PDB who they have PUB MED ID"+PMCAndPDB.size());
		
		parser.close(); 
		
	}
	
	public  void Mining (String FilterBy,String Pipeline) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		
		
		
		
		//String FilterBy="[experimentalTechnique:X-RAY DIFFRACTION,SOLUTION NMR][publicationYear:2019-2020][resolution:2.0]";
		//String Pipeline="arp/warp,buccaneer,shelxe,phenix.autobuild,phenix autobuild";
	
		
		Vector <String>OfficialPipelineNames= new Vector<String>(Arrays.asList(Pipeline.split(","))); 
		for(String Pipe : OfficialPipelineNames) {
			Tools.put(Pipe.split(":")[0], Pipe.split(":")[1]);
		}
		System.out.println(Tools);
		System.out.println(Pipeline+": "+Tools);
		
		PrepareDatasets(FilterBy);
	
		ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1);
		while (!PMCAndPDB.isEmpty()) {
				es.execute(new PMCMultiThreaded());// new thread
		}
		
		es.shutdown();
		
		while(es.isTerminated()==false) ;
		
		
		String CSV="ID,Reso,PubDate,Tool,PDB,MostCountry,ListOfCountries,FirstAuthor\n";
		for(String record : CSVRecords)
			CSV+=record;
		new TxtFiles().WriteStringToTxtFile("Papers.csv", CSV);
		System.out.println(CSVRecords.size());
	}

	public void LoadDataFromPDB() throws IOException {
		URL url = new URL("http://www.rcsb.org/pdb/rest/customReport.csv?pdbids=*&customReportColumns=structureId,pmc,pubmedId,structureTitle,experimentalTechnique,publicationYear,resolution&service=wsfile&format=csv");
		FileUtils.copyURLToFile(url, new File("PDBBank.csv"));
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Map.Entry<String,HashMap<String,String>> pmc = PickPMCID();
		if(pmc==null)
			return;
		System.out.println(pmc.getKey());
		
		
		
		
		//Tools.add("arp/warp");
		//Tools.add("buccaneer");
		//Tools.add("shelxe");
		//Tools.add("phenix.autobuild");
		//Tools.add("phenix autobuild");
		
		String url="https://www.ebi.ac.uk/europepmc/webservices/rest/"+pmc.getValue().get("pubmedId")+"/fullTextXML";
		String Txt=null;
		try {
			Txt = new Downloder().GetHttpRequste(url);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if(Txt.length()==0)
			return;
		for(String tool : Tools.keySet() ) {
			if(Txt.toLowerCase().contains(tool)) {
				
				String Record="";
				
				 Record=pmc.getValue().get("pubmedId")+","+pmc.getValue().get("resolution")+","+pmc.getValue().get("publicationYear")+",";
				
				url="https://www.ebi.ac.uk/europepmc/webservices/rest/search?query="+pmc.getValue().get("pubmedId")+"&resultType=core&format=json";
				System.out.println(url);
				JSONObject obj=null;
				try {
					obj = new JSONObject(new Downloder().GetHttpRequste(url));
				} catch (JSONException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				
				
				JSONArray authors = obj.getJSONObject("resultList").getJSONArray("result").getJSONObject(0).getJSONObject("authorList").getJSONArray("author");
				
				Vector<String> COUNTRIES = new Vector<String>();
				
				for (int i = 0; i < authors.length(); i++) {
					
					if(obj.has("resultList")) {
						
						JSONObject authorAffiliationsList = obj.getJSONObject("resultList").getJSONArray("result").getJSONObject(0).getJSONObject("authorList").getJSONArray("author").getJSONObject(i).getJSONObject("authorAffiliationsList");
for(int a=0 ;a < authorAffiliationsList.length(); ++a) {
	
						String val=new NLP().TokenTypeCOUNTRY(authorAffiliationsList.get("authorAffiliation").toString());
						if(val.trim().length()!=0)// found a COUNTRY
						COUNTRIES.add(val) ;
}
					
				}
					
					
				}
				
				
				LinkedHashMap<String, Integer> COUNTRIESfrequente = new LinkedHashMap<String, Integer>();
				for(String country : COUNTRIES) {
					if(COUNTRIESfrequente.containsKey(country))
						COUNTRIESfrequente.put(country, COUNTRIESfrequente.get(country)+1);
					else
						COUNTRIESfrequente.put(country, 1);
						
				}
				Map.Entry<String, Integer> maxEntry = null;

				for (Map.Entry<String, Integer> entry : COUNTRIESfrequente.entrySet())
				{
				    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
				    {
				        maxEntry = entry;
				    }
				}
				
				if(maxEntry != null) {
					String ListOfCOUNTRIES="";
					for(String c : COUNTRIESfrequente.keySet())
						
						ListOfCOUNTRIES+=c+" ";
						
					 Record+=Tools.get(tool)+","+pmc.getKey()+","+maxEntry.getKey()+","+ListOfCOUNTRIES+","+COUNTRIES.get(0)+"\n";
				}
					
				else
					Record+=Tools.get(tool)+","+pmc.getKey()+","+"-1\n";
				
			
				
				AddCSVRecord(Record);

			}
		}
	     
	}
	
	
	 
}
