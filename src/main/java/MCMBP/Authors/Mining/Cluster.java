package MCMBP.Authors.Mining;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import org.json.simple.parser.ParseException;

import MCMBP.Utilities.TxtFiles;

public class Cluster {

	public  void CreateJobs(String FilterBy , String Pipelines,String CrossrefEmail, String ElsevierToken , String PDBList , String JobParameters) throws IOException, ParseException, URISyntaxException, InterruptedException {
		// TODO Auto-generated method stub

		String JarPath=Cluster.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().toString();
		PMCMultiThreaded pmc = new PMCMultiThreaded();
		pmc.PrepareDatasets(FilterBy, PDBList);
		String Sbatch="";
		
		//int countSbatch=0;
		for(String pdb : PMCMultiThreaded.PMCAndPDB.keySet()) {
			if(!new File("papers/"+PMCMultiThreaded.PMCAndPDB.get(pdb).get("pubmedId")+".txt").exists()) {
			String Script="#!/bin/bash\n" +JobParameters+"\n"; 
					
			
			Script+="\n java  -jar "+JarPath+" MiningAuthors FilterBy=\"[structureId:"+pdb+"]\" "+Pipelines+" CrossrefEmail="+CrossrefEmail+" ElsevierToken="+ElsevierToken+" ExtractingInformation=F";
			new TxtFiles().WriteStringToTxtFile(pdb+".sh",Script);
			if(GetNumberOfpendingJobs() < 9000) {
				RunCmd("sbatch "+pdb+".sh");
			}
			else {
				while(GetNumberOfpendingJobs() > 9000) {
					Thread.sleep(1000);
				}
			}
			
			/*
			Sbatch+="sbatch "+pdb+".sh \n";
			if(Sbatch.split("\n").length>9000) {
				new TxtFiles().WriteStringToTxtFile("Sbatch"+countSbatch+".sh",Sbatch);
				countSbatch++;
				
				Sbatch="";
			}
			*/
			
			}
		}
		//new TxtFiles().WriteStringToTxtFile("SbatchLast.sh",Sbatch);
		
	}
	
	String GetLogname() throws IOException {
		return System.getenv("LOGNAME");
		
	}
	int GetNumberOfpendingJobs() throws IOException {
		
		BufferedReader stdInput = RunCmd("squeue -u "+GetLogname()+" -t PENDING | wc -l ");
		String st="";
		String Num="";
		while ((st = stdInput.readLine()) != null) {
			System.out.println(st);
			Num+=st;
		}
		return Integer.valueOf(Num);
	}
	
	BufferedReader RunCmd(String cmd) throws IOException {
		System.out.println(cmd);
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(cmd);
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
return stdInput;
	}
	

}
