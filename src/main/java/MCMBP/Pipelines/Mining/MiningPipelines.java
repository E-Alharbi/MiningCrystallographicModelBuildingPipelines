package MCMBP.Pipelines.Mining;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.simple.parser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import MCMBP.Authors.Mining.PMCMultiThreaded;
import MCMBP.Resources.PDBe;
import MCMBP.Resources.crossref;
import MCMBP.Resources.elsevier;
import MCMBP.Utilities.CSVReader;
import MCMBP.Utilities.Downloder;
import MCMBP.Utilities.TxtFiles;
import me.tongfei.progressbar.ProgressBar;


public class MiningPipelines implements Runnable{

	static Stack<String>  PDBIDs= new Stack<String>();
	static String CrossrefEmail="";
	static String ElsevierToken="";
	
	static String PaperNotFound="PDB,DOI\n";
	static String CSV="PDB,Pipeline,PaperLink\n";
	static String PaperFoundButNotUsePipeline="PDB,DOI\n";
	static HashMap<String,String> Tools= new HashMap<String,String>();
	static ProgressBar pb;
	static synchronized void AddToPaperNotFound(String Val) {
		PaperNotFound+=Val;
	}
	static synchronized void AddToCSV(String Val) {
		CSV+=Val;
	}
	static synchronized void AddToPaperFoundButNotUsePipeline(String Val) {
		PaperFoundButNotUsePipeline+=Val;
	}
	static synchronized String PickPDB() {
		if(!PDBIDs.isEmpty()) {
			//System.out.println("Remaining PDB "+PDBIDs.size());
			return PDBIDs.pop();
		}
		return null;
	}
	public  void Mining(String Pipeline, String PDBList, String CrossrefEmail1, String ElsevierToken1, String FilterBy, boolean Multithreaded) throws IOException, ParseException {
		// TODO Auto-generated method stub
		CrossrefEmail=CrossrefEmail1;
		ElsevierToken=ElsevierToken1;
		System.out.println(Pipeline);
		System.out.println(CrossrefEmail1);
		Vector<String> PDB= new Vector<String>();
		if(PDBList.trim().length()!=0) {
			 PDB= new TxtFiles().ReadIntoVec(PDBList,true);
				System.out.println("FilterBy will not be used because PDBList is used");

		}
		else {
			PMCMultiThreaded pmc= 	new PMCMultiThreaded();
			//pmc.PrepareDatasets(FilterBy);
			for(String pdb : pmc.PMCAndPDB.keySet()) {
				PDB.add(pdb);
			}
		}
		
		
		Vector <String>OfficialPipelineNames= new Vector<String>(Arrays.asList(Pipeline.split(","))); 
		for(String Pipe : OfficialPipelineNames) {
			Tools.put(Pipe.split(":")[0], Pipe.split(":")[1]);
		}
		System.out.println(Tools);
		
		
		
		
		
		PDBIDs.addAll(PDB);
		
		 pb = new ProgressBar("Downloading papers", PDBIDs.size());
		
		if(Multithreaded==true) {
		ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1);
		while (!PDBIDs.isEmpty()) {
				es.execute(new MiningPipelines());// new thread
		}
		
		es.shutdown();
		
		while(es.isTerminated()==false) ;
		}
		
		if(Multithreaded==false) {
		while (!PDBIDs.isEmpty()) {
			new MiningPipelines().run();
	}
		}
		pb.close();
		new TxtFiles().WriteStringToTxtFile("FoundPapers.csv", CSV);
		new TxtFiles().WriteStringToTxtFile("PapersNOTFound.csv", PaperNotFound);
		new TxtFiles().WriteStringToTxtFile("PapersFoundButNotUsePipeline.csv", PaperFoundButNotUsePipeline);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		
		pb.step();
String PDBIDAsFRomTheExcel=	PickPDB();
if(PDBIDAsFRomTheExcel==null)
	return;
PDBIDAsFRomTheExcel=PDBIDAsFRomTheExcel.substring(0, 4);
String PDBBankRes=null;
HashMap<String, HashMap<String, String>> pdbbank=null;
try {
	PDBBankRes = new Downloder().GetHttpRequste("https://www.rcsb.org/pdb/rest/customReport.csv?pdbids="+PDBIDAsFRomTheExcel+"&customReportColumns=pubmedId,doi&primaryOnly=1&service=wsfile&format=csv");
	 pdbbank= new CSVReader().ReadIntoHashMap(PDBBankRes, "structureId");
	 pdbbank=new PDBe().UpdateDOI(pdbbank, PDBIDAsFRomTheExcel);// sometimes, PDB bank does not provide a DOI so we obtain the DOI from PDBe 
} catch (IOException | ParseException e1) {
	// TODO Auto-generated catch block
	//e1.printStackTrace();
}
//System.out.println(new CSVReader().ReadIntoHashMap(PDBBankRes, "structureId"));


String pubmedId="";
try {
	if(pdbbank.size()>0) { // in case no data found in PDBBank
	pubmedId=pdbbank.get(PDBIDAsFRomTheExcel).get("pubmedId");
	String PMC=  new Downloder().GetHttpRequste("https://www.ebi.ac.uk/europepmc/webservices/rest/"+pubmedId+"/fullTextXML");
	if(PMC.trim().length()==0) {
		PMC=new elsevier().Get(ElsevierToken, pdbbank.get(PDBIDAsFRomTheExcel).get("doi"));
		
		if(PMC.trim().length()==0) {
		
		PMC=new crossref().Get(CrossrefEmail, pdbbank.get(PDBIDAsFRomTheExcel).get("doi"),pdbbank.get(PDBIDAsFRomTheExcel).get("structureId"));
	}
	}

	
	if(PMC.length()==0) {
		AddToPaperNotFound(PDBIDAsFRomTheExcel+","+pdbbank.get(PDBIDAsFRomTheExcel).get("doi")+"\n");
		
	}
	
	
	boolean UsePipeline=false;
	if(PMC.trim().length()!=0)
	for(String tool : Tools.keySet() ) {
		if(PMC.toLowerCase().contains(tool)) {
			
			AddToCSV(PDBIDAsFRomTheExcel+","+Tools.get(tool)+","+pdbbank.get(PDBIDAsFRomTheExcel).get("doi")+"\n");
			
			UsePipeline=true;
		}
		}
	if(UsePipeline==false && PMC.length()!=0)
	
	AddToPaperFoundButNotUsePipeline(PDBIDAsFRomTheExcel+","+pdbbank.get(PDBIDAsFRomTheExcel).get("doi")+"\n");	
	}
} catch (DOMException | IOException | ParserConfigurationException | SAXException e) {
	// TODO Auto-generated catch block
	//e.printStackTrace();
}

		
		
		
		
	}

}
