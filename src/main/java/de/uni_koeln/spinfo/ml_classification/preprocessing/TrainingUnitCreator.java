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

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;

import de.uni_koeln.spinfo.ml_classification.data.FocusClassifyUnit;
import de.uni_koeln.spinfo.ml_classification.workflow.Util;

public class TrainingUnitCreator {

	private File trainingFile, focusesFile, studiesFile, degreesFile;
	private List<FocusClassifyUnit> classifiedData = new ArrayList<FocusClassifyUnit>();
	private Map<String, List<String>> focusKeys, studiesKeys, degreesKeys;
	// private int numberOfCategories = 0;

	public TrainingUnitCreator(File trainingFile, File focusesFile, 
			File studiesFile, File degreesFile) {
		this.trainingFile = trainingFile;
		// this.classifiedData
		this.focusesFile = focusesFile;
		// this.numberOfCategories = categories;
		this.studiesFile = studiesFile;
		this.degreesFile = degreesFile;
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
	 * Map with study subjects-Strings and keywords. Value might be empty (not
	 * null!) if there are no keywords
	 * 
	 * @return
	 */
	public Map<String, List<String>> getStudiesKeys() {
		return studiesKeys;
	}
	
	/**
	 * Map with degree-Strings and keywords. Value might be empty (not null!) if
	 * there are no keywords
	 * 
	 * @return
	 */
	public Map<String, List<String>> getDegreeKeys() {
		return degreesKeys;
	}

	/**
	 * creates a FocusClassifyUnit for every JobAd in .xslx-Document. Sets
	 * content and Boolean whether or not the JobAd belongs to a focus
	 * 
	 * @return annotated JobAds
	 * @throws IOException
	 */
	public List<FocusClassifyUnit> getTrainingData(Boolean safeUnused) throws IOException {

		focusKeys = setKeys(focusesFile);
		studiesKeys = setKeys(studiesFile);
		degreesKeys = setKeys(degreesFile);

		if (classifiedData.isEmpty()) {
			System.out.println("Read new Data");
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

				
				
				Set<String> inFocusSet = splitIntoPieces(focusString);
				Set<String> studiesSet = splitIntoPieces(studySubjectsString);
				Set<String> degreesSet = splitIntoPieces(degreesString);

//				System.out.println(studySubjects);
//				System.out.println(studiesKeys.keySet());
				//TODO hier sind studieskeys und studysubjects noch groß geschrieben
				Map<String, Boolean> inFocus = setLabels(inFocusSet, focusKeys.keySet());
				Map<String, Boolean> studies = setLabels(studiesSet, studiesKeys.keySet());
				Map<String, Boolean> degrees = setLabels(degreesSet, degreesKeys.keySet());

				fcu.setStudySubjects(studies);
				fcu.setDegrees(degrees);
				

				if (!inFocus.containsValue(true)) { // TODO was, wenn Focus,
													// aber keine Studienfächer?
					// export Data for test instances
					noFocus.add(fcu);

				} else {
					
					fcu.setInFocus(inFocus);
					classifiedData.add(fcu);
				}
				row++;

			}
			wb.close();
			if (safeUnused)
				Util.exportUnitstoXLSX("notUsedData.xlsx", noFocus);

		}
		
		return classifiedData;

	}

	private Map<String, List<String>> setKeys(File foFile) throws IOException {
		Map<String, List<String>> toReturn = new HashMap<String, List<String>>();

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
					String k = keywords[i].trim().toLowerCase();//.toLowerCase();		
					k = k.replace("++", "[\\-\\s\\),A-Za-zäöüÄÖÜß]*");
					keys.add(k);
				}
			}
			toReturn.put(focus, keys);
			row++;
		}

		wb.close();

		
		return toReturn;
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

	private Map<String, Boolean> setLabels(Set<String> posLabels, Set<String> allLabels) {
		Map<String, Boolean> labels = new HashMap<String, Boolean>();
		
		// if (!posLabels.isEmpty()) {
		// creates Map of focuses the unit belongs to (or not)
		for (String focus : allLabels) {
			if (posLabels.contains(focus))
				labels.put(focus, new Boolean(true));
			else
				labels.put(focus, new Boolean(false));
		}
		// }
		return labels;
	}



}
