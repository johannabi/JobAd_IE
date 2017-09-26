package de.uni_koeln.spinfo.ml_classification.applications;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.core.data.ExperimentConfiguration;
import de.uni_koeln.spinfo.classification.core.data.FeatureUnitConfiguration;
import de.uni_koeln.spinfo.classification.core.distance.Distance;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.AbstractFeatureQuantifier;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.LogLikeliHoodFeatureQuantifier;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.TFIDFFeatureQuantifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.data.CategoryResult;
import de.uni_koeln.spinfo.classification.zoneAnalysis.data.ExperimentResult;
import de.uni_koeln.spinfo.ml_classification.classifiers.FocusAbstractClassifier;
import de.uni_koeln.spinfo.ml_classification.classifiers.FocusMLKNNClassifier;
import de.uni_koeln.spinfo.ml_classification.classifiers.FocusNaiveBayesClassifier;
import de.uni_koeln.spinfo.ml_classification.data.MLCategoryResult;
import de.uni_koeln.spinfo.ml_classification.data.MLExperimentResult;
import de.uni_koeln.spinfo.ml_classification.workflow.FocusJobs;
import de.uni_koeln.spinfo.ml_classification.workflow.FocusSingleExperimentExecutor;

/**
 * evaluates the multilabel classification with the given configurations
 * @author Johanna
 *
 */
public class SingleExperimentExecution {
	/////////////////////////////
	// APP-CONFIGURATION
	/////////////////////////////

	static File inputFile = new File("ml_classification/data/trainingSets/getIn_JobAdDB_great.xlsx");
	static String outputFolder = "ml_classification/output";
	static File focusesFile = new File("ml_classification/data/getIn_focuses.xlsx");
	static boolean safeUnusedUnits = false;
	/** use serialized data (training & classified) to evaluate */
	static boolean useSavedData = false;
	////////////////////////////////////////
	/////// experiment parameters
	///////////////////////////////////////
	static boolean preClassify = false;
	static File resultOutputFolder = new File("ml_classification/output/evaluation/singleResults");
	static int knnValue = 16;
	static boolean ignoreStopwords = true;
	static boolean normalizeInput = true;
	static boolean useStemmer = true;
	static boolean suffixTrees = false;
	static int[] nGrams = {3}; 
	static int miScoredFeaturesPerClass = 0;
	static Distance distance = Distance.COSINUS;
	static double threshold = 0.5;
	static boolean allowEmptyLabelMap = true;
	static FocusAbstractClassifier classifier = new FocusNaiveBayesClassifier(threshold);
//	static FocusAbstractClassifier classifier = new FocusMLKNNClassifier(knnValue, distance, threshold);
	static AbstractFeatureQuantifier quantifier = new LogLikeliHoodFeatureQuantifier();
	static List<String> evaluationCategories = null;
	//////////////////////////////////////
	///////// END///
	//////////////////////////////////////

	/////////////////////////////
	// END
	/////////////////////////////

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		
		FocusJobs jobs = new FocusJobs(allowEmptyLabelMap);
		MLExperimentResult result = new MLExperimentResult();
		
//		evaluationCategories = new ArrayList<String>();
//		evaluationCategories.add("Administration");
//		evaluationCategories.add("BeratungConsulting");
//		evaluationCategories.add("RiskComplianceManagement");
//		evaluationCategories.add("DatenbankentwicklungBI");
//		evaluationCategories.add("QualityAssurance");
//		evaluationCategories.add("Webentwicklung");
//		evaluationCategories.add("Anwendungsentwicklung");
//		evaluationCategories.add("Projektmanagement");


		if(useSavedData){
			System.out.println("Deserialisierung...");
			File c = new File("classified.ser");
			File exp = new File("expConfig.ser");
			
			FileInputStream fis = new FileInputStream(c);
			ObjectInputStream ois = new ObjectInputStream(fis);
			Map<ClassifyUnit, Map<String, Boolean>> classified = (Map<ClassifyUnit, Map<String, Boolean>>) ois.readObject();
			fis = new FileInputStream(exp);
			ois = new ObjectInputStream(fis);
			ExperimentConfiguration expConfig = (ExperimentConfiguration) ois.readObject();
			
			ois.close();
			
			result = jobs.evaluateML(classified, evaluationCategories, expConfig);
		} else {
			
			FeatureUnitConfiguration fuc = new FeatureUnitConfiguration(normalizeInput, useStemmer, ignoreStopwords, nGrams,
					false, miScoredFeaturesPerClass, suffixTrees);
			ExperimentConfiguration expConfig = new ExperimentConfiguration(fuc, quantifier, classifier, inputFile,
					outputFolder);
			result = FocusSingleExperimentExecutor.crossValidate(expConfig, jobs, preClassify,
					evaluationCategories, focusesFile, safeUnusedUnits);
		}

		List<MLCategoryResult> catResults = result.getMLCategoryEvaluations();
		for (CategoryResult cr : catResults) {
			System.out.println("TP: " + cr.getTP() + " - FP: " + cr.getFP() + " - FN: " 
					+ cr.getFN() + " - TN: " + cr.getTN());
			System.out.println(cr);			
		}
		System.out.println(result.getMacroAveraging());
		System.out.println(result.getMicroAveraging());

		System.out.println("Hamming Loss: \t" + result.getHammingLoss());
		System.out.println("One Error: \t" + result.getOneError());
		System.out.println("Coverage: \t" + result.getCoverage());
		
		System.out.println("Average Precision: " + result.getAverPrec());
		System.out.println("Precision: " + result.getPrecision());
		System.out.println("Average Recall: " + result.getAverRec());
		System.out.println("Recall: " + result.getRecall());
		System.out.println("F-Measure: " + result.getF1Measure());
		System.out.println("Average F-Measure: " + result.getAverF1());
		System.out.println("Accuracy: " + result.getAccuracy());
		System.out.println("Classification Accuracy: " + result.getClassificationAccuracy());
		
		

		// store result
		 jobs.persistExperimentResult(result, resultOutputFolder);
		 List<ExperimentResult> results = new ArrayList<ExperimentResult>();
		 results.add(result);
		 jobs.exportResults(results,resultOutputFolder);
	}
}
