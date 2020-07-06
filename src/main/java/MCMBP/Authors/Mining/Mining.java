package MCMBP.Authors.Mining;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import MCMBP.Pipelines.Mining.MiningPipelines;



public class Mining {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub

		Vector<String> Parm = new Vector<String>();
		for (int i = 0; i < args.length; ++i) {

			if (args[i].contains("=")) {
				Parm.addAll(Arrays.asList(args[i].split("=")));
			}

		}
		if(args[0].equals("MiningAuthors")) {
			
			if(checkArg(Parm,"Pipeline").trim().length()==0)
			{
				System.out.println("Please type in the pipelines/tools that you want to mining about. For example. Pipeline=\"arp/warp:ARP/wARP,buccaneer:Buccaneer,shelxe:Shelxe,phenix.autobuild:Phenix Autobuild,phenix autobuild:Phenix Autobuild\". The name before(:) is the pipeline official name that usauly uses in the reserach paper when they refer to the pipeline and the name after (:) is to use in the CSV file. This help when the pipeline mentions in differnts names in differnts reserach papers.");
				System.exit(-1);
			}
			new PMCMultiThreaded().Mining(checkArg(Parm,"FilterBy"),checkArg(Parm,"Pipeline"));
		}
		
	if(args[0].equals("MiningPipeline")) {
			
			if(checkArg(Parm,"Pipeline").trim().length()==0)
			{
				System.out.println("Please type in the pipelines/tools that you want to mining about. For example. Pipeline=\"arp/warp:ARP/wARP,buccaneer:Buccaneer,shelxe:Shelxe,phenix.autobuild:Phenix Autobuild,phenix autobuild:Phenix Autobuild\". The name before(:) is the pipeline official name that usauly uses in the reserach paper when they refer to the pipeline and the name after (:) is to use in the CSV file. This help when the pipeline mentions in differnts names in differnts reserach papers.");
				System.exit(-1);
			}
			if(checkArg(Parm,"CrossrefEmail").trim().length()==0)
			{
				System.out.println("Please type in your registered email in Crossref. If you did not register, please do it from here https://apps.crossref.org/clickthrough/researchers. Then, you can enter your email using this keyword CrossrefEmail=youremail@email.com");
				System.exit(-1);
			}
			if(checkArg(Parm,"ElsevierToken").trim().length()==0)
			{
				System.out.println("Please type in your Elsevier Token. If you did not have Elsevier Token, you can get it from here  https://dev.elsevier.com and selects get api key. Then, you can enter your token using this keyword ElsevierToken=aaaaaa");
				System.exit(-1);
			}
			new MiningPipelines().Mining(checkArg(Parm,"Pipeline"),checkArg(Parm,"PDBList"),checkArg(Parm,"CrossrefEmail"),checkArg(Parm,"ElsevierToken"),checkArg(Parm,"FilterBy"));
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
