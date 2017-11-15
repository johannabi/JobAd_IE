package de.uni_koeln.spinfo.ml_classification.workflow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;

import de.uni_koeln.spinfo.classification.core.classifier.model.Model;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.core.data.ExperimentConfiguration;
import de.uni_koeln.spinfo.classification.core.data.FeatureUnitConfiguration;
import de.uni_koeln.spinfo.classification.core.featureEngineering.FeatureUnitTokenizer;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureReduction.MutualInformationFilter;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureReduction.Normalizer;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureReduction.Stemmer;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureReduction.StopwordFilter;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureSelection.LetterNGrammGenerator;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureSelection.SuffixTreeFeatureGenerator;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.AbstractFeatureQuantifier;
import de.uni_koeln.spinfo.classification.core.helpers.ClassifyUnitFilter;
import de.uni_koeln.spinfo.classification.core.helpers.EncodingProblemTreatment;
import de.uni_koeln.spinfo.classification.core.helpers.crossvalidation.CrossvalidationGroupBuilder;
import de.uni_koeln.spinfo.classification.core.helpers.crossvalidation.TrainingTestSets;
import de.uni_koeln.spinfo.classification.zoneAnalysis.classifier.svm.SVMClassifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.data.ExperimentResult;
import de.uni_koeln.spinfo.ml_classification.classifiers.FocusAbstractClassifier;
import de.uni_koeln.spinfo.ml_classification.data.FocusClassifyUnit;
import de.uni_koeln.spinfo.ml_classification.data.MLExperimentResult;
import de.uni_koeln.spinfo.ml_classification.evaluation.MLEvaluator;
import de.uni_koeln.spinfo.ml_classification.extractors.AbstractExtractor;
import de.uni_koeln.spinfo.ml_classification.extractors.DegreeExtractor;
import de.uni_koeln.spinfo.ml_classification.extractors.StudyExtractor;
import de.uni_koeln.spinfo.ml_classification.preprocessing.TrainingUnitCreator;

public class FocusJobs {

	protected SuffixTreeFeatureGenerator suffixTreeBuilder;
	protected StopwordFilter sw_filter;
	protected Normalizer normalizer;
	protected Stemmer stemmer;
	protected FeatureUnitTokenizer tokenizer;
	protected Map<String, List<String>> focusKeywords, studyKeywords, degreeKeywords;
	/* Set of all Tokens in the Corpus */
	protected Set<String> allTokens = new HashSet<String>();
	private List<String> focusList, studyList, degreesList;
	MutualInformationFilter mi_filter = new MutualInformationFilter();
	private boolean allowEmptyLabelMap = true;

	public FocusJobs(boolean allowEmptyLabelMap) throws IOException {
		this.allowEmptyLabelMap = allowEmptyLabelMap;
		sw_filter = new StopwordFilter(new File("ml_classification/data/stopwords.txt"));
		normalizer = new Normalizer();
		stemmer = new Stemmer();
		tokenizer = new FeatureUnitTokenizer();
		suffixTreeBuilder = new SuffixTreeFeatureGenerator();
	}
	
	public FocusJobs() throws IOException {
		sw_filter = new StopwordFilter(new File("ml_classification/data/stopwords.txt"));
		normalizer = new Normalizer();
		stemmer = new Stemmer();
		tokenizer = new FeatureUnitTokenizer();
		suffixTreeBuilder = new SuffixTreeFeatureGenerator();
	}

	public Map<String, List<String>> getKeywords() {
		return focusKeywords;
	}

	/**
	 * creates a list of classify units from the given training data-File
	 * 
	 * @param trainingData
	 * @param treatEncoding
	 *            encodes content if true is set
	 * @param focusesFile
	 *            file that contains focuses and related keywords
	 * @param safeUnused
	 *            safes unused classify units in a file if true is set
	 * @return
	 * @throws IOException
	 */
	public List<ClassifyUnit> getCategorizedAdsFromFile(File trainingData, boolean treatEncoding, File focusesFile,
			File studiesFile, File degreesFile, Boolean safeUnused) throws IOException {

		List<FocusClassifyUnit> fcus = new ArrayList<FocusClassifyUnit>();
		List<ClassifyUnit> toReturn = new ArrayList<ClassifyUnit>();

		TrainingUnitCreator tug = new TrainingUnitCreator(trainingData, focusesFile, 
				studiesFile, degreesFile);
		fcus = tug.getTrainingData(safeUnused);


		focusKeywords = tug.getFocusKeys();
		studyKeywords = tug.getStudiesKeys();
		degreeKeywords = tug.getDegreeKeys();

		focusList = new ArrayList<String>(focusKeywords.keySet());
		studyList = new ArrayList<String>(studyKeywords.keySet());
		degreesList = new ArrayList<String>(degreeKeywords.keySet());

		//TODO hier ist studyList vollständig

		toReturn.addAll(fcus);
		if (treatEncoding) {
			for (ClassifyUnit classifyUnit : toReturn) {
				String content = classifyUnit.getContent();
				classifyUnit.setContent(EncodingProblemTreatment.normalizeEncoding(content));
			}
		}

		return toReturn;
	}

	/**
	 * initializes incoming Classify Units by creating Focus Classify Units for
	 * each and tokenizing their content
	 * 
	 * @param trainingData
	 *            Classify Units to initialize
	 * @param startNew
	 *            set true if it's a new run (important for cross validation;
	 *            set of all tokens will be initialized
	 * @return
	 */
	public List<ClassifyUnit> initializeClassifyUnits(List<ClassifyUnit> trainingData, Boolean startNew) {

		if (startNew)
			allTokens = new HashSet<String>();

		List<ClassifyUnit> toProcess = new ArrayList<ClassifyUnit>();
		/** */
		for (ClassifyUnit jobAd : trainingData) {
			//TODO warum neue objekte erzeugen??
			FocusClassifyUnit newUnit = new FocusClassifyUnit(jobAd.getContent(), jobAd.getID(), 
					((FocusClassifyUnit) jobAd).getContentHTML());
			String title = ((FocusClassifyUnit) jobAd).getTitle();
			newUnit.setTitle(title);
			newUnit.setInFocus(((FocusClassifyUnit) jobAd).getInFocus());
			newUnit.setDegrees(((FocusClassifyUnit) jobAd).getDegrees());
			newUnit.setStudySubjects(((FocusClassifyUnit) jobAd).getStudySubjects());
			List<String> tokens = tokenizer.tokenize(newUnit.getContent());
			if (tokens == null)
				continue;

			newUnit.setFeatureUnits(tokens);
			toProcess.add(newUnit);
		}

		return toProcess;
	}

	/**
	 * selects features for each ClassifyUnit according to the given FeatureUnitConfiguration
	 * @param trainingData contains all data
	 * @param fuc contains the given configuration to create features
	 * @param trainingPhase set true, if it's the training data, so the list of all tokens
	 * will be set
	 * @return ClassifyUnits with feature sets
	 */
	public List<ClassifyUnit> setFeatures(List<ClassifyUnit> trainingData, FeatureUnitConfiguration fuc,
			boolean trainingPhase) {

		for (ClassifyUnit fcu : trainingData) {
			if (fuc.isNormalize())
				normalizer.normalize(fcu.getFeatureUnits());
			if (fuc.isFilterStopwords()) {
				List<String> filtered = sw_filter.filterStopwords(fcu.getFeatureUnits());
				fcu.setFeatureUnits(filtered);
			}
			if (fuc.isStem()) {
				List<String> stems = stemmer.getStems(fcu.getFeatureUnits());
				fcu.setFeatureUnits(stems);
			}
			int[] ngrams2 = fuc.getNgrams();
			if (ngrams2 != null) {
				List<String> ngrams = new ArrayList<String>();
				for (int i : ngrams2) {
					ngrams.addAll(LetterNGrammGenerator.getNGramms(fcu.getFeatureUnits(), i, fuc.isContinuusNGrams()));
				}
				fcu.setFeatureUnits(ngrams);
			}
			if (trainingPhase)
				allTokens.addAll(fcu.getFeatureUnits());
		}

		if (fuc.getMiScore() != 0) {

			if (trainingPhase) {
				mi_filter.initialize(fuc, trainingData, focusList);
			}
			mi_filter.filter(trainingData, fuc.getMiScore());
		}
		if (fuc.isSuffixTree()) {
			trainingData = suffixTreeBuilder.getSuffixTreeFreatures(trainingData);
		}
		List<ClassifyUnit> filtered = ClassifyUnitFilter.filterByFUs(trainingData, 1);
		return filtered;

	}

	/**
	 * sets feature weighting according to the given feature quantifier
	 * @param trainingData all data the should receive a feature vector
	 * @param fq feature quantifier to weight features
	 * @param featureUnitOrder list of features that should be weighted
	 * @return ClassifyUnits that contain weighted feature vectors
	 */
	public List<ClassifyUnit> setFeatureVectors(List<ClassifyUnit> trainingData, AbstractFeatureQuantifier fq,
			List<String> featureUnitOrder) {
		if (fq != null) {
			fq.setFeatureValues(trainingData, featureUnitOrder);
		}

		return trainingData;
	}

	/**
	 * @param cus
	 *            the classify units
	 * @param expConfig
	 *            the experiment configuration
	 * @return a model for the specified experiment configuration
	 * @throws IOException
	 */
	public Model getModelForClassifier(List<ClassifyUnit> cus, ExperimentConfiguration expConfig) throws IOException {
		File modelFile = expConfig.getModelFile();
		// modelFile.createNewFile();
		Model model;
		if (expConfig.getClassifier() instanceof SVMClassifier) {
			SVMClassifier svmC = (SVMClassifier) expConfig.getClassifier();
			svmC.buildModel(expConfig, cus);
			return null;
		}

		if (!modelFile.exists()) {
			// build model...
			model = expConfig.getClassifier().buildModel(cus, expConfig.getFeatureConfiguration(),
					expConfig.getFeatureQuantifier(), expConfig.getDataFile());
			// store model
			// Util.exportModel(expConfig.getModelFile(), model);
			return model;
		} else {
			System.out.println("read model..");
			// read model...
			FileInputStream fis = new FileInputStream(modelFile);
			ObjectInputStream in = new ObjectInputStream(fis);
			try {
				Object o = in.readObject();
				model = (Model) o;
				in.close();
				return model;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				in.close();
				return null;
			}
		}
	}

	public Model getNewModelForClassifier(List<ClassifyUnit> trainingData, ExperimentConfiguration expConfig)
			throws IOException {
		Model model = expConfig.getClassifier().buildModel(trainingData, expConfig.getFeatureConfiguration(),
				expConfig.getFeatureQuantifier(), expConfig.getDataFile());

		// TODO store model
		return model;
	}

	/**
	 * creates FocusClassifyUnits for each Job Ad that should be classified
	 * @param dataToAnnotate Job Ads
	 * @param treatEncoding
	 * @return List of FocusClassifyUnit that should be classified
	 * @throws IOException
	 */
	public List<FocusClassifyUnit> getNewAdsFromFile(File dataToAnnotate, boolean treatEncoding) throws IOException {

		List<FocusClassifyUnit> toAnnotate = new ArrayList<FocusClassifyUnit>();

		FileInputStream fis = new FileInputStream(dataToAnnotate);
		XSSFWorkbook wb = new XSSFWorkbook(fis);
		XSSFSheet sheet = wb.getSheetAt(0);

		int start = 1;
		int end = sheet.getLastRowNum();

		int row = start;
		while (row <= end) {
			XSSFRow r = sheet.getRow(row);
			String title = r.getCell(0).getStringCellValue();
			String contentHTML = r.getCell(1).getStringCellValue();
			String jobAdContent = deleteHTML(contentHTML);
			toAnnotate.add(new FocusClassifyUnit(title, jobAdContent, contentHTML));
			row++;
		}
		wb.close();
		if (treatEncoding) {
			for (FocusClassifyUnit classifyUnit : toAnnotate) {
				String c = classifyUnit.getContent();
				classifyUnit.setContent(EncodingProblemTreatment.normalizeEncoding(c));
			}
		}
		return toAnnotate;
	}

	/**
	 * deletes html tags from a String
	 * @param jobAdContent
	 * @return
	 */
	private String deleteHTML(String jobAdContent) {
		String[] lines = jobAdContent.split("\n");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < lines.length; i++) {

			String line = lines[i];
			line = Jsoup.parse(lines[i]).text();
			line = line.replace("\\n", "");
			sb.append(line);
			sb.append("\n");
		}

		return sb.toString();
	}

	/**
	 * classifies the JobAds to annotate with the given model, configurations
	 * and classifier
	 * 
	 * @param toAnnotate
	 * @param expConfig
	 * @param model
	 *
	 * @return
	 */
	public Map<ClassifyUnit, Map<String, Boolean>> classify(List<ClassifyUnit> toAnnotate,
			ExperimentConfiguration expConfig, Model model) {

		FocusAbstractClassifier classifier = (FocusAbstractClassifier) expConfig.getClassifier();

		Map<ClassifyUnit, Map<String, Boolean>> classified = new HashMap<ClassifyUnit, Map<String, Boolean>>();
		for (ClassifyUnit cu : toAnnotate) {

			FocusClassifyUnit fcu = ((FocusClassifyUnit) cu);

			Map<String, Boolean> focusesClassified = ((FocusAbstractClassifier) classifier).classify(fcu, model,
					focusList, allTokens);

			if (!focusesClassified.containsValue(true) && !allowEmptyLabelMap) {
				// take most possible label as true
				List<Entry<String, Double>> sorted = Util.sortByComparator(fcu.getRanking());
				String mostPossible = sorted.get(0).getKey();
				focusesClassified.put(mostPossible, true);
			}
			classified.put(fcu, focusesClassified);
		}

		return classified;
	}
	
	public List<ClassifyUnit> extractStudySubjects(List<ClassifyUnit> toAnnotate,
			ExperimentConfiguration expConfig, Model model) throws IOException{
		
		AbstractExtractor extractor = new StudyExtractor(studyKeywords);
		
//		Map<ClassifyUnit, Map<String, Boolean>> extracted = new HashMap<ClassifyUnit, Map<String, Boolean>>();
		
		
		for (ClassifyUnit classifyUnit : toAnnotate) {
			
			FocusClassifyUnit fcu = ((FocusClassifyUnit) classifyUnit);
			
			Map<String, Boolean> extractedStudies = extractor.extract(fcu);
//			extracted.put(fcu, extractedStudies);
			fcu.setExtractedStudies(extractedStudies);
			
		}
		
		return toAnnotate;
	}
	
	public List<ClassifyUnit> extractDegrees(List<ClassifyUnit> toAnnotate,
			ExperimentConfiguration expConfig, Model model) throws IOException{
		
		AbstractExtractor extractor = new DegreeExtractor(degreeKeywords);
		
		for (ClassifyUnit classifyUnit : toAnnotate){
			FocusClassifyUnit fcu = ((FocusClassifyUnit) classifyUnit);
			
			Map<String, Boolean> extractedDegrees = extractor.extract(fcu);
			fcu.setExtractedDegrees(extractedDegrees);
		}
		
		
		return toAnnotate;
	}

	/**
	 * exports classified Data to .xlsx-File
	 * @param classifiedFocus
	 * @param outputFile
	 * @param outputRanking set true, if ranking vector should be written, set
	 *  false if definite labels should be written
	 * @throws IOException
	 */
	public void exportClassifiedData(Map<ClassifyUnit, Map<String, Boolean>> classifiedFocus, File outputFile,
			boolean outputRanking) throws IOException {

		String[] headRow = new String[5];
		headRow[0] = "title";
		headRow[1] = "content";
		headRow[2] = "studySubjects";
		headRow[3] = "thematicPriorities";
		headRow[4] = "degree";

		FileOutputStream fos = new FileOutputStream(outputFile);
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("classified");

		int row = 0;
		// kopfzeile
		XSSFRow r = sheet.createRow(row++);
		XSSFCell cell;
		for (int i = 0; i < headRow.length; i++) {
			cell = r.createCell(i);
			cell.setCellValue(headRow[i]);
		}

		for (Map.Entry<ClassifyUnit, Map<String, Boolean>> e : classifiedFocus.entrySet()) {

			FocusClassifyUnit fcu = ((FocusClassifyUnit) e.getKey());
			Map<String, Boolean> focusesMap = e.getValue();
			Map<String, Boolean> studiesMap = fcu.getExtractedStudies();
			Map<String, Boolean> degreesMap = fcu.getExtractedDegrees();

			String classified = "";
			if (outputRanking)
				classified = getRanking(fcu.getRanking());
			else
				classified = getFocuses(focusesMap);
			
			String studies = getFocuses(studiesMap);
			String degrees = getFocuses(degreesMap);
			

			r = sheet.createRow(row);
			// title
			cell = r.createCell(0);
			cell.setCellValue(fcu.getTitle());
			// content with html
			cell = r.createCell(1);
			cell.setCellValue(fcu.getContentHTML());
			// studies extracted
			cell = r.createCell(2);
			cell.setCellValue(studies);
			// focus classified
			cell = r.createCell(3);
			cell.setCellValue(classified);
			row++;
			// degrees extracted
			cell = r.createCell(4);
			cell.setCellValue(degrees);
		}

		wb.write(fos);
		fos.close();
		wb.close();

		System.out.println("Generated output file: " + outputFile.getAbsolutePath());
	}

	private String getRanking(Map<String, Double> ranking) {
		StringBuffer sb = new StringBuffer();
		DecimalFormat f = new DecimalFormat("#0.0000");
		List<Entry<String, Double>> rankedLabels = Util.sortByComparator(ranking);
		for (Map.Entry<String, Double> e : rankedLabels) {
			sb.append(e.getKey());
			if (Double.isNaN(e.getValue()))
				sb.append(": " + 0.0 + "\n");
			else
				sb.append(": " + f.format(e.getValue()) + "\n");
		}
		return sb.toString();
	}

	private String getFocuses(Map<String, Boolean> keyFocus) {
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, Boolean> e : keyFocus.entrySet()) {
			if (e.getValue()) {
				sb.append(e.getKey());
				sb.append(",");
			}
		}
		if (sb.length() > 1) {
			sb.deleteCharAt(sb.length() - 1); // delete last comma
			return sb.toString();
		} else
			return "";
	}

	/**
	 * cross validates the given experiment configurations with the given ClassifyUnits
	 * Number of cross-validation groups is 10
	 * @param paragraphs
	 * @param expConfig
	 * @return
	 * @throws IOException
	 */
	public Map<ClassifyUnit, Map<String, Boolean>> crossvalidate(List<ClassifyUnit> paragraphs,
			ExperimentConfiguration expConfig) throws IOException {
		// build crossvalidationgroups...
		int numberOfCrossValidGroups = 10;
		CrossvalidationGroupBuilder<ClassifyUnit> cvgb = new CrossvalidationGroupBuilder<ClassifyUnit>(paragraphs,
				numberOfCrossValidGroups);
		Iterator<TrainingTestSets<ClassifyUnit>> iterator = cvgb.iterator();

		// classify..
		Map<ClassifyUnit, Map<String, Boolean>> classified = new HashMap<ClassifyUnit, Map<String, Boolean>>();

		while (iterator.hasNext()) {
			// System.out.println("CrossValidation: ");
			TrainingTestSets<ClassifyUnit> testSets = iterator.next();

			List<ClassifyUnit> trainingSet = testSets.getTrainingSet();
			List<ClassifyUnit> testSet = testSets.getTestSet();
			// System.out.println("Get Model...");
			Model model = getModelForClassifier(trainingSet, expConfig);
			
//			System.out.println("Extract Study Subjects...");
			testSet = extractStudySubjects(testSet, expConfig, model);
			
//			System.out.println("Extract Degrees...");
			testSet = extractDegrees(testSet, expConfig, model);

//			 System.out.println("Classify...");
			classified.putAll(classify(testSet, expConfig, model));
		}

		return classified;
	}

	/**
	 * evaluates the classified Job Ads with the given categories
	 * @param classified
	 * @param categories
	 * @param expConfig
	 * @return
	 */
	public Map<String,MLExperimentResult> evaluateML(Map<ClassifyUnit, Map<String, Boolean>> classified, List<String> categories,
			ExperimentConfiguration expConfig) {
		
		MLEvaluator evaluator = new MLEvaluator();
//		FocusEvaluator evaluator = new FocusEvaluator();
		Map<String, MLExperimentResult> results = evaluator.evaluate(classified, expConfig, categories, 
				focusList, studyList, degreesList);

//		MLExperimentResult er = new MLExperimentResult();
//		er.setExperimentConfiguration(expConfig.toString());
//		er.setHammingLoss(evaluator.getHammingLoss());
//		er.setMaxLoss(evaluator.getMaxLoss());
//		er.setOneError(evaluator.getOneError());
//		er.setCoverage(evaluator.getCoverage());
//		// er.setTotalCorrect(evaluator.getTotalCorrect());
//		er.setNumberOfEvalData(classified.size());
//		er.setAccuracy(evaluator.getOverallAccuracy());
//		er.setF1Measure(evaluator.getOverallF1Score());
//		er.setPrecision(evaluator.getOverallPrecision());
//		er.setAverPrec(evaluator.getAverPrec());
//		er.setAverRec(evaluator.getAverRec());
//		er.setAverF1(evaluator.getAverF1());
//		er.setClassificationAccuracy(evaluator.getClassAccuracy());
//		er.setRecall(evaluator.getOverallRecall());
//		er.setTN(evaluator.getOverallTNs());
//		er.setTP(evaluator.getOverallTPs());
//		er.setFN(evaluator.getOverallFNs());
//		er.setFP(evaluator.getOverallFPs());
//		er.setMisClassified(evaluator.getMisclassified());
//		er.setMLCategoryEvaluations(evaluator.getCategoryEvaluations());
		return results;
	}

	/**
	 * serializes the given ExperimentResults
	 * @param ers
	 * @param outputFolder
	 * @return
	 * @throws IOException
	 */
	public File persistExperimentResults(List<ExperimentResult> ers, File outputFolder) throws IOException {
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}
		StringBuffer sb1 = new StringBuffer();
		for (ExperimentResult experimentResult : ers) {
			sb1.append(experimentResult.getExperimentConfiguration()
					+ ((MLExperimentResult) experimentResult).getHammingLoss() + experimentResult.getID());
		}
		int hash = sb1.toString().hashCode();
		String objectFileName = null;
		StringBuffer sb = new StringBuffer();
		sb.append(outputFolder.getAbsolutePath() + "\\");
		sb.append(ers.size() + "_Results_" + hash);
		// String expconf = ers.get(0).getExperimentConfiguration();
		// String withoutFUConf = expconf.substring(expconf.indexOf('&')+1);
		// sb.append(withoutFUConf);
		// sb.append("_"+ers.size()+"_experiments");
		objectFileName = sb.toString();
		File file = new File(objectFileName + ".bin");
		if (!file.exists()) {
			file.createNewFile();
		}

		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream out = new ObjectOutputStream(fos);
		for (ExperimentResult experimentResult : ers) {
			out.writeObject(experimentResult);
		}
		out.flush();
		out.close();
		System.out.println("**************************************___WRITTEN___: " + objectFileName
				+ ".bin___***************************************************");
		return file;

	}

	/**
	 * serializes the given ExperimentResult
	 * @param er
	 * @param outputFolder
	 * @return
	 * @throws IOException
	 */
	public File persistExperimentResult(ExperimentResult er, File outputFolder) throws IOException {
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}
		String objectFileName = null;
		StringBuffer sb = new StringBuffer();
		sb.append(outputFolder.getAbsolutePath() + "\\");
		sb.append("singleResult");
		sb.append(er.getExperimentConfiguration().replaceAll("[:\\-]", "_"));
		objectFileName = sb.toString();
		File file = new File(objectFileName + ".bin");
		if (!file.exists()) {
			file.createNewFile();
		}

		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream out = new ObjectOutputStream(fos);

		out.writeObject(er);
		out.flush();
		out.close();
		System.out.println("**************************************___WRITTEN: " + objectFileName
				+ ".bin___***************************************************");
		return file;
	}

	/**
	 * combines the result of pre-classifier and classifier
	 * @param classified
	 * @param preClassified
	 * @return
	 */
	public Map<ClassifyUnit, Map<String, Boolean>> mergeResults(Map<ClassifyUnit, Map<String, Boolean>> classified,
			Map<ClassifyUnit, Map<String, Double>> preClassified) {
		// TODO improve....
		Map<ClassifyUnit, Map<String, Boolean>> toReturn = new HashMap<ClassifyUnit, Map<String, Boolean>>();

		for (Map.Entry<ClassifyUnit, Map<String, Boolean>> e : classified.entrySet()) {
			ClassifyUnit cu = e.getKey();
			Map<String, Boolean> clFocus = classified.get(cu);
			Map<String, Double> prclFocus = preClassified.get(cu);
			Map<String, Boolean> merged = new HashMap<String, Boolean>();
			List<Double> list = Arrays.asList(ArrayUtils.toObject(cu.getFeatureVector()));
			Double max = Collections.max(list);
			Double min = Collections.min(list);
			System.out.println("Max: " + max + " Min: " + min);
			for (Map.Entry<String, Boolean> f : clFocus.entrySet()) {
				Boolean cl = f.getValue();
				Double rank = prclFocus.get(f.getKey());
				if (rank < 0.0) {
					merged.put(f.getKey(), f.getValue());
					continue;
				}

				if (cl || (rank > (0.8 * max)))
					merged.put(f.getKey(), true);
				else
					merged.put(f.getKey(), false);
			}

			// iterates over classified and compares with pre-classified
			// for (Map.Entry<String, Boolean> f : clFocus.entrySet()) {
			// String focus = f.getKey();
			// if (f.getValue().equals(prclFocus.get(focus)))
			// merged.put(f.getKey(), f.getValue());
			// else if (f.getValue() && !prclFocus.get(focus))
			// merged.put(f.getKey(), true);
			// else if (!f.getValue() && prclFocus.get(focus))
			// merged.put(f.getKey(), true);
			// }
			toReturn.put(cu, merged);
		}

		return toReturn;
	}

	/**
	 * analyzes the trainingData in terms of label distributions
	 * and label combinations.
	 * 
	 * @param data
	 * @return String with results
	 */
	public String analyzeData(List<ClassifyUnit> data) {
		StringBuilder result = new StringBuilder();
		/** frequency distribution of focuses per classify unit */
		Map<Integer, Integer> freqDistrFocus = new HashMap<Integer, Integer>();
		/** frequency distribution of study subjects per classify unit */
		Map<Integer, Integer> freqDistrStudy = new HashMap<Integer, Integer>();
		/** frequency distribution of degrees per classify unit */
		Map<Integer, Integer> freqDistrDegree = new HashMap<Integer, Integer>();

		/** frequency of specific combinations of focuses */
		Map<Set<String>, Integer> focusCombis = new HashMap<Set<String>, Integer>();
		Map<String, Integer> focusFreq = new HashMap<String, Integer>();

		// compute distributions
		for (ClassifyUnit cu : data) {
			FocusClassifyUnit fcu = (FocusClassifyUnit) cu;
			Map<String, Boolean> inFocus = fcu.getInFocus();
			Integer studyCount = fcu.getStudySubjects().size();
			Integer degreeCount = fcu.getDegrees().size();

			Integer freq = 0;
			if (freqDistrStudy.containsKey(studyCount))
				freq = freqDistrStudy.get(studyCount);
			freqDistrStudy.put(studyCount, freq + 1);

			freq = 0;
			if (freqDistrDegree.containsKey(degreeCount))
				freq = freqDistrDegree.get(degreeCount);
			freqDistrDegree.put(degreeCount, freq + 1);

			freq = 0;
			Integer focusCount = 0;
			Set<String> currentCombi = new HashSet<String>();
			for (Map.Entry<String, Boolean> e : inFocus.entrySet()) {
				if (e.getValue()) {
					focusCount++;
					currentCombi.add(e.getKey());
					freq = 0;
					if (focusFreq.containsKey(e.getKey()))
						freq = focusFreq.get(e.getKey());
					focusFreq.put(e.getKey(), freq + 1);
				}
			}

			freq = 0;
			if (freqDistrFocus.containsKey(focusCount))
				freq = freqDistrFocus.get(focusCount);
			freqDistrFocus.put(focusCount, freq + 1);

			freq = 0;
			if (focusCombis.containsKey(currentCombi))
				freq = focusCombis.get(currentCombi);
			focusCombis.put(currentCombi, freq + 1);

		}

		// compute means
		result.append("Frequency Distribution of Focuses: \n");
		result.append(Util.writeDistribution(freqDistrFocus, "Focuses", data.size()));

		result.append("Frequency Distribution of Study Subjects: \n");
		result.append(Util.writeDistribution(freqDistrStudy, "Study Subjects", data.size()));

		result.append("Frequency Distribution of Degrees: \n");
		result.append(Util.writeDistribution(freqDistrDegree, "Degrees", data.size()));

		result.append("--------------------------------\n");

		// build frequency of each focus
		List<Entry<String, Integer>> focuses = new LinkedList<Entry<String, Integer>>(focusFreq.entrySet());
		Collections.sort(focuses, new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}

		});
		result.append("Frequencies of Focuses: \n");
		for (Map.Entry<String, Integer> e : focuses) {
			result.append("\t" + e.getKey() + ": " + e.getValue() + " times\n");
		}
		result.append("\n\n----------------------------------\n");

		// build focus combis
		List<Entry<Set<String>, Integer>> list = new LinkedList<Entry<Set<String>, Integer>>(focusCombis.entrySet());
		Collections.sort(list, new Comparator<Entry<Set<String>, Integer>>() {

			@Override
			public int compare(Entry<Set<String>, Integer> o1, Entry<Set<String>, Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}

		});

		result.append("Frequency Distribution of Focus Combinations (" + list.size() + " different Combinations):\n");
		for (Map.Entry<Set<String>, Integer> e : list) {
			result.append("\t" + e.getKey() + ": " + e.getValue() + " times\n");
		}
		result.append("\n\n----------------------------------\n");
		Map<String, Map<String, Integer>> binaryCombis = Util.getBinaryCombinations(focusCombis, focusList);

		for (Map.Entry<String, Map<String, Integer>> e : binaryCombis.entrySet()) {
			result.append(e.getKey() + " combined with:\n");
			for (Map.Entry<String, Integer> d : e.getValue().entrySet()) {
				result.append(d.getKey() + ": " + d.getValue() + " times\n");
			}
			result.append("********************************\n");
		}

		return result.toString();
	}

	/**
	 * exports evaluation results to .xlsx-File
	 * @param results
	 * @param folder
	 * @throws IOException
	 */
	public void exportResults(List<ExperimentResult> results, File folder) throws IOException {

		String[] headRow = new String[21];
		headRow[0] = "macroPre";
		headRow[1] = "macroRec";
		headRow[2] = "macroFme";
		headRow[3] = "microPre";
		headRow[4] = "microRec";
		headRow[5] = "microFme";
		headRow[6] = "averPre";
		headRow[7] = "averRec";
		headRow[8] = "averFme";
		headRow[9] = "classAcc";
		headRow[10] = "hloss";
		headRow[11] = "oer";
		headRow[12] = "cov";
		
		headRow[13] = "Classifier";
		headRow[14] = "Distance";
		headRow[15] = "Quantifier";
		headRow[16] = "NGrams";
		headRow[17] = "Mi-Score";
		headRow[18] = "NoStopwords";
		headRow[19] = "Normalized";
		headRow[20] = "Stemmed";
		

		if (!folder.exists()) {
			folder.mkdirs();
		}
		String fileName = folder.getAbsolutePath() + "/" + results.hashCode() + ".xlsx";

		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet("Misclassified");
		int r = 0;

		Row row = sheet.createRow(r++);
		Cell cell;
		for (int i = 0; i < headRow.length; i++) {
			cell = row.createCell(i);
			cell.setCellValue(headRow[i]);
		}


		for (ExperimentResult result : results) {
			MLExperimentResult mer = (MLExperimentResult) result;
			row = sheet.createRow(r++);
			
			row = Util.createRow(mer, row);

		}
		FileOutputStream fos = new FileOutputStream(fileName);
		wb.write(fos);
		wb.close();

	}

}
