package MCMBP.Authors.Mining;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import MCMBP.Resources.PDBe;
import MCMBP.Resources.crossref;
import MCMBP.Resources.elsevier;
import MCMBP.Utilities.CSVFilter;
import MCMBP.Utilities.Downloder;
import MCMBP.Utilities.TxtFiles;
import me.tongfei.progressbar.ProgressBar;

public class MiningInPapers implements Runnable {

	static String CrossrefEmail = "";
	static String ElsevierToken = "";

	public static HashMap<String, HashMap<String, String>> PMCAndPDB = new HashMap<String, HashMap<String, String>>();

	static int PMCidsIndex = 0;
	static Vector<String> CSVRecords = new Vector<String>();
	static HashMap<String, Vector<String>> Tools = new HashMap<String, Vector<String>>();

	static ProgressBar pb;

	static synchronized Map.Entry<String, HashMap<String, String>> PickPMCID() {
		if (!PMCAndPDB.isEmpty()) {

			Map.Entry<String, HashMap<String, String>> entry = PMCAndPDB.entrySet().iterator().next();
			PMCAndPDB.remove(entry.getKey());

			return entry;
		}
		return null;
	}

	static synchronized HashMap<String, HashMap<String, String>> GetByPubmed(String pubmed) {

		HashMap<String, HashMap<String, String>> temp = new HashMap<String, HashMap<String, String>>();
		for (String pdb : PMCAndPDB.keySet()) {

			if (PMCAndPDB.get(pdb).get("pubmedId").equals(pubmed)) {
				temp.put(pdb, PMCAndPDB.get(pdb));

			}
		}
		return temp;
	}

	static synchronized void AddCSVRecord(String Record) {

		CSVRecords.add(Record);

	}

	void UpdateLog() {
		if (new File("papers").exists() && new File("affiliations").exists()) {

			pb.step();

		}
	}

	public void PrepareDatasets(String FilterBy, String PDBList) throws IOException, ParseException {
		HashMap<String, Vector<String>> FilterByKeywordsAndValues = new HashMap<String, Vector<String>>();
		if (FilterBy.trim().length() != 0) {
			String[] FilterByKeywords = FilterBy.split("\\]");
			for (String keyword : FilterByKeywords) {
				String[] val = keyword.split(":");

				FilterByKeywordsAndValues.put(val[0].replaceAll("\\[", ""), new CSVFilter().SplitKeywordVal(val[1]));

			}
		}

		if (!new File("PDBBank.csv").exists())
			LoadDataFromPDB();

		CSVParser parser = new CSVParser(new FileReader("PDBBank.csv"), CSVFormat.DEFAULT);
		List<CSVRecord> list = parser.getRecords();

		String[] Headers = list.get(0).toString().split("values=\\[")[1].replaceAll("\\]\\]", "").split(",");
		HashMap<String, Integer> HeadersWithIndex = new HashMap<String, Integer>();

		int index = 0;
		for (String H : Headers) {
			HeadersWithIndex.put(H.trim(), index);
			index++;
		}

		list.remove(0);// remove headers record

		for (String FilterKeywords : FilterByKeywordsAndValues.keySet()) {

			list = new CSVFilter().FilterCSV(FilterByKeywordsAndValues.get(FilterKeywords), HeadersWithIndex, list,
					FilterKeywords);
		}

		if (PDBList.trim().length() != 0) {
			List<CSVRecord> ToRemove = new ArrayList<CSVRecord>();
			Vector<String> PDB = new TxtFiles().ReadIntoVec(PDBList, true);

			for (CSVRecord record : list) {
				if (!PDB.contains(record.get(HeadersWithIndex.get("structureId")).trim()))
					ToRemove.add(record);

			}

			for (CSVRecord pdb : ToRemove) {
				list.remove(pdb);
			}
		}

		for (CSVRecord record : list) {

			if (record.get(HeadersWithIndex.get("pubmedId")).length() != 0) {
				HashMap<String, String> RequiredCol = new HashMap<String, String>();
				RequiredCol.put("pubmedId", record.get(HeadersWithIndex.get("pubmedId")));
				RequiredCol.put("publicationYear", record.get(HeadersWithIndex.get("publicationYear")));
				RequiredCol.put("resolution", record.get(HeadersWithIndex.get("resolution")));
				RequiredCol.put("doi", record.get(HeadersWithIndex.get("doi")));

				PMCAndPDB.put(record.get(HeadersWithIndex.get("structureId")), RequiredCol);

			}

		}
		parser.close();

	}

	public void Mining(String FilterBy, String Pipeline, boolean UseDownloadedPapers, boolean Multithreaded,
			String crossrefEmail, String elsevierToken, String PDBList, boolean ExtractingInformation,
			String ApplicationIdBack4app, String APIKeyBack4app, String BingApiKey) throws FileNotFoundException,
			IOException, ParseException, XPathExpressionException, ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub

		CrossrefEmail = crossrefEmail;
		ElsevierToken = elsevierToken;

		System.out.println("FilterBy " + FilterBy);

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

		PrepareDatasets(FilterBy, PDBList);
		System.out.println("Number of PDB after filtering and which have PUB MED ID " + PMCAndPDB.size());
		NLP.LoadNLP();
		// start = Instant.now();
		pb = new ProgressBar("Downloading papers", PMCAndPDB.size());
		if (UseDownloadedPapers == false) {

			CheckDirAndFile("affiliations");
			CheckDirAndFile("papers");

			if (Multithreaded == false) {
				while (!PMCAndPDB.isEmpty()) {

					new MiningInPapers().run();

				}
			}

			if (Multithreaded == true) {

				ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
				while (!PMCAndPDB.isEmpty()) {
					es.execute(new MiningInPapers());// new thread
				}

				es.shutdown();

				while (es.isTerminated() == false)
					;
			}

		}

		pb.close();

		if (ExtractingInformation == true) {

			PrepareDatasets(FilterBy, PDBList);
			// NLP
			File[] affiliations = new File("affiliations").listFiles();
			File[] papers = new File("papers").listFiles();
			NLP.LoadNLP();
			pb = new ProgressBar("Extracting information ", papers.length);

			String CSVHeaders = "ID,Resolution,PublicationYear,Tool,PDB,MostCountry,ListOfCountries,FirstAuthor,PublishedInOnePaper,journal,occurrence,NameAsInPaper,journalAbbreviation,PartOfSentence,Confidence\n";
			String CSVWithNoneRepeatedPubid = CSVHeaders;
			String RecordNonRepeatedAuotherInfo = "";
			HashMap<String, String> RecordNonRepeatedPubid = new HashMap<String, String>();
			for (File j : affiliations) {
				File Paper = null;
				for (File x : papers) {
					if (j.getName().replaceAll(FilenameUtils.getExtension(j.getName()), "")
							.equals(x.getName().replaceAll(FilenameUtils.getExtension(x.getName()), ""))) {
						Paper = x;
					}
				}
				String Papertxt = new TxtFiles().readFileAsString(Paper.getAbsolutePath());

				pb.step();
				for (String toolofficialname : Tools.keySet()) {

					for (int t = 0; t < Tools.get(toolofficialname).size(); ++t) { // loop on the tool names.
						String tool = Tools.get(toolofficialname).get(t);
						if (Papertxt.contains(tool)) {
							Pair<String, String> IsToolMentioned = NLP.SplitSentence(Paper.getAbsolutePath(), tool);
							if (IsToolMentioned != null && IsToolMentioned.First.trim().length() != 0) {

								int occurrence = StringUtils.countMatches(Jsoup.parse(Papertxt).text(), tool);
								String Record = "";

								Vector<String> COUNTRIES = new Vector<String>();

								LinkedHashMap<Integer, Vector<String>> authors = ListofAffiliation(j.getAbsolutePath(),
										true, ApplicationIdBack4app, APIKeyBack4app, BingApiKey);

								for (Integer Author : authors.keySet()) {
									COUNTRIES.addAll(authors.get(Author));
								}

								String journalname = journal(j.getAbsolutePath(), "Title");
								String journalAbbreviation = journal(j.getAbsolutePath(), "ISOAbbreviation");

								LinkedHashMap<String, Integer> COUNTRIESfrequente = new LinkedHashMap<String, Integer>();
								for (String country : COUNTRIES) {
									if (COUNTRIESfrequente.containsKey(country))
										COUNTRIESfrequente.put(country, COUNTRIESfrequente.get(country) + 1);
									else
										COUNTRIESfrequente.put(country, 1);

								}

								Map.Entry<String, Integer> maxEntry = null;

								for (Map.Entry<String, Integer> entry : COUNTRIESfrequente.entrySet()) {
									if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
										maxEntry = entry;
									}
								}

								HashMap<String, HashMap<String, String>> pmc = GetByPubmed(
										j.getName().replaceAll("." + FilenameUtils.getExtension(j.getName()), ""));
								String PublishedInOnePaper = "F";
								if (pmc.size() > 1)
									PublishedInOnePaper = "T";

								String PDBs = "";
								String Reso = "";

								for (String PDBID : pmc.keySet()) {
									Record = pmc.get(PDBID).get("pubmedId") + "," + pmc.get(PDBID).get("resolution")
											+ "," + pmc.get(PDBID).get("publicationYear") + ",";
									PDBs += PDBID + " ";
									Reso += pmc.get(PDBID).get("resolution") + " ";
									RecordNonRepeatedAuotherInfo = pmc.get(PDBID).get("pubmedId") + "," + Reso + ","
											+ pmc.get(PDBID).get("publicationYear") + ",";

									String FirstAuthorCountry = "";
									if (authors.get(0).size() != 0) {
										FirstAuthorCountry = authors.get(0).get(0);
									}

									if (maxEntry != null) {
										String ListOfCOUNTRIES = "";
										for (String c : COUNTRIESfrequente.keySet())

											ListOfCOUNTRIES += c + " ";

										Record += toolofficialname + "," + PDBID + "," + maxEntry.getKey() + ","
												+ ListOfCOUNTRIES + "," + FirstAuthorCountry + "," + PublishedInOnePaper
												+ "," + journalname + "," + occurrence + "," + tool + ","
												+ journalAbbreviation + "," + IsToolMentioned.First + ","
												+ IsToolMentioned.Second + "\n";
										RecordNonRepeatedAuotherInfo += toolofficialname + "," + PDBs + ","
												+ maxEntry.getKey() + "," + ListOfCOUNTRIES + "," + COUNTRIES.get(0)
												+ "," + PublishedInOnePaper + "," + journalname + "," + occurrence + ","
												+ tool + "," + journalAbbreviation + "," + IsToolMentioned.First + ","
												+ IsToolMentioned.Second + "\n";
									}

									else {
										Record += toolofficialname + "," + PDBID + "," + "-1,-1," + FirstAuthorCountry
												+ "," + PublishedInOnePaper + "," + journalname + "\n";
										RecordNonRepeatedAuotherInfo += toolofficialname + "," + PDBID + ","
												+ "-1,-1,-1," + PublishedInOnePaper + "," + journalname + ","
												+ occurrence + "," + tool + "," + journalAbbreviation + ","
												+ IsToolMentioned.First + "," + IsToolMentioned.Second + "\n";
									}

									AddCSVRecord(Record);
								}

								CSVWithNoneRepeatedPubid += RecordNonRepeatedAuotherInfo;

								if (RecordNonRepeatedPubid.containsKey(
										j.getName().replaceAll("." + FilenameUtils.getExtension(j.getName()), ""))) { // if
																														// contained
																														// then,
																														// meaning
																														// two
																														// pipeline
																														// have
																														// used
																														// in
																														// this
																														// paper.
									RecordNonRepeatedPubid.remove(
											j.getName().replaceAll("." + FilenameUtils.getExtension(j.getName()), ""));
								} else {
									RecordNonRepeatedPubid.put(
											j.getName().replaceAll("." + FilenameUtils.getExtension(j.getName()), ""),
											RecordNonRepeatedAuotherInfo);
								}
								break; // we found one of the tool names
							}
						}
					}
				}
			}
			pb.close();

			String CSV = CSVHeaders;
			for (String record : CSVRecords)
				CSV += record;

			String RecordNonRepeatedPubidCSV = CSVHeaders;
			for (String record : RecordNonRepeatedPubid.keySet())
				RecordNonRepeatedPubidCSV += RecordNonRepeatedPubid.get(record);

			new TxtFiles().WriteStringToTxtFile("AuthorsInformation.csv", CSV);
			new TxtFiles().WriteStringToTxtFile("NonDuplicatedPipelineAuthorsInformation.csv",
					CSVWithNoneRepeatedPubid);
			new TxtFiles().WriteStringToTxtFile("NonDuplicatedPubid.csv", RecordNonRepeatedPubidCSV);
		}
	}

	public void LoadDataFromPDB() throws IOException {
		URL url = new URL(
				"http://www.rcsb.org/pdb/rest/customReport.csv?pdbids=*&customReportColumns=structureId,pmc,pubmedId,structureTitle,experimentalTechnique,publicationYear,resolution,doi&primaryOnly=1&service=wsfile&format=csv");
		FileUtils.copyURLToFile(url, new File("PDBBank.csv"));

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		Map.Entry<String, HashMap<String, String>> pmc = PickPMCID();
		if (pmc == null) {

			return;
		}

		HashMap<String, HashMap<String, String>> pmcForPDBe = new HashMap<String, HashMap<String, String>>();

		pmcForPDBe.put(pmc.getKey(), pmc.getValue());

		if (pmc.getValue().get("doi").trim().length() == 0)
			try {
				pmcForPDBe = new PDBe().UpdateDOI(pmcForPDBe, pmc.getKey());
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (ParseException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		pmc = pmcForPDBe.entrySet().iterator().next();

		UpdateLog();

		String url = "https://www.ebi.ac.uk/europepmc/webservices/rest/" + pmc.getValue().get("pubmedId")
				+ "/fullTextXML";
		String Txt = null;
		try {
			Txt = new Downloder().GetHttpRequste(url);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (Txt.trim().length() == 0) {

			try {
				Txt = new elsevier().Get(ElsevierToken, pmc.getValue().get("doi"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		if (Txt.trim().length() == 0) { // try crossref

			try {
				Txt = new crossref().Get(CrossrefEmail, pmc.getValue().get("doi"), pmc.getKey());
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

		if (Txt.length() != 0) {

			url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id="
					+ pmc.getValue().get("pubmedId") + "&retmode=xml";
			try {
				String affiliation = new Downloder().GetHttpRequste(url);
				if (affiliation.length() != 0) {

					new TxtFiles().WriteStringToTxtFile("affiliations/" + pmc.getValue().get("pubmedId") + ".xml",
							affiliation);
					new TxtFiles().WriteStringToTxtFile("papers/" + pmc.getValue().get("pubmedId") + ".txt", Txt);

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	LinkedHashMap<Integer, Vector<String>> ListofAffiliation(String xml, boolean DoNLP, String ApplicationIdBack4app,
			String APIKeyBack4app, String BingApiKey) throws ParserConfigurationException, MalformedURLException,
			SAXException, IOException, XPathExpressionException {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();

		db.setEntityResolver(new EntityResolver() {

			@Override
			public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

				return new InputSource(new StringReader(""));
			}
		});
		Document doc = db.parse(new File(xml));
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = "//*[local-name()='Author']";
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
		LinkedHashMap<Integer, Vector<String>> Authors = new LinkedHashMap<Integer, Vector<String>>();

		for (int a = 0; a < nodeList.getLength(); ++a) {
			Authors.put(a, new Vector<String>());
			for (int i = 0; i < nodeList.item(a).getChildNodes().getLength(); ++i) {
				if (nodeList.item(a).getChildNodes().item(i).getNodeName().equals("AffiliationInfo")) {
					for (int aff = 0; aff < nodeList.item(a).getChildNodes().item(i).getChildNodes()
							.getLength(); ++aff) {
						if (nodeList.item(a).getChildNodes().item(i).getChildNodes().item(aff).getNodeName()
								.equals("Affiliation")) {
							String val = nodeList.item(a).getChildNodes().item(i).getChildNodes().item(aff)
									.getTextContent();
							if (DoNLP == true) {
								val = NLP.TokenTypeCOUNTRY(val, ApplicationIdBack4app, APIKeyBack4app, BingApiKey);

							}
							Authors.get(a).add(val);
						}

					}
				}
			}
		}

		return Authors;

	}

	String journal(String xml, String InfoType) throws ParserConfigurationException, MalformedURLException,
			SAXException, IOException, XPathExpressionException {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		db.setEntityResolver(new EntityResolver() {

			@Override
			public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

				return new InputSource(new StringReader(""));
			}
		});

		Document doc = db.parse(new File(xml));
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = "//Journal/*[local-name()='" + InfoType + "']";
		NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

		if (nodeList.getLength() > 0)

			return nodeList.item(0).getTextContent().replaceAll(",", " ");
		else
			return "";

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

			System.out.print("Error: Unable to create ");

			return false;

		}

	}
}
