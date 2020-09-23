package MCMBP.Authors.Mining;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.net.URL;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import MCMBP.Resources.PDBe;
import MCMBP.Resources.crossref;
import MCMBP.Resources.elsevier;
import MCMBP.Utilities.CSVFilter;
import MCMBP.Utilities.Downloder;

import MCMBP.Utilities.TxtFiles;
import me.tongfei.progressbar.ProgressBar;




public class PMCMultiThreaded implements Runnable {

	static String CrossrefEmail="";
	static String ElsevierToken="";
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
		
		
	
	}
	
	
	public static HashMap<String,HashMap<String,String>> PMCAndPDB = new HashMap<String,HashMap<String,String>> ();

	static int PMCidsIndex=0;
	static Vector<String> CSVRecords=  new Vector<String>();
	static HashMap<String,String> Tools= new HashMap<String,String>();
	static Instant start;
	
	static ProgressBar pb ;
	
	static synchronized Map.Entry<String,HashMap<String,String>> PickPMCID() {
		if(!PMCAndPDB.isEmpty()) {
			
			Map.Entry<String,HashMap<String,String>> entry = PMCAndPDB.entrySet().iterator().next();
			PMCAndPDB.remove(entry.getKey());
			
			return entry;
		}
		return null;
	}
	static synchronized HashMap<String,HashMap<String,String>> GetByPubmed(String pubmed) {
		
		HashMap<String,HashMap<String,String>> temp= new HashMap<String,HashMap<String,String>>();
		for(String pdb : PMCAndPDB.keySet()) {
			
			if(PMCAndPDB.get(pdb).get("pubmedId").equals(pubmed)) {
				temp.put(pdb, PMCAndPDB.get(pdb));
				
			}
		}
		return temp;
	}
	

	
	static synchronized void AddCSVRecord(String Record) {
		
		CSVRecords.add(Record);
		
		 
	}
	
	void UpdateLog() {
		if(new File("papers").exists()&& new File("json").exists()) {
			
			
			
			
			pb.step();
			
		}
	}
	public void PrepareDatasets(String FilterBy , String PDBList) throws IOException, ParseException {
		HashMap<String,Vector<String>> FilterByKeywordsAndValues= new HashMap<String,Vector<String>>();
		if(FilterBy.trim().length()!=0) {
		String []FilterByKeywords=FilterBy.split("\\]");
		for(String keyword : FilterByKeywords) {
			String [] val =  keyword.split(":");
			
			FilterByKeywordsAndValues.put(val[0].replaceAll("\\[",""), new CSVFilter().SplitKeywordVal(val[1]));
			
		}
		}
		
		if(!new File("PDBBank.csv").exists())
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
		
		list.remove(0);// remove headers record
		
		
		for(String FilterKeywords : FilterByKeywordsAndValues.keySet()) {
			
			list=new CSVFilter().FilterCSV(FilterByKeywordsAndValues.get(FilterKeywords),HeadersWithIndex,list,FilterKeywords);	
		}
		
		if(PDBList.trim().length()!=0) {
		List<CSVRecord> ToRemove = new ArrayList<CSVRecord>();
		Vector<String> PDB= new TxtFiles().ReadIntoVec(PDBList,true);
		
		for(CSVRecord record : list) {
			if(!PDB.contains(record.get(HeadersWithIndex.get("structureId")).trim()))
				ToRemove.add(record);
				
		}
		
		for(CSVRecord pdb: ToRemove) {
			list.remove(pdb);
		}
		}
		
		
		System.out.println("Number of PDB "+list.size());
		
		for (CSVRecord record : list) {
			
			
			
			if(record.get(HeadersWithIndex.get("pubmedId")).length()!=0) {
				HashMap<String,String> RequiredCol=new HashMap<String,String>();
				RequiredCol.put("pubmedId", record.get(HeadersWithIndex.get("pubmedId")));
				RequiredCol.put("publicationYear", record.get(HeadersWithIndex.get("publicationYear")));
				RequiredCol.put("resolution", record.get(HeadersWithIndex.get("resolution")));
				RequiredCol.put("doi", record.get(HeadersWithIndex.get("doi")));

				PMCAndPDB.put(record.get(HeadersWithIndex.get("structureId")), RequiredCol);
				
				

			}
			
		}
		parser.close(); 
		
		
		
	}
	
	public  void Mining (String FilterBy,String Pipeline, boolean UseExistsPapers, boolean Multithreaded,String crossrefEmail,String elsevierToken, String PDBList , boolean ExtractingInformation) throws FileNotFoundException, IOException, ParseException {
		// TODO Auto-generated method stub
		
		CrossrefEmail=crossrefEmail;
		ElsevierToken=elsevierToken;
		
		System.out.println("FilterBy "+FilterBy);
		
		
		Vector <String>OfficialPipelineNames= new Vector<String>(Arrays.asList(Pipeline.split(","))); 
		for(String Pipe : OfficialPipelineNames) {
			Tools.put(Pipe.split(":")[0], Pipe.split(":")[1]);
		}
		
		
		PrepareDatasets(FilterBy,PDBList);
		System.out.println("Number of PDB after filtering and which have PUB MED ID "+PMCAndPDB.size());
		NLP.LoadNLP();
		start = Instant.now();
		pb = new ProgressBar("Downloading papers", PMCAndPDB.size());
		if(UseExistsPapers==false) {
		//if(new File("json").exists())
		//	FileUtils.deleteDirectory(new File("json"));
		//if(new File("papers").exists())
			//FileUtils.deleteDirectory(new File("papers"));
		
		
		CheckDirAndFile("json");
		CheckDirAndFile("papers");
		 
			
		 if(Multithreaded==false) {
			while (!PMCAndPDB.isEmpty()) {
				
				
				new PMCMultiThreaded().run();
				//UpdateLog();
		}
		 }
			
			if(Multithreaded==true) {
				
			ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			while (!PMCAndPDB.isEmpty()) {
					es.execute(new PMCMultiThreaded());// new thread
			}
			
			es.shutdown();
			
			while(es.isTerminated()==false) ;
			}
			
		}
		
		
		pb.close();
		
		
		if(ExtractingInformation==true) {
	
	
		PrepareDatasets(FilterBy,PDBList);
		//NLP
		File [] json= new File("json").listFiles();
		File [] papers= new File("papers").listFiles();
		NLP.LoadNLP();
		pb = new ProgressBar("Extracting information ", papers.length);
		String CSVWithNoneRepeatedPubid="ID,Resolution,PublicationYear,Tool,PDB,MostCountry,ListOfCountries,FirstAuthor,PublishedInOnePaper,journal,occurance\n";
		String RecordNonRepeatedAuotherInfo="";
		HashMap<String,String> RecordNonRepeatedPubid  = new HashMap<String,String>();
		for(File j: json) {
			File Paper=null;
			for(File x: papers) {
				if(j.getName().replaceAll(FilenameUtils.getExtension(j.getName()), "").equals(x.getName().replaceAll(FilenameUtils.getExtension(x.getName()), ""))) {
					Paper=x;
				}
			}
			String Papertxt=new TxtFiles().readFileAsString(Paper.getAbsolutePath());
			String jsontxt=new TxtFiles().readFileAsString(j.getAbsolutePath());
			
			pb.step();
			for(String tool : Tools.keySet() ) {
				
				
				if(Papertxt.toLowerCase().contains(tool)) {
					int occurance = StringUtils.countMatches(Papertxt.toLowerCase(),tool);
					String Record="";
					
					
					Vector<String> COUNTRIES = new Vector<String>();
					
					
					
					
					List<String> authors=new PMCMultiThreaded().ListofAffiliation(jsontxt);
					String journalname= new PMCMultiThreaded().journal(jsontxt);
							if(authors!=null) {
								for(int a=0 ;a < authors.size(); ++a) {
			
								String val=NLP.TokenTypeCOUNTRY(authors.get(a).toString());
								if(val.trim().length()!=0)// found a COUNTRY
								COUNTRIES.add(val) ;
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
								
								HashMap<String,HashMap<String,String>> pmc=GetByPubmed(j.getName().replaceAll("."+FilenameUtils.getExtension(j.getName()), ""));
								String PublishedInOnePaper="F";
								if(pmc.size()>1)
									PublishedInOnePaper="T";
								
								String PDBs="";
								String Reso="";
								
								for(String PDBID : pmc.keySet()) {
								Record=pmc.get(PDBID).get("pubmedId")+","+pmc.get(PDBID).get("resolution")+","+pmc.get(PDBID).get("publicationYear")+",";
								PDBs+=PDBID+" ";
								Reso+=pmc.get(PDBID).get("resolution")+" ";
								RecordNonRepeatedAuotherInfo=pmc.get(PDBID).get("pubmedId")+","+Reso+","+pmc.get(PDBID).get("publicationYear")+",";
								
								
								if(maxEntry != null) {
									String ListOfCOUNTRIES="";
									for(String c : COUNTRIESfrequente.keySet())
										
										ListOfCOUNTRIES+=c+" ";
										
									 Record+=Tools.get(tool)+","+PDBID+","+maxEntry.getKey()+","+ListOfCOUNTRIES+","+COUNTRIES.get(0)+","+PublishedInOnePaper+","+journalname+","+occurance+"\n";
									 RecordNonRepeatedAuotherInfo+=Tools.get(tool)+","+PDBs+","+maxEntry.getKey()+","+ListOfCOUNTRIES+","+COUNTRIES.get(0)+","+PublishedInOnePaper+","+journalname+","+occurance+"\n";
								}
									
								else {
									Record+=Tools.get(tool)+","+PDBID+","+"-1,-1,-1,"+PublishedInOnePaper+","+journalname+"\n";
									RecordNonRepeatedAuotherInfo+=Tools.get(tool)+","+PDBID+","+"-1,-1,-1,"+PublishedInOnePaper+","+journalname+","+occurance+"\n";
								}
								
								AddCSVRecord(Record);
								}
								 CSVWithNoneRepeatedPubid+=RecordNonRepeatedAuotherInfo;
								 
								 if(RecordNonRepeatedPubid.containsKey(j.getName().replaceAll("."+FilenameUtils.getExtension(j.getName()), ""))) { // if contained then, meaning two pipeline have used in this paper.
									 RecordNonRepeatedPubid.remove(j.getName().replaceAll("."+FilenameUtils.getExtension(j.getName()), ""));
								 }
								 else {
									 RecordNonRepeatedPubid.put(j.getName().replaceAll("."+FilenameUtils.getExtension(j.getName()),"") , RecordNonRepeatedAuotherInfo);
								 }
								
				}
			}
		}
		pb.close();
		
		
		
		
		String CSV="ID,Resolution,PublicationYear,Tool,PDB,MostCountry,ListOfCountries,FirstAuthor,PublishedInOnePaper,journal,occurance\n";
		for(String record : CSVRecords)
			CSV+=record;
		
		String RecordNonRepeatedPubidCSV="ID,Resolution,PublicationYear,Tool,PDB,MostCountry,ListOfCountries,FirstAuthor,PublishedInOnePaper,journal,occurance\n";
		for(String record : RecordNonRepeatedPubid.keySet())
			RecordNonRepeatedPubidCSV+=RecordNonRepeatedPubid.get(record);
		
		new TxtFiles().WriteStringToTxtFile("AuthorsInformation.csv", CSV);
		new TxtFiles().WriteStringToTxtFile("NonDuplicatedPipelineAuthorsInformation.csv", CSVWithNoneRepeatedPubid);
		new TxtFiles().WriteStringToTxtFile("NonDuplicatedPubid.csv", RecordNonRepeatedPubidCSV);
	}
	}

	public void LoadDataFromPDB() throws IOException {
		URL url = new URL("http://www.rcsb.org/pdb/rest/customReport.csv?pdbids=*&customReportColumns=structureId,pmc,pubmedId,structureTitle,experimentalTechnique,publicationYear,resolution,doi&primaryOnly=1&service=wsfile&format=csv");
		FileUtils.copyURLToFile(url, new File("PDBBank.csv"));
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		Map.Entry<String,HashMap<String,String>> pmc = PickPMCID();
if(pmc==null) {
			
			
			return;
		}
		
		HashMap<String, HashMap<String, String>> pmcForPDBe= new HashMap<String, HashMap<String, String>>();
		
		pmcForPDBe.put(pmc.getKey(), pmc.getValue());
		
		if(pmc.getValue().get("doi").trim().length()==0)
			try {
				pmcForPDBe=new PDBe().UpdateDOI(pmcForPDBe, pmc.getKey() );
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (ParseException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		pmc = pmcForPDBe.entrySet().iterator().next();
		
		System.out.println("PDB "+pmc+" thread "+Thread.currentThread().getId());
		
		UpdateLog();
		
		
		
		
		
		
		String url="https://www.ebi.ac.uk/europepmc/webservices/rest/"+pmc.getValue().get("pubmedId")+"/fullTextXML";
		String Txt=null;
		try {
			Txt = new Downloder().GetHttpRequste(url);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("PDB europepmc "+pmc);
		if(Txt.trim().length()==0) {
			System.out.println("Trying elsevier "+pmc);
			try {
				Txt=	new elsevier().Get(ElsevierToken, pmc.getValue().get("doi"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		System.out.println("PDB Elsevier "+pmc);
		if(Txt.trim().length()==0) { // try crossref
			System.out.println("Trying crossref "+pmc);
			try {
				Txt=	new crossref().Get(CrossrefEmail, pmc.getValue().get("doi"), pmc.getValue().get("structureId"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("PDB crossref "+pmc);
		
		
		if(Txt.length()!=0) {
			System.out.println("Found "+pmc);
			url="https://www.ebi.ac.uk/europepmc/webservices/rest/search?query="+pmc.getValue().get("pubmedId")+"&resultType=core&format=json";
			
			try {
				String json = new Downloder().GetHttpRequste(url);
				if(json.length()!=0) {
					
					new TxtFiles().WriteStringToTxtFile("json/"+pmc.getValue().get("pubmedId")+".json", json);
					new TxtFiles().WriteStringToTxtFile("papers/"+pmc.getValue().get("pubmedId")+".txt", Txt);
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		System.out.println("PDB json "+pmc);
		
	}
	
	List<String> ListofAffiliation(String json){
		
		Vector<String> JsonRegx=new Vector<String>();
		JsonRegx.add("$..authorAffiliationsList.*.*");
		JsonRegx.add("$..affiliation");
		for(int i=0 ; i < JsonRegx.size();++i) {
			List<String> authors = com.jayway.jsonpath.JsonPath.parse(json).read(JsonRegx.get(i));
			if(authors.size()!=0)
				return authors;
		}
		System.out.println("Error author Affiliations not found "+ Thread.currentThread().getId());
		return null;
		
		
			
		
		
	}
String journal(String json){
		
		Vector<String> JsonRegx=new Vector<String>();
		JsonRegx.add("$..journal.title");
		
		for(int i=0 ; i < JsonRegx.size();++i) {
			List<String> journal = com.jayway.jsonpath.JsonPath.parse(json).read(JsonRegx.get(i));
			if(journal.size()!=0)
				return journal.get(0).replaceAll(",", " ");
		}
		System.out.println("Error journal name not found "+ Thread.currentThread().getId());
		return null;
		
		
			
		
		
	}
	static public boolean CheckDirAndFile(String Path) {

		try {
			File directory = new File(Path);
			if (!directory.exists()) {
				directory.mkdir();
			}
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.print( "Error: Unable to create " );

			return false;

		}

	}
}
