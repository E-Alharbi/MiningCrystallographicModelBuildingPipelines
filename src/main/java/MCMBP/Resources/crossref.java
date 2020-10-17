package MCMBP.Resources;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import MCMBP.Utilities.Downloder;
import MCMBP.Utilities.TxtFiles;

public class crossref {
	public static void main(String[] args)
			throws IOException, URISyntaxException, ParseException, ParserConfigurationException, SAXException {

	}

	public String Get(String CrossrefEmail, String DOI, String PDB)
			throws IOException, ParserConfigurationException, SAXException {

		String PMC = new Downloder().GetHttpRequste(
				"https://doi.crossref.org/servlet/query?pid=" + CrossrefEmail + "&format=unixref&id=" + DOI);
		String Txt = "";
		if (PMC.contains("collection property=")) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(
					new URL("https://doi.crossref.org/servlet/query?pid=" + CrossrefEmail + "&format=unixref&id=" + DOI)
							.openStream());

			if (!PMC.contains("syndication")) { // links contain syndication do not work

				if (doc.getElementsByTagName("resource").item(1).getTextContent().toLowerCase().contains("pdf")) {

					File myFile = new File(new Downloder().Download(
							doc.getElementsByTagName("resource").item(1).getTextContent().trim(), PDB, "pdf"));
					PDDocument docpdf;
					try {
						docpdf = PDDocument.load(myFile);
						PDFTextStripper stripper = new PDFTextStripper();
						String text = stripper.getText(docpdf);
						Txt = text;

						docpdf.close();
						FileUtils.deleteQuietly(myFile);

					} catch (Exception e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
						if (myFile.exists())
							FileUtils.deleteQuietly(myFile);

					}

				} else {

					Txt = new TxtFiles().readFileAsString(new Downloder().Download(
							doc.getElementsByTagName("resource").item(1).getTextContent().trim(), PDB, "html"));
				}

			} else {

				if (doc.getElementsByTagName("resource").item(0).getTextContent().contains("gad")) {

					Txt = new TxtFiles().readFileAsString(new Downloder().Download(
							"http://genesdev.cshlp.org/content/"
									+ doc.getElementsByTagName("item_number").item(0).getTextContent() + ".full",
							PDB, "html"));

				} else if (doc.getElementsByTagName("resource").item(0).getTextContent().contains("jbc")) {

					String ID = doc.getElementsByTagName("item_number").item(0).getTextContent().replaceAll("/jbc/",
							"");
					ID = ID.replaceAll(".atom", "");

					Txt = new TxtFiles().readFileAsString(new Downloder().Download(
							"https://www.jbc.org/content/"
									+ doc.getElementsByTagName("item_number").item(0).getTextContent() + ".full",
							PDB, "html"));
				} else {

					Txt = new TxtFiles().readFileAsString(new Downloder().Download(
							doc.getElementsByTagName("resource").item(0).getTextContent().trim(), PDB, "html"));

				}

			}

		}
		if (new File(PDB + ".html").exists())
			FileUtils.deleteQuietly(new File(PDB + ".html"));
		return Txt;
	}
}
