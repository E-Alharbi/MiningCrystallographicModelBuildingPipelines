package MCMBP.Authors.Mining;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import org.jsoup.Jsoup;

import MCMBP.Utilities.TxtFiles;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class SentenceAnalysis {

	public void Analysis(String Pipeline, String PapersFolder) throws IOException {

		HashMap<String, Vector<String>> Tools = new HashMap<String, Vector<String>>();
		// String
		// Pipeline="arp/warp:ARP/wARP,ARP/warp:ARP/wARP,ARP/wARP:ARP/wARP,ARP/WARP:ARP/wARP,buccaneer:Buccaneer,Buccaneer:Buccaneer,BUCCANEER:Buccaneer,shelxe:Shelxe,Shelxe:Shelxe,SHELXE:Shelxe,phenix.autobuild:PHENIX
		// AutoBuild,phenix autobuild:PHENIX AutoBuild,AutoBuild:PHENIX
		// AutoBuild,PHENIX.AUTOBUILD:PHENIX AutoBuild,PHENIX AUTOBUILD:PHENIX
		// AutoBuild,AUTOBUILD:PHENIX AutoBuild,PHENIX AutoBuild:PHENIX AutoBuild";
		Vector<String> OfficialPipelineNames = new Vector<String>(Arrays.asList(Pipeline.split(",")));
		for (String Pipe : OfficialPipelineNames) {

			if (Tools.containsKey(Pipe.split(":")[1])) {
				Vector<String> pipelinesname = Tools.get(Pipe.split(":")[1]);
				pipelinesname.add(Pipe.split(":")[0]);
				Tools.put(Pipe.split(":")[1], pipelinesname);
			} else {
				Vector<String> pipelinesname = new Vector<String>();
				pipelinesname.add(Pipe.split(":")[0]);
				Tools.put(Pipe.split(":")[1], pipelinesname);
			}
		}

		System.out.println(Tools);
		// TODO Auto-generated method stub
		// set up pipeline properties
		Properties props = new Properties();
		// set the list of annotators to run
		props.setProperty("annotators", "tokenize,ssplit");
		// build pipeline

		Vector<String> Sentences = new Vector<String>();

		File[] papers = new File(PapersFolder).listFiles();
		int countpaper = 0;
		for (File paper : papers) {
			System.out.println("count " + countpaper);
			countpaper++;
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
			// create a document object
			CoreDocument doc = new CoreDocument(
					Jsoup.parse(new TxtFiles().readFileAsString(paper.getAbsolutePath())).text());

			// annotate
			pipeline.annotate(doc);
			// display sentences
			String sentence = "";
			for (CoreSentence sent : doc.sentences()) {
				for (String pipe : Tools.keySet()) {
					for (int p = 0; p < Tools.get(pipe).size(); ++p) {
						if (sent.text().contains(Tools.get(pipe).get(p))) {
							// System.out.println("Sent: "+sent.text());
							sentence = Jsoup.parse(sent.text()).text();
							Sentences.add(sentence);

						}
					}

				}

			}
		}

		String SentencesFile = "";
		for (int s = 0; s < Sentences.size(); ++s)
			SentencesFile += Sentences.get(s) + "\n";
		new TxtFiles().WriteStringToTxtFile("Sentences.txt", SentencesFile);
		HashMap<String, Integer> WordCount = new HashMap<String, Integer>();
		// set up pipeline properties
		Properties props1 = new Properties();
		// set the list of annotators to run
		props1.setProperty("annotators", "tokenize,ssplit,pos,lemma,pos");
		// build pipeline
		StanfordCoreNLP pipeline1 = new StanfordCoreNLP(props1);
		// create a document object
		for (int s = 0; s < Sentences.size(); ++s) {
			CoreDocument document1 = pipeline1.processToCoreDocument(Sentences.get(s));
			// display tokens
			for (CoreLabel tok : document1.tokens()) {

				if (tok.tag().startsWith("V") || tok.tag().startsWith("N")) {
					if (WordCount.containsKey(tok.lemma()))
						WordCount.put(tok.lemma(), WordCount.get(tok.lemma()) + 1);
					else
						WordCount.put(tok.lemma(), 1);
				}

			}
		}

		String CSV = "Word,Occurrence\n";
		for (String word : WordCount.keySet()) {
			CSV += word.replaceAll(",", "") + "," + WordCount.get(word) + "\n";
		}
		new TxtFiles().WriteStringToTxtFile("Words.csv", CSV);
	}

}
