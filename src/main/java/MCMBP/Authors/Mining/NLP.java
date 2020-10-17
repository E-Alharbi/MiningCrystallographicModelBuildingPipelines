package MCMBP.Authors.Mining;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.jsoup.Jsoup;
import org.xml.sax.SAXException;

import MCMBP.Utilities.BingMaps;
import MCMBP.Utilities.GeoInfo;
import MCMBP.Utilities.TxtFiles;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class NLP {
	static StanfordCoreNLP pipeline;
	static StanfordCoreNLP pipeline1;
	static StanfordCoreNLP pipeline2;

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException,
			XPathExpressionException, ParserConfigurationException, SAXException {

		// Example
		NLP.LoadNLP();
		// System.out.println(NLP.SplitSentence("20862319.txt", "ARP/wARP").toString());
		System.out.println(
				NLP.IsMentioned(" Automated model building was conducted with ARP/wARP", "ARP/wARP").toString());

	}

	static void LoadNLP() {
		Properties props = new Properties();
		// props.setProperty("annotators",
		// "tokenize,ssplit,pos,lemma,ner,parse,coref,quote");
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");

		pipeline = new StanfordCoreNLP(props);

		Properties props1 = new Properties();
		props1.setProperty("annotators", "tokenize,ssplit");
		pipeline1 = new StanfordCoreNLP(props1);

		Properties props2 = new Properties();

		props2.setProperty("annotators", "tokenize,ssplit,pos,lemma");

		pipeline2 = new StanfordCoreNLP(props2);

	}

	static synchronized String TokenTypeCOUNTRY(String Txt, String ApplicationIdBack4app, String APIKeyBack4app,
			String BingApiKey) throws IOException {

		CoreDocument document = new CoreDocument(Txt);
		pipeline.annotate(document);

		// get confidences for entities
		String COUNTRY = "";

		for (CoreEntityMention em : document.entityMentions()) {

			// System.out.println("Text "+em.text());
			// System.out.println("Type "+em.entityType());
			if (em.entityType().equals("COUNTRY"))
				COUNTRY = em.text();

		}
		// System.out.println("COUNTRY "+COUNTRY);
		if (COUNTRY.trim().length() == 0) { // if country not found in the author affiliation, we get the county name
											// from the city or state name
			for (CoreEntityMention em : document.entityMentions()) {

				if (em.entityType().equals("CITY") || em.entityType().equals("STATE_OR_PROVINCE")
						|| em.entityType().equals("LOCATION")) {

					COUNTRY = new GeoInfo().country(em.text(), ApplicationIdBack4app, APIKeyBack4app);
				}
				if (COUNTRY != null && COUNTRY.trim().length() != 0) // if we get the country, we did not need to
																		// continue
					break;
			}
		}

		if (COUNTRY == null || COUNTRY.trim().length() == 0) { // if still not found the county name, we try to get from
																// the organization address
			String ORGANIZATIONAddress = "";
			for (CoreEntityMention em : document.entityMentions()) {
				if (em.entityType().equals("ORGANIZATION") || em.entityType().equals("CITY")
						|| em.entityType().equals("STATE_OR_PROVINCE") || em.entityType().equals("LOCATION")) {
					ORGANIZATIONAddress += em.text() + ",";
					// Set<String> CountiesList=new BingMaps().OrganizationLocation(em.text());
					// if(CountiesList.size()==1)
					// if(COUNTRY==null || COUNTRY.trim().length()==0)
					// COUNTRY=CountiesList.toArray()[0].toString();

				}
			}

			Set<String> CountiesList = new BingMaps().OrganizationLocation(ORGANIZATIONAddress, BingApiKey);
			if (CountiesList.size() == 1)
				COUNTRY = CountiesList.toArray()[0].toString();

		}

		return COUNTRY;
	}

	public static Pair<String, String> SplitSentence(String Paper, String Tool) throws IOException {

		CoreDocument doc = new CoreDocument(
				Jsoup.parse(new TxtFiles().readFileAsString(new File(Paper).getAbsolutePath())).text());
		pipeline1.annotate(doc);
		Pair<String, String> Con = null;
		// Pair <String,String> ToReturn=null;
		Vector<Pair<String, String>> ToReturn = new Vector<Pair<String, String>>();
		for (CoreSentence sent : doc.sentences()) {
			// if(sent.text().contains(Tool)) {
			Con = IsMentioned(sent.text(), Tool);

			// System.out.println(sent.text());
			if (Con.Second.equals("High") && Con.First != null && Con.First.trim().length() != 0) {
				// System.out.println(sent.text());
				// System.out.println(sent.text());
				return Con;
			}
			if (Con.Second.equals("Low")) {
				// ToReturn=Con;
				ToReturn.add(Con);
			}

			// }

		}
		if (ToReturn.size() > 0)
			return ToReturn.get(0);

		return null;
	}

	static Pair<String, String> IsMentioned(String Sentence, String Tool) {
		// Sentence=Jsoup.parse(Sentence).text();
		// System.out.println(Sentence);
		String BuildingKeywordsInSentence = "build|building|automate|trace|determine|rebuild|generate|process|tracing|place|complete";
		String RefAndModKeywordsInSentence = "modification|density|refinement|refine";

		CoreDocument document1 = pipeline2.processToCoreDocument(Sentence);
		String lemmatizeSentence = "";
		for (CoreLabel tok : document1.tokens()) {

			lemmatizeSentence += tok.lemma() + tok.after();

		}

		Pattern PatternRegex = Pattern.compile("(" + BuildingKeywordsInSentence + ")+(.*)" + Tool.toLowerCase() + "|"
				+ Tool.toLowerCase() + "(.*)" + "(" + BuildingKeywordsInSentence + ")+");
		Matcher matcher = PatternRegex.matcher(lemmatizeSentence.toLowerCase());
		// System.out.println("here "+lemmatizeSentence);
		String Building = "";
		String RefAndMod = "";
		if (matcher.find()) {
			Building = matcher.group();
			RefAndMod = "High";
			System.out.println("matcher " + matcher.group());
			// System.out.println("matcher "+matcher.group());

			System.out.println("here " + matcher.groupCount());
			// System.out.println("here "+lemmatizeSentence);
		}

		PatternRegex = Pattern.compile("(" + RefAndModKeywordsInSentence + ")+(.*)" + Tool.toLowerCase() + "|"
				+ Tool.toLowerCase() + "(.*)" + "(" + RefAndModKeywordsInSentence + ")+");
		matcher = PatternRegex.matcher(lemmatizeSentence);

		if (matcher.find()) {
			RefAndMod = "Low";
			// System.out.println(matcher.group());
		}

		return new Pair<String, String>(Building.replaceAll(",", ""), RefAndMod.replaceAll(",", ""));

	}
}
