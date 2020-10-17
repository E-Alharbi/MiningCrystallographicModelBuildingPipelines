package MCMBP.Utilities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

public class CSVFilter {

	public List<CSVRecord> FilterCSV(Vector<String> Values, HashMap<String, Integer> HeadersWithIndex,
			List<CSVRecord> list, String Col) {
		Vector<String> tempValues = new Vector<String>();
		for (String val : Values) {

			tempValues.add(val.trim()); // remove spaces
		}
		Values.addAll(tempValues);
		List<CSVRecord> temp = new ArrayList<CSVRecord>();

		for (CSVRecord record : list) {
			String val = record.get(HeadersWithIndex.get(Col));

			if (Values.contains(val.trim())) {

				temp.add(record);
			}
		}
		return temp;

	}

	public Vector<String> SplitKeywordVal(String v) {

		Vector<String> Values = new Vector<String>();
		if (v.contains(",")) {
			String[] SplitedValues = v.split(",");

			for (int i = 0; i < SplitedValues.length; ++i) {
				Values.add(SplitedValues[i]);
			}
		} else if (v.contains("-") && StringUtils.isNumeric(v.replaceAll("-", "")) == true) {
			String Start = v.split("-")[0];
			String End = v.split("-")[1];

			for (double i = Double.parseDouble(Start); i <= Double.parseDouble(End); i = i + 0.01) {

				boolean Added = false;
				for (int frac = 0; frac < 10; ++frac) {
					if (new BigDecimal(i).setScale(2, RoundingMode.HALF_UP).remainder(BigDecimal.ONE)
							.compareTo(new BigDecimal("0." + frac + "0")) == 0) {// if 2019.00 convert to 2019
						if (frac == 0)
							Values.add(String.valueOf(new BigDecimal(i).setScale(0, RoundingMode.HALF_UP)));

						Values.add(String.valueOf(new BigDecimal(i).setScale(1, RoundingMode.HALF_UP)));

						Added = true;
					}
				}
				if (Added == false)
					Values.add(String.valueOf(new BigDecimal(i).setScale(2, RoundingMode.HALF_UP)));
			}
		}

		else {
			Values.add(v);
		}
		return Values;
	}
}
