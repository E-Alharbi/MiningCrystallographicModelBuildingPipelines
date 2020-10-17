package MCMBP.Authors.Mining;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;

import org.json.simple.parser.ParseException;

import MCMBP.Utilities.TxtFiles;

public class Cluster {

	public void CreateJobs(String FilterBy, String Pipelines, String CrossrefEmail, String ElsevierToken,
			String PDBList, String JobParameters, String ApplicationIdBack4app, String APIKeyBack4app)
			throws IOException, ParseException, URISyntaxException, InterruptedException {

		String JarPath = Cluster.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().toString();
		MiningInPapers pmc = new MiningInPapers();
		pmc.PrepareDatasets(FilterBy, PDBList);

		int count = 1;
		for (String pdb : MiningInPapers.PMCAndPDB.keySet()) {
			System.out.println("preparing job " + count + " out of " + MiningInPapers.PMCAndPDB.keySet().size());
			count++;
			if (!new File("papers/" + MiningInPapers.PMCAndPDB.get(pdb).get("pubmedId") + ".txt").exists()) {
				String Script = "#!/bin/bash\n" + SplitJobParameters(JobParameters) + "\n";

				Script += "\n java -Xmx3048m -jar " + JarPath + " MiningAuthors FilterBy=\"[structureId:" + pdb
						+ "]\" Pipeline=\"" + Pipelines + "\" CrossrefEmail=" + CrossrefEmail + " ElsevierToken="
						+ ElsevierToken + " ExtractingInformation=F " + " ApplicationIdBack4app="
						+ ApplicationIdBack4app + " APIKeyBack4app=" + APIKeyBack4app;
				new TxtFiles().WriteStringToTxtFile(pdb + ".sh", Script);

				if (GetNumberOfpendingJobs() < 9000) {
					RunCmd("sbatch " + pdb + ".sh");
				} else {
					while (GetNumberOfpendingJobs() > 9000) {
						Thread.sleep(100);
					}
				}

			}
		}

	}

	String GetLogname() throws IOException {
		return System.getenv("LOGNAME");

	}

	int GetNumberOfpendingJobs() throws IOException {

		BufferedReader stdInput = RunCmd("squeue -u " + GetLogname() + " -t PENDING");
		String st = "";
		String Num = "";
		while ((st = stdInput.readLine()) != null) {

			Num += st + "\n";
		}
		return Integer.valueOf(Num.split("\n").length);
	}

	BufferedReader RunCmd(String cmd) throws IOException {

		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(cmd);
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		return stdInput;
	}

	String SplitJobParameters(String Parameters) {
		String Pram = "";
		String Val = "";
		boolean IsParm = false;
		boolean IsVal = false;

		LinkedHashMap<String, String> JobParameters = new LinkedHashMap<String, String>();

		for (int i = 0; i < Parameters.length(); ++i) {
			if (Parameters.charAt(i) == ']') {

				JobParameters.put(Pram, Val);
				Pram = "";
				Val = "";
				IsParm = false;
				IsVal = false;

			}
			if (IsVal == true) {
				Val += Parameters.charAt(i);
			}
			if (Parameters.charAt(i) == '#') {

				IsParm = false;
				IsVal = true;
			}
			if (IsParm == true) {
				Pram += Parameters.charAt(i);
			}
			if (Parameters.charAt(i) == '[') {
				Pram = "";
				IsParm = true;
			}

		}

		String ScriptHeader = "";
		for (String Parm : JobParameters.keySet()) {
			if (JobParameters.get(Parm).trim().length() != 0)
				ScriptHeader += "#SBATCH " + Parm + "=" + JobParameters.get(Parm) + "\n";
			else
				ScriptHeader += Parm + "\n";
		}

		return ScriptHeader;
	}

}
