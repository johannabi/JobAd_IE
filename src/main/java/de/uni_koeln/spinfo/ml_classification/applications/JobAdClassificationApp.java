package de.uni_koeln.spinfo.ml_classification.applications;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.uni_koeln.spinfo.classification.core.classifier.model.Model;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.core.data.ExperimentConfiguration;
import de.uni_koeln.spinfo.classification.core.data.FeatureUnitConfiguration;
import de.uni_koeln.spinfo.classification.core.distance.Distance;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.AbstractFeatureQuantifier;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.LogLikeliHoodFeatureQuantifier;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.TFIDFFeatureQuantifier;
import de.uni_koeln.spinfo.ml_classification.classifiers.FocusAbstractClassifier;
import de.uni_koeln.spinfo.ml_classification.classifiers.FocusMLKNNClassifier;
import de.uni_koeln.spinfo.ml_classification.classifiers.FocusNaiveBayesClassifier;
import de.uni_koeln.spinfo.ml_classification.workflow.FocusJobs;
import de.uni_koeln.spinfo.ml_classification.workflow.Util;

/**
 * application to classify focuses of new job ads. Configurations
 * for classification can be written in ml_classification/configurations.txt
 * possible configurations are listed (in german) 
 * in ml_classification/configurations_manual.pdf
 * @author Johanna
 *
 */
public class JobAdClassificationApp {

	/////////////////////////////
	// APP-CONFIGURATION
	/////////////////////////////

	static String configPath = "ml_classification/configurations.txt";
	static File inputFile;// = new
							// File("ml_classification/data/trainingSets/getIn_JobAdDB_great.xlsx");
	static String outputFolder;// = "ml_classification/output";
	static File trainingFile;// = new
								// File("ml_classification/data/trainingSets/getIn_JobAdDB_great.xlsx");
	static File focusesFile;// = new
							// File("ml_classification/data/getIn_focuses.xlsx");
	static File studiesFile = new File("ml_classification/data/studysubjects.xlsx");
	
	static File degreesFile = new File("ml_classification/data/degrees.xlsx");// TODO in config-Datei übertragen
	static boolean safeUnusedUnits = false;
	/** use serialized data (training & classified) to evaluate */
	static boolean useSavedData = false;
	////////////////////////////////////////
	/////// experiment parameters
	///////////////////////////////////////
	static boolean preClassify = false;
	static int knnValue;// = 13;
	static boolean ignoreStopwords;
	static boolean normalizeInput;
	static boolean useStemmer;
	static boolean suffixTrees = false;
	static int[] nGrams;
	static int miScoredFeaturesPerClass = 0;
	static Distance distance;
	static double threshold = 0.5;
	static boolean outputRanking;
	static boolean allowEmptyLabelMap = true;
	static FocusAbstractClassifier classifier;
	static AbstractFeatureQuantifier quantifier;
	static List<String> evaluationCategories = null;
	//////////////////////////////////////
	///////// END///
	//////////////////////////////////////

	/////////////////////////////
	// END
	/////////////////////////////

	public static void main(String[] args) throws IOException {

		try {
			initialize();

			if (!trainingFile.exists()) {
				System.out.println(trainingFile.getAbsolutePath() + "\n cannot be found");
				System.exit(0);
			}
			if (!inputFile.exists()) {
				System.out.println(inputFile.getAbsolutePath() + "\n cannot be found");
				System.exit(0);
			}
			if (!focusesFile.exists()) {
				System.out.println(focusesFile.getAbsolutePath() + "\n cannot be found");
				System.exit(0);
			}
			System.out.println("Configurations successfully initialized");
		} catch (IOException e) {
			System.err.println("Problem in intialization");
			e.printStackTrace();
		}

		FocusJobs jobs = new FocusJobs(allowEmptyLabelMap);

		FeatureUnitConfiguration fuc = new FeatureUnitConfiguration(normalizeInput, useStemmer, ignoreStopwords, nGrams,
				false, miScoredFeaturesPerClass, suffixTrees);
		ExperimentConfiguration expConfig = new ExperimentConfiguration(fuc, quantifier, classifier, inputFile,
				outputFolder);
		List<ClassifyUnit> trainingData = null;
		trainingData = jobs.getCategorizedAdsFromFile(trainingFile, expConfig.getFeatureConfiguration().isTreatEncoding(), 
				focusesFile, studiesFile, degreesFile, safeUnusedUnits);
		System.out.println("added " + trainingData.size() + " training-Ads");

		// initialize Classify Units, Features, Vectors...
		trainingData = jobs.initializeClassifyUnits(trainingData, true);
		trainingData = jobs.setFeatures(trainingData, expConfig.getFeatureConfiguration(), true);
		trainingData = jobs.setFeatureVectors(trainingData, expConfig.getFeatureQuantifier(), null);

		// build model
		Model model = jobs.getNewModelForClassifier(trainingData, expConfig);

		// get data to annotate
		List<ClassifyUnit> toAnnotate = new ArrayList<ClassifyUnit>();
		toAnnotate.addAll(jobs.getNewAdsFromFile(inputFile, expConfig.getFeatureConfiguration().isTreatEncoding()));
		System.out.println("added " + toAnnotate.size() + " new Ads to annotate");
		// initialize Classify Unity, Features, Vectors....
		toAnnotate = jobs.initializeClassifyUnits(toAnnotate, false);
		toAnnotate = jobs.setFeatures(toAnnotate, expConfig.getFeatureConfiguration(), false);
		toAnnotate = jobs.setFeatureVectors(toAnnotate, expConfig.getFeatureQuantifier(), model.getFUOrder());
		// extract study subjects
		toAnnotate = jobs.extractStudySubjects(toAnnotate, expConfig, model);
		// extract degrees
		toAnnotate = jobs.extractDegrees(toAnnotate, expConfig, model);
		// classify focuses
		Map<ClassifyUnit, Map<String, Boolean>> classifiedFocus = jobs.classify(toAnnotate, expConfig, model);

		// output
		File outputFile = new File(outputFolder + "/result.xlsx");
		jobs.exportClassifiedData(classifiedFocus, outputFile, outputRanking);
	}

	/**
	 * generates configurations from configurations file
	 * @throws IOException
	 */
	private static void initialize() throws IOException {

		String[] config = Util.getConfigurations(configPath, 15);
		inputFile = new File(config[0]);
		outputFolder = config[1];
		trainingFile = new File(config[2]);
		focusesFile = new File(config[3]);

		ignoreStopwords = config[4].equalsIgnoreCase("true");
		normalizeInput = config[5].equalsIgnoreCase("true");
		useStemmer = config[6].equalsIgnoreCase("true");
		if (config[7] != null)
			nGrams = Arrays.stream(config[7].split(",")).mapToInt(Integer::parseInt).toArray();

		if (config[8] != null) {
			if (config[8].equalsIgnoreCase("cosinus"))
				distance = Distance.COSINUS;
			else if (config[8].equalsIgnoreCase("euklid"))
				distance = Distance.EUKLID;
			else if (config[8].equalsIgnoreCase("manhattan"))
				distance = Distance.MANHATTAN;
		}
		if (config[9] != null)
			threshold = Double.parseDouble(config[9]);

		if (config[10] != null)
			knnValue = Integer.parseInt(config[10]);

		if (config[11] != null)
			allowEmptyLabelMap = config[11].equalsIgnoreCase("true");

		if (config[12].equalsIgnoreCase("ranking"))
			outputRanking = true;
		else if (config[12].equalsIgnoreCase("labels"))
			outputRanking = false;

		if (config[13].equalsIgnoreCase("NaiveBayes"))
			classifier = new FocusNaiveBayesClassifier(threshold);
		else if (config[13].equalsIgnoreCase("MLKNN"))
			classifier = new FocusMLKNNClassifier(knnValue, distance, threshold);

		if (config[14].equalsIgnoreCase("tfidf"))
			quantifier = new TFIDFFeatureQuantifier();
		else if (config[14].equalsIgnoreCase("loglikelihood"))
			quantifier = new LogLikeliHoodFeatureQuantifier();

	}
}
