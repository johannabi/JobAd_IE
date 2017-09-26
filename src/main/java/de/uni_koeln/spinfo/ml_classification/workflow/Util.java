package de.uni_koeln.spinfo.ml_classification.workflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.uni_koeln.spinfo.ml_classification.data.FocusClassifyUnit;
import de.uni_koeln.spinfo.ml_classification.data.MLExperimentResult;
import de.uni_koeln.spinfo.classification.core.classifier.model.Model;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.core.data.FeatureUnitConfiguration;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureReduction.Normalizer;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureReduction.Stemmer;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureReduction.StopwordFilter;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureSelection.LetterNGrammGenerator;

public class Util {

	public static void exportVectors(List<String> features, List<ClassifyUnit> classifyUnits)
			throws FileNotFoundException {

		PrintWriter pw = new PrintWriter(new File("vectors.csv"));
		StringBuilder sb = new StringBuilder();

		for (String feature : features) {
			sb.append(feature + ",");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("\n");

		for (ClassifyUnit cu : classifyUnits) {
			double[] values = cu.getFeatureVector();
			for (int i = 0; i < values.length; i++) {
				sb.append(values[i] + ",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("\n");
		}
		pw.write(sb.toString());
		pw.close();

	}

	public static void writeTXTFile(String toWrite, String path) throws IOException {

		String folder = "ml_classification/output/statistics/";
		
		File exportFolder = new File(folder);
		if(!exportFolder.isDirectory())
			exportFolder.mkdirs();
		
		File export = new File(folder + path);
		if (!export.exists())
			export.createNewFile();
		PrintWriter pw = new PrintWriter(export);
		pw.write(toWrite);
		pw.close();
		
		System.out.println("created file: " + export.getAbsolutePath());
	}

	public static void exportUnitstoXLSX(String path, Set<FocusClassifyUnit> data) throws IOException {

		String[] headRow = new String[5];
		headRow[0] = "title";
		headRow[1] = "content";
		headRow[2] = "studySubjects";
		headRow[3] = "thematicPriorities";
		headRow[4] = "degree";

		File export = new File(path);
		if (!export.exists())
			export.createNewFile();

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("TestInstances");
		int r = 0;

		Row row = sheet.createRow(r++);
		Cell cell;
		for (int i = 0; i < headRow.length; i++) {
			cell = row.createCell(i);
			cell.setCellValue(headRow[i]);
		}

		for (FocusClassifyUnit fcu : data) {

			row = sheet.createRow(r++);

			cell = row.createCell(0);
			if (!fcu.getTitle().isEmpty())
				cell.setCellValue(fcu.getTitle());
			cell = row.createCell(1);
			if (!fcu.getContentHTML().isEmpty())
				cell.setCellValue(fcu.getContentHTML());
			cell = row.createCell(2);
			if (!fcu.getStudySubjects().isEmpty())
				cell.setCellValue(createString(fcu.getStudySubjects()));
			cell = row.createCell(3);
			if (fcu.getInFocus() != null)
				cell.setCellValue(createString(fcu.getInFocus()));
			cell = row.createCell(4);
			if (!fcu.getDegrees().isEmpty())
				cell.setCellValue(createString(fcu.getDegrees()));
		}

		FileOutputStream fos = new FileOutputStream(path);
		wb.write(fos);
		wb.close();
	}

	private static String createString(Map<String, Boolean> inFocus) {
		StringBuilder toReturn = new StringBuilder();
		String comma = "";
		for (Map.Entry<String, Boolean> e : inFocus.entrySet()) {
			if (e.getValue()) {
				toReturn.append(comma + e.getKey());
				comma = ",";
			}

		}
		return toReturn.toString();
	}

	private static String createString(Set<String> studySubjects) {
		StringBuilder toReturn = new StringBuilder();
		String comma = "";
		for (String string : studySubjects) {
			toReturn.append(comma + string);
			comma = ",";
		}
		return toReturn.toString();
	}

	public static void exportModel(File file, Model model) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(model);
		fos.close();
		oos.close();
	}

	public static void exportmisclassifiedtoXLSX(String path, Map<ClassifyUnit, Map<String, Boolean>> misClassified)
			throws IOException {
		String[] headRow = new String[5];
		headRow[0] = "title";
		headRow[1] = "content";
		headRow[2] = "classified";
		headRow[3] = "gold standard";

		File export = new File(path);
		if (!export.exists())
			export.createNewFile();

		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("Misclassified");
		int r = 0;

		Row row = sheet.createRow(r++);
		Cell cell;
		for (int i = 0; i < headRow.length; i++) {
			cell = row.createCell(i);
			cell.setCellValue(headRow[i]);
		}

		for (Map.Entry<ClassifyUnit, Map<String, Boolean>> e : misClassified.entrySet()) {

			FocusClassifyUnit fcu = (FocusClassifyUnit) e.getKey();
			row = sheet.createRow(r++);

			cell = row.createCell(0);
			if (!fcu.getTitle().isEmpty())
				cell.setCellValue(fcu.getTitle());
			cell = row.createCell(1);
			if (!fcu.getContent().isEmpty())
				cell.setCellValue(fcu.getContent());
			cell = row.createCell(2);
			if (e.getValue() != null)
				cell.setCellValue(createStringRank(sortByComparator(fcu.getRanking())));
			cell = row.createCell(3);
			if (fcu.getInFocus() != null)
				cell.setCellValue(createString(fcu.getInFocus()));
		}

		FileOutputStream fos = new FileOutputStream(path);
		wb.write(fos);
		wb.close();

	}

	private static String createStringRank(List<Entry<String, Double>> list) {
		StringBuilder toReturn = new StringBuilder();
		String comma = "";
		for (Map.Entry<String, Double> e : list) {
			toReturn.append(comma + e.getKey() + ": " + e.getValue());
			comma = "\n";
		}
		return toReturn.toString();
	}

	public static List<Entry<String, Double>> sortByComparator(Map<String, Double> unsorted) {
		List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(unsorted.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});

		return list;
	}

	public static void computeStatistics(List<Double> nearestDist) {
		double mean = 0d;
		double sd = 0d;
		double max = 0;
		double min = 1;
		int n = nearestDist.size();
		System.out.println(n);

		for (int i = 0; i < n; i++) {
			Double x = nearestDist.get(i);
			// if(x < 0)
			// System.out.println(x + " - i: " + i);
			mean += x;
			if (max < x)
				max = x;
			if (min > x)
				min = x;
		}
		mean = mean / n;

		for (int i = 0; i < n; i++) {
			Double x = nearestDist.get(i);
			sd += (x - mean) * (x - mean);
		}
		sd = Math.sqrt(1.0 / (n - 1.0) * sd);

		System.out.println("Mean: " + mean);
		System.out.println("SD: " + sd);
		System.out.println("min: " + min);
		System.out.println("max: " + max);
		System.out.println("*************");
	}

	public static String writeDistribution(Map<Integer, Integer> distr, String topic, int size) {
		StringBuilder toReturn = new StringBuilder();
		int sum = 0;

		for (Map.Entry<Integer, Integer> e : distr.entrySet()) {
			toReturn.append("\t" + e.getKey() + " Labels: " + e.getValue() + " times\n");
			sum += e.getValue() * e.getKey();
		}
		double average = (double) sum / (double) size;
		toReturn.append("\tAverage: " + average + " " + topic + " per JobAd\n\n");
		return toReturn.toString();
	}

	public static Map<String, List<String>> prepareKeywords(Map<String, List<String>> keywords,
			FeatureUnitConfiguration fuc) throws IOException {
		Map<String, List<String>> toReturn = new HashMap<String, List<String>>();
		Normalizer normalizer = new Normalizer();
		StopwordFilter sw_filter = new StopwordFilter(new File("classification/data/stopwords.txt"));
		Stemmer stemmer = new Stemmer();

		for (Map.Entry<String, List<String>> e : keywords.entrySet()) {
			List<String> processed = new ArrayList<String>(e.getValue());
			if (e.getValue().isEmpty()) {
				toReturn.put(e.getKey(), processed);
				continue;
			}

			if (fuc.isNormalize())
				normalizer.normalize(processed);
			if (fuc.isFilterStopwords()) {
				List<String> filtered = sw_filter.filterStopwords(e.getValue());
				processed = new ArrayList<String>(filtered);
			}
			if (fuc.isStem()) {
				List<String> stems = stemmer.getStems(processed);
				processed = new ArrayList<String>(stems);
			}
			int[] ngrams2 = fuc.getNgrams();
			if (ngrams2 != null) {
				List<String> ngrams = new ArrayList<String>();
				for (int i : ngrams2) {
					ngrams.addAll(LetterNGrammGenerator.getNGramms(e.getValue(), i, fuc.isContinuusNGrams()));
				}
				processed = new ArrayList<String>(ngrams);
			}
			toReturn.put(e.getKey(), processed);
		}

		return toReturn;
	}

	public static Map<String, Map<String, Integer>> getBinaryCombinations(Map<Set<String>, Integer> focusCombis, List<String> focusList) {
		Map<String, Map<String, Integer>> toReturn = new HashMap<String, Map<String, Integer>>();

		for(String focus : focusList){
			Map<String, Integer> combiCount = new HashMap<String, Integer>();
			//get all Sets with focus
			for(Map.Entry<Set<String>, Integer> e : focusCombis.entrySet()){
				if(e.getKey().contains(focus)){
					Integer currFreq = e.getValue();
					for(String partner : e.getKey()){
						if(e.getKey().size() == 1){
							combiCount.put(partner, e.getValue());
							continue;
						}
						if(partner.equals(focus))
							continue;
						Integer prevFreq = 0;
						if(combiCount.containsKey(partner))
							prevFreq = combiCount.get(partner);
						combiCount.put(partner, (prevFreq + currFreq));
					}
				}
			}
			toReturn.put(focus, combiCount);
		}
		
		
		
//		for (Map.Entry<Set<String>, Integer> e : focusCombis.entrySet()) {
//			Set<String> currCombi = e.getKey();
//
//			for (String label : currCombi) {
//				Map<String, Integer> partners = new HashMap<String, Integer>();
//				// für label liegen schon kombinationen vor
//				if (toReturn.containsKey(label))
//					partners = toReturn.get(label);
//				if (currCombi.size() == 1) {
//					partners.put(label, e.getValue());
//					toReturn.put(label, partners);
//					continue;
//				}
//				for (String label2 : currCombi){
//					if(label2.equals(label))
//						continue;
//					Integer freq = 0;
//					if(partners.containsKey(label2))
//						freq = partners.get(label2);
//					partners.put(label2, (freq + e.getValue()));
//					
//				}
//			}
//		}

		return toReturn;
	}

	public static String makePrettyExperimentConf(String experimentConfiguration) {
		String classifier = null;
		String distance = null;
		String quantifier = null;
		boolean stopwords = false;
		boolean normalize = false;
		boolean suffixTree = false;
		boolean stem = false;
		int mi = 0;
		String nGram = null;
		String k = null;
		StringBuffer buff = new StringBuffer();
		String[] snippets = experimentConfiguration.split("&");
		System.out.println(experimentConfiguration);
		for (String string : snippets) {
			String[] smallSnippets = string.split("_");
			for (String smallS : smallSnippets) {
				if (smallS.equals("swFilter")) {
					stopwords = true;
				}
				if (smallS.equals("norm")) {
					normalize = true;
				}
				if (smallS.equals("stem")) {
					stem = true;
				}
				if (smallS.equals("suffixTrees")) {
					suffixTree = true;
				}
				if (smallS.startsWith("mi:")) {
					mi = Integer.parseInt(smallS.substring(3));

				}
				if (smallS.endsWith("gramms")) {
					nGram = smallS;

				}
				if (smallS.contains("Classifier")) {
					classifier = smallS.substring(0, smallS.length() - 10);
					k = smallS.replaceAll("[^\\d.]", "");
				}
				if (smallS.contains("Quantifier")) {
					quantifier = smallS.substring(0, smallS.length() - 17);
				}
				if (smallS.equals("MANHATTAN") || smallS.equals("COSINUS") || smallS.equals("EUKLID")) {
					distance = smallS;
				}
			}

		}
		if (k == null) {
			buff.append(classifier + "\t");
		} else {
			buff.append(classifier + "(" + k + ")\t");
		}
		buff.append(distance + "\t");
		buff.append(quantifier + "\t");
		buff.append(nGram + "\t");
		buff.append(mi + "\t");
		buff.append(stopwords + "\t");
		buff.append(normalize + "\t");
		buff.append(stem + "\t");
		buff.append(suffixTree + "\t");

		return buff.toString();
	}

	public static String[] getConfigurations(String configPath, int variables) throws IOException {
	
		String[] toReturn = new String[variables];
		BufferedReader br = new BufferedReader(new FileReader(configPath));
		String line;
		int i = 0;
		while((line = br.readLine()) != null && i < variables){
			int sub = line.indexOf(":");
			String value = line.substring(sub + 1, line.length());

			if(value.isEmpty())
				toReturn[i] = null;
			else
				toReturn[i] = value;

			i++;
		}
		br.close();
		
		return toReturn;
	}

	public static Row createRow(MLExperimentResult mer, Row row) {
		Cell cell;
		DecimalFormat f = new DecimalFormat("#0.0000");
		
		cell = row.createCell(0);
		cell.setCellValue(f.format(mer.getMacroAveraging().get("Macro Precision: ")));
		cell = row.createCell(1);
		cell.setCellValue(f.format(mer.getMacroAveraging().get("Macro Recall: ")));
		cell = row.createCell(2);
		cell.setCellValue(f.format(mer.getMacroAveraging().get("Macro F1: ")));
		cell = row.createCell(3);
		cell.setCellValue(f.format(mer.getMicroAveraging().get("Micro Precision: ")));
		cell = row.createCell(4);
		cell.setCellValue(f.format(mer.getMicroAveraging().get("Micro Recall: ")));
		cell = row.createCell(5);
		cell.setCellValue(f.format(mer.getMicroAveraging().get("Micro F1: ")));
		cell = row.createCell(6);
		cell.setCellValue(f.format(mer.getAverPrec()));
		cell = row.createCell(7);
		cell.setCellValue(f.format(mer.getAverRec()));
		cell = row.createCell(8);
		cell.setCellValue(f.format(mer.getAverF1()));
		cell = row.createCell(9);
		cell.setCellValue(f.format(mer.getClassificationAccuracy()));
		cell = row.createCell(10);
		cell.setCellValue(f.format(mer.getHammingLoss()));
		cell = row.createCell(11);
		cell.setCellValue(f.format(mer.getOneError()));
		cell = row.createCell(12);
		cell.setCellValue(f.format(mer.getCoverage()));
		
		String expConfig = makePrettyExperimentConf(mer.getExperimentConfiguration());
		String[] configs = expConfig.split("\t");
		for (int i = 0; i < configs.length; i++) {
			cell = row.createCell(i + 13);
			cell.setCellValue(configs[i]);
		}
//		cell = row.createCell(13);
//		cell.setCellValue("");
//		cell = row.createCell(14);
//		cell.setCellValue("");
//		cell = row.createCell(15);
//		cell.setCellValue("");
//		cell = row.createCell(16);
//		cell.setCellValue("");
//		cell = row.createCell(17);
//		cell.setCellValue("");
//		cell = row.createCell(18);
//		cell.setCellValue("");
//		cell = row.createCell(19);
//		cell.setCellValue("");
//		cell = row.createCell(20);
//		cell.setCellValue("");
//		cell = row.createCell(21);
//		cell.setCellValue("");
		return row;
	}
	


}
