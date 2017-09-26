package de.uni_koeln.spinfo.ml_classification.preprocessing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;

import de.uni_koeln.spinfo.ml_classification.data.FocusClassifyUnit;
import de.uni_koeln.spinfo.ml_classification.workflow.Util;

public class TrainingUnitCreator {

	private File trainingFile, focusesFile;
	private List<FocusClassifyUnit> classifiedData = new ArrayList<FocusClassifyUnit>();
	private Map<String, List<String>> focusKeys;
	// private int numberOfCategories = 0;

	public TrainingUnitCreator(File trainingFile, File focusesFile) {
		this.trainingFile = trainingFile;
		// this.classifiedData
		this.focusesFile = focusesFile;
		// this.numberOfCategories = categories;
	}

	/**
	 * Map with focus-Strings and keywords. Value might be empty (not null!) if
	 * there are no keywords
	 * 
	 * @return
	 */
	public Map<String, List<String>> getFocusKeys() {
		return focusKeys;
	}

	/**
	 * creates a FocusClassifyUnit for every JobAd in .xslx-Document. Sets
	 * content and Boolean whether or not the JobAd belongs to a focus
	 * 
	 * @return annotated JobAds
	 * @throws IOException
	 */
	public List<FocusClassifyUnit> getTrainingData(Boolean safeUnused) throws IOException {

		setFocusKeys(focusesFile);

		if (classifiedData.isEmpty()) {

			FileInputStream fis = new FileInputStream(trainingFile);
			XSSFWorkbook wb = new XSSFWorkbook(fis);
			XSSFSheet sheet = wb.getSheetAt(0);
			Set<FocusClassifyUnit> noFocus = new HashSet<FocusClassifyUnit>();

			int row = 1;
			while (row <= sheet.getLastRowNum()) {
				XSSFRow r = sheet.getRow(row);
				String title = r.getCell(0).getStringCellValue();
				String contentHTML = r.getCell(1).getStringCellValue();
				String titelContentHTML = title + "\n" + contentHTML;
				String titleContent = deleteHTML(titelContentHTML);
				String studySubjectsString = r.getCell(2).getStringCellValue();
				String focusString = r.getCell(3).getStringCellValue();
				String degreesString = r.getCell(4).getStringCellValue();
				FocusClassifyUnit fcu = new FocusClassifyUnit(title, titleContent, contentHTML);

				Set<String> posFocuses = splitIntoPieces(focusString);
				Set<String> studySubjects = splitIntoPieces(studySubjectsString);
				Set<String> degrees = splitIntoPieces(degreesString);

				Map<String, Boolean> inFocus = new HashMap<String, Boolean>();
				if (!posFocuses.isEmpty()) {
					// creates Map of focuses the unit belongs to (or not)
					for (String focus : focusKeys.keySet()) {
						if (posFocuses.contains(focus))
							inFocus.put(focus, new Boolean(true));
						else
							inFocus.put(focus, new Boolean(false));
					}
					// noFocus.add(fcu);
					// row++;
					// continue;
				}
				fcu.setStudySubjects(studySubjects);
				fcu.setDegrees(degrees);
				if (!inFocus.containsValue(true)) {
					// export Data for test instances
					noFocus.add(fcu);

				} else {
					fcu.setInFocus(inFocus);
					classifiedData.add(fcu);
				}
				row++;

			}
			wb.close();
			if(safeUnused)
				Util.exportUnitstoXLSX("notUsedData.xlsx", noFocus);

		}
		return classifiedData;

	}

	private void setFocusKeys(File foFile) throws IOException {
		focusKeys = new HashMap<String, List<String>>();

		FileInputStream fis = new FileInputStream(foFile);
		XSSFWorkbook wb = new XSSFWorkbook(fis);
		XSSFSheet sheet = wb.getSheetAt(0);

		int row = 1;

		// System.out.println(sheet.getLastRowNum());
		while (row <= sheet.getLastRowNum()) {
			XSSFRow r = sheet.getRow(row);
			String focus = r.getCell(0).getStringCellValue();
			if (focus.startsWith("*")) {
				row++;
				continue;
			}
			focus = focus.replaceAll(" / ", "");
			focus = focus.replaceAll(" ", "");
			focus = focus.replaceAll("&", "");

			XSSFCell c = r.getCell(1);
			List<String> keys = new ArrayList<String>();
			if (c != null) {
				String keywordsString = c.getStringCellValue();
				String[] keywords = keywordsString.split(",");
				for (int i = 0; i < keywords.length; i++) {
					String k = keywords[i].trim().toLowerCase();
					keys.add(k);
				}
			}
			focusKeys.put(focus, keys);
			row++;
		}

		wb.close();
	}

	private String deleteHTML(String jobAdContent) {
		String[] lines = jobAdContent.split("\n");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < lines.length; i++) {
			String line = Jsoup.parse(lines[i]).text();
			line = line.replace("\\n", "");
			sb.append(line);
			sb.append("\n");
		}
		return sb.toString();
	}

	private Set<String> splitIntoPieces(String wholeString) {
		Set<String> pieces = new HashSet<String>(Arrays.asList(wholeString.split(",")));
		Set<String> toReturn = new HashSet<String>();
		for (String string : pieces) {

			string = string.replaceAll(" / ", "");
			string = string.replaceAll(" ", "");
			string = string.replaceAll("&", "");

			if (!string.isEmpty())
				toReturn.add(string);
		}

		return toReturn;
	}

}
