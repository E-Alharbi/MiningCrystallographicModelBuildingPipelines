package MCMBP.Authors.Mining;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;





public class Mining {

	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException, URISyntaxException, InterruptedException, XPathExpressionException, ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub

		Vector<String> Parm = new Vector<String>();
		boolean Multithreaded=false;
		
		for (int i = 0; i < args.length; ++i) {

			if (args[i].contains("=")) {
				Parm.addAll(Arrays.asList(args[i].split("=")));
			}

		}
		boolean ExtractingInformation=true;
		if(checkArg(Parm,"ExtractingInformation").trim().length()!=0)
		{
			if(checkArg(Parm,"ExtractingInformation").equals("F"))
				ExtractingInformation=false;
		}
		
		if(checkArg(Parm,"Multithreaded").trim().length()!=0)
		{
			if(checkArg(Parm,"Multithreaded").equals("T"))
				Multithreaded=true;
		}
		if(args[0].equals("MiningAuthors")) {
			
			if(checkArg(Parm,"Pipeline").trim().length()==0)
			{
				System.out.println("Please type in the pipelines/tools that you want to mining about. For example. Pipeline=\"arp/warp:ARP/wARP,buccaneer:Buccaneer,shelxe:Shelxe,phenix.autobuild:Phenix Autobuild,phenix autobuild:Phenix Autobuild\". The name before(:) is the pipeline official name that usauly uses in the reserach paper when they refer to the pipeline and the name after (:) is to use in the CSV file. This help when the pipeline mentions in differnts names in differnts reserach papers.");
				System.exit(-1);
			}
			boolean UseExistsPapers=false;
			if(checkArg(Parm,"UseExistsPapers").trim().length()!=0)
			{
				if(checkArg(Parm,"UseExistsPapers").equals("T")) {
					UseExistsPapers=true;
					if(checkArg(Parm,"FilterBy").trim().length()!=0)
					{
						
							System.out.println("FilterBy will not be used as UseExistsPapers set to true");
					}
				}
					
			}
			
			
			
			
			new PMCMultiThreaded().Mining(checkArg(Parm,"FilterBy"),checkArg(Parm,"Pipeline"),UseExistsPapers,Multithreaded,checkArg(Parm,"CrossrefEmail"),checkArg(Parm,"ElsevierToken"),checkArg(Parm,"PDBList"),ExtractingInformation,checkArg(Parm,"ApplicationIdBack4app"),checkArg(Parm,"APIKeyBack4app"));
		}
		
		if(args[0].equals("Cluster")) {
			
			new Cluster().CreateJobs(checkArg(Parm,"FilterBy"),checkArg(Parm,"Pipeline"),checkArg(Parm,"CrossrefEmail"),checkArg(Parm,"ElsevierToken"),checkArg(Parm,"PDBList"),checkArg(Parm,"JobParameters"),checkArg(Parm,"ApplicationIdBack4app"),checkArg(Parm,"APIKeyBack4app"));
		}
		
		
	
	}
	static String checkArg(Vector<String> Args, String Keyword) {
		for (int i = 0; i < Args.size(); ++i) {
			if (Args.get(i).equals(Keyword)) {
				return Args.get(i + 1);
			}
		}
		return "";

	}
}
