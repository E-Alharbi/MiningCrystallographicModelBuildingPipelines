package MCMBP.Analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import MCMBP.Utilities.TxtFiles;

public class PapersAnalysis {

	public void Analysis(String CSVFile) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		// usual input NonDuplicatedPubidNewRESOLVE.csv
		int NumberOfPaperToPutInOther = 50;
		CSVParser parser = new CSVParser(new FileReader(CSVFile), CSVFormat.EXCEL.withHeader());
		List<CSVRecord> list = parser.getRecords();

		parser = new CSVParser(
				new FileReader(new PapersAnalysis()
						.TrimRecords(list, "journal", NumberOfPaperToPutInOther, parser.getHeaderNames(),
								"journals with less than " + NumberOfPaperToPutInOther + " research papers")
						.getAbsoluteFile()),
				CSVFormat.EXCEL.withHeader());
		list = parser.getRecords();

		HashMap<String, Vector<String>> journals = new PapersAnalysis().FilterByCol(list, "journal");
		HashMap<String, Vector<String>> PublicationYear = new PapersAnalysis().FilterByCol(list, "PublicationYear");

		HashMap<String, Double> journalscount = new HashMap<String, Double>();
		HashMap<String, HashMap<String, Double>> journalscountByPipeline = new HashMap<String, HashMap<String, Double>>();

		for (String j : journals.keySet()) {

			journalscount.put(j, Double.valueOf(journals.get(j).size()));

			journalscountByPipeline = new PapersAnalysis().CountPipeline(list, "journal", j, journalscountByPipeline);

		}

		HashMap<String, Double> PublicationYearcount = new HashMap<String, Double>();
		HashMap<String, HashMap<String, Double>> PublicationYearByPipeline = new HashMap<String, HashMap<String, Double>>();

		for (String j : PublicationYear.keySet()) {
			PublicationYearcount.put(j, Double.valueOf(PublicationYear.get(j).size()));
			PublicationYearByPipeline = new PapersAnalysis().CountPipeline(list, "PublicationYear", j,
					PublicationYearByPipeline);

		}

		for (String j : journals.keySet()) {
			journalscount.put(j,
					new BigDecimal(journals.get(j).size()).setScale(2, RoundingMode.HALF_UP).doubleValue());

		}

		LinkedHashMap<String, Double> sorted = new PapersAnalysis().sortHashMapByValues(journalscount);
		String CSV = "journal,papercount\n";

		for (String j : sorted.keySet()) {

			CSV += j + "," + sorted.get(j) + "\n";

		}

		new TxtFiles().WriteStringToTxtFile("journals.csv", CSV);

		CSV = "journal,Pipeline,papercount\n";
		for (String j : journalscountByPipeline.keySet()) {

			for (String pipe : journalscountByPipeline.get(j).keySet()) {
				for (int p = 0; p < journalscountByPipeline.get(j).get(pipe); ++p) {
					CSV += j + "," + pipe + "," + sorted.get(j) + "\n";

				}
			}
		}

		new TxtFiles().WriteStringToTxtFile("journalsbyPipelines.csv", CSV);

		CSV = "PublicationYear,count\n";
		for (String j : PublicationYearcount.keySet()) {
			CSV += j + "," + PublicationYearcount.get(j) + "\n";
		}
		new TxtFiles().WriteStringToTxtFile("PublicationYear.csv", CSV);

		CSV = "PublicationYear,Pipeline,count\n";
		for (String j : PublicationYearcount.keySet()) {

			for (String pipe : PublicationYearByPipeline.get(j).keySet()) {
				for (int p = 0; p < PublicationYearByPipeline.get(j).get(pipe); ++p) {
					CSV += j + "," + pipe + "," + PublicationYearcount.get(j) + "\n";

				}
			}
		}
		new TxtFiles().WriteStringToTxtFile("PublicationYearByPipeline.csv", CSV);

	}

//https://stackoverflow.com/questions/8119366/sorting-hashmap-by-values
	public LinkedHashMap<String, Double> sortHashMapByValues(HashMap<String, Double> passedMap) {
		List<String> mapKeys = new ArrayList<>(passedMap.keySet());
		List<Double> mapValues = new ArrayList<>(passedMap.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);

		LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<>();

		Iterator<Double> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Double val = valueIt.next();
			Iterator<String> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				String key = keyIt.next();
				Double comp1 = passedMap.get(key);
				Double comp2 = val;

				if (comp1.equals(comp2)) {
					keyIt.remove();
					sortedMap.put(key, val);
					break;
				}
			}
		}
		return sortedMap;
	}

	HashMap<String, Vector<String>> FilterByCol(List<CSVRecord> list, String Col) {
		HashMap<String, Vector<String>> journals = new HashMap<String, Vector<String>>();
		for (CSVRecord record : list) {

			if (record.toMap().size() > 1) {
				if (journals.containsKey(record.get(Col))) {
					if (!journals.get(record.get(Col)).contains(record.get("ID"))) {
						journals.get(record.get(Col)).add(record.get("ID"));
					}
				} else {
					journals.put(record.get(Col), new Vector<String>());
					journals.get(record.get(Col)).add(record.get("ID"));
				}
			}
		}
		return journals;
	}

	File TrimRecords(List<CSVRecord> records, String Col, int NumOfTrimLimit, List<String> Headers, String UpdateVal)
			throws IOException {
		HashMap<String, Vector<String>> FilteredRecords = FilterByCol(records, Col);

		List<String> NewRec = new ArrayList<String>();
		for (String h : Headers)
			NewRec.add(h);

		String FileName = "Temp.csv";
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(FileName));
		CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withFirstRecordAsHeader());
		csvPrinter.printRecord(NewRec);
		for (CSVRecord rec : records) {
			if (rec.toMap().size() > 1) {
				NewRec = new ArrayList<String>();

				for (int r = 0; r < Headers.size(); ++r) {
					if (FilteredRecords.get(rec.get(Col)).size() < NumOfTrimLimit && Headers.get(r).equals(Col))

						NewRec.add(UpdateVal);
					else

						NewRec.add(rec.get(Headers.get(r)));
				}

				csvPrinter.printRecord(NewRec);
			}
		}

		csvPrinter.flush();
		csvPrinter.close();
		return new File(FileName);

	}

	HashMap<String, HashMap<String, Double>> CountPipeline(List<CSVRecord> list, String ColTitle, String ColVal,
			HashMap<String, HashMap<String, Double>> container) {
		for (CSVRecord record : list) {

			if (record.toMap().size() > 1 && record.get(ColTitle).equals(ColVal)) {

				if (container.containsKey(ColVal)) {
					HashMap<String, Double> Temp = container.get(ColVal);
					if (container.get(ColVal).containsKey(record.get("Tool"))) {

						Temp.put(record.get("Tool"), Temp.get(record.get("Tool")) + 1.0);

					} else {

						Temp.put(record.get("Tool"), 1.0);

					}
					container.put(ColVal, Temp);

				} else {
					HashMap<String, Double> Temp = new HashMap<String, Double>();
					Temp.put(record.get("Tool"), 1.0);
					container.put(ColVal, Temp);

				}
			}
		}
		return container;
	}
}
