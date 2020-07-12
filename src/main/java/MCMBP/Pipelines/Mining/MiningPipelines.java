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
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import MCMBP.Authors.Mining.PMCMultiThreaded;
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
	public  void Mining(String Pipeline, String PDBList, String CrossrefEmail1, String ElsevierToken1, String FilterBy, boolean Multithreaded) throws IOException {
		// TODO Auto-generated method stub
		CrossrefEmail=CrossrefEmail1;
		ElsevierToken=ElsevierToken1;
		System.out.println(Pipeline);
		System.out.println(CrossrefEmail1);
		Vector<String> PDB= new Vector<String>();
		if(PDBList.trim().length()!=0) {
			 PDB= new TxtFiles().ReadIntoVec(PDBList,true);
				System.out.println("FilterBy will not be used as UseExistsPapers set to true");

		}
		else {
			PMCMultiThreaded pmc= 	new PMCMultiThreaded();
			pmc.PrepareDatasets(FilterBy);
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

try {
	PDBBankRes = new Downloder().GetHttpRequste("https://www.rcsb.org/pdb/rest/customReport.csv?pdbids="+PDBIDAsFRomTheExcel+"&customReportColumns=pubmedId,doi&primaryOnly=1&service=wsfile&format=csv");
} catch (IOException e1) {
	// TODO Auto-generated catch block
	//e1.printStackTrace();
}
//System.out.println(new CSVReader().ReadIntoHashMap(PDBBankRes, "structureId"));


String pubmedId="";
try {
	if(new CSVReader().ReadIntoHashMap(PDBBankRes, "structureId").size()>0) { // in case not data found in PDBBank
	pubmedId=new CSVReader().ReadIntoHashMap(PDBBankRes, "structureId").get(PDBIDAsFRomTheExcel).get("pubmedId");
	String PMC=  new Downloder().GetHttpRequste("https://www.ebi.ac.uk/europepmc/webservices/rest/"+pubmedId+"/fullTextXML");
	if(PMC.trim().length()==0) {
		PMC=new Downloder().GetHttpRequste("https://api.elsevier.com/content/article/doi/"+new CSVReader().ReadIntoHashMap(PDBBankRes, "structureId").get(PDBIDAsFRomTheExcel).get("doi")+"?APIKey="+ElsevierToken);
	if(PMC.trim().length()==0) {
		//System.out.println(new CSVReader().ReadIntoHashMap(PDBBankRes, "structureId").get(PDBID).get(0).get("doi"));
		
		PMC=new Downloder().GetHttpRequste("https://doi.crossref.org/servlet/query?pid="+CrossrefEmail+"&format=unixref&id="+new CSVReader().ReadIntoHashMap(PDBBankRes, "structureId").get(PDBIDAsFRomTheExcel).get("doi"));
		if(PMC.contains("collection property=")) {
			//System.out.println(PMC.split("collection property=")[1].split("</collection>")[0]);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new URL("https://doi.crossref.org/servlet/query?pid="+CrossrefEmail+"&format=unixref&id="+new CSVReader().ReadIntoHashMap(PDBBankRes, "structureId").get(PDBIDAsFRomTheExcel).get("doi")).openStream());
			
			if(!PMC.contains("syndication")) { // links contain syndication do not work  
		
		//System.out.println(doc.getElementsByTagName("resource").item(0).getTextContent());
		//System.out.println(doc.getElementsByTagName("resource").item(1).getTextContent());
		if(doc.getElementsByTagName("resource").item(1).getTextContent().toLowerCase().contains("pdf")) {
			
	
		
		File myFile = new File(new Downloder().Download(doc.getElementsByTagName("resource").item(1).getTextContent().trim(),"pdf"));
	
	     PDDocument docpdf;
		try {
			docpdf = PDDocument.load(myFile);
			 PDFTextStripper stripper = new PDFTextStripper();
		        String text = stripper.getText(docpdf);
		        PMC=text;
		       // System.out.println("Text size: " + text.length() + " characters:");
		        docpdf.close();
		        FileUtils.deleteQuietly(myFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			
		} 
	
	       
	        //System.out.println(text);
	    }
		else {
			//System.out.println("not pdf");
			
			PMC=new TxtFiles().readFileAsString(new Downloder().Download(doc.getElementsByTagName("resource").item(1).getTextContent().trim(),"html"));
		}
		
	   	
	
			
			
			
		
	
	}
	else {
		
	if(doc.getElementsByTagName("resource").item(0).getTextContent().contains("gad")) {
		//System.out.println("GAD");
		
		PMC=new TxtFiles().readFileAsString(new Downloder().Download("http://genesdev.cshlp.org/content/"+doc.getElementsByTagName("item_number").item(0).getTextContent()+".full","html"));
	}
	else if(doc.getElementsByTagName("resource").item(0).getTextContent().contains("jbc")) {
		//System.out.println("JBC");
		String ID=doc.getElementsByTagName("item_number").item(0).getTextContent().replaceAll("/jbc/", "");
		ID=ID.replaceAll(".atom", "");
		//System.out.println(ID);
		
		PMC=new TxtFiles().readFileAsString(new Downloder().Download("https://www.jbc.org/content/"+doc.getElementsByTagName("item_number").item(0).getTextContent()+".full","html"));
	}
	else {
		
		//System.out.println("CAN");
		
		PMC=new TxtFiles().readFileAsString(new Downloder().Download(doc.getElementsByTagName("resource").item(0).getTextContent().trim(),"html"));
		
	}
	
	
	
	
	
	}
		
		}
	}
	}

	
	if(PMC.length()==0) {
		AddToPaperNotFound(PDBIDAsFRomTheExcel+","+new CSVReader().ReadIntoHashMap(PDBBankRes, "structureId").get(PDBIDAsFRomTheExcel).get("doi")+"\n");
		
	}
	
	
	boolean UsePipeline=false;
	if(PMC.trim().length()!=0)
	for(String tool : Tools.keySet() ) {
		if(PMC.toLowerCase().contains(tool)) {
			
			AddToCSV(PDBIDAsFRomTheExcel+","+Tools.get(tool)+","+new CSVReader().ReadIntoHashMap(PDBBankRes, "structureId").get(PDBIDAsFRomTheExcel).get("doi")+"\n");
			
			UsePipeline=true;
		}
		}
	if(UsePipeline==false && PMC.length()!=0)
	
	AddToPaperFoundButNotUsePipeline(PDBIDAsFRomTheExcel+","+new CSVReader().ReadIntoHashMap(PDBBankRes, "structureId").get(PDBIDAsFRomTheExcel).get("doi")+"\n");	
	}
} catch (DOMException | IOException | ParserConfigurationException | SAXException e) {
	// TODO Auto-generated catch block
	//e.printStackTrace();
}

		
		
		
		
	}

}
