package de.uni_koeln.spinfo.ml_classification.applications;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uni_koeln.spinfo.classification.core.data.ExperimentConfiguration;
import de.uni_koeln.spinfo.classification.core.data.FeatureUnitConfiguration;
import de.uni_koeln.spinfo.classification.core.distance.Distance;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.AbstractFeatureQuantifier;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.LogLikeliHoodFeatureQuantifier;
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
 * 
 * 
 * Executes a lot of experiments and serializes them. Stores experiment results in
 * ml_classification/output/evaluation/defaultResult
 * you can rank the results afterwards with RankResultsApplication.java
 * In addition, exports experiment results as .xlsx-File
 */
public class DefaultExperimentGenerator {
	
	// APP-CONFIGURATION
	
	static File trainingDataFile = new File("ml_classification/data/trainingSets/JobAdDB_small.xlsx");

	static File resultsOutputFile = new File("ml_classification/output/evaluation/defaultResults/17_09_21_preClassifiedCSV");

	static boolean preClassify = false;
	
	static boolean safeUnusedUnits = false;

	static String outputFolder = "ml_classification/output";
	
	static File focusesFile = new File("ml_classification/data/focuses.xlsx");
	
	static File studiesFile = new File("ml_classification/data/studysubjects.xlsx");
	
	static File degreesFile = new File("ml_classification/data/degrees.xlsx");
	
	static List<String> evaluationCategories;
	
	static boolean allowEmptyLabelmap = true;
	
	
	// END

	public static void main(String[] args) throws IOException, ClassNotFoundException{
		

		FocusJobs jobs = new FocusJobs(allowEmptyLabelmap);

		List<FocusAbstractClassifier> classifiers = new ArrayList<FocusAbstractClassifier>();

		classifiers.add(new FocusMLKNNClassifier(13, Distance.COSINUS));
		classifiers.add(new FocusMLKNNClassifier(10, Distance.COSINUS));
		classifiers.add(new FocusMLKNNClassifier(7, Distance.COSINUS));
		

		List<AbstractFeatureQuantifier> quantifiers = new ArrayList<AbstractFeatureQuantifier>();
		quantifiers.add(new LogLikeliHoodFeatureQuantifier());
//		quantifiers.add(new TFIDFFeatureQuantifier());
//		quantifiers.add(new AbsoluteFrequencyFeatureQuantifier());
//		quantifiers.add(new RelativeFrequencyFeatureQuantifier());

		for (FocusAbstractClassifier classifier : classifiers) {

			for (AbstractFeatureQuantifier fq : quantifiers) {

				List<ExperimentResult> resultList = new ArrayList<ExperimentResult>(); 
				Map<String, MLExperimentResult> result = null;

				for (int suffixTrees = 0; suffixTrees <= 0; suffixTrees++) { //kein Suffixtree

					for (int norm = 1; norm <= 1; norm++) { // nur normiert

						for (int stem = 1; stem <= 1; stem++) { // nur gestemmt

							for (int stopwords = 1; stopwords <= 1; stopwords++) { 

								for (int n = 1; n <= 1; n++) { // nur 3-gramme

									int[] nGrams = null;
									switch (n) {
									case 1: {
										nGrams = new int[] { 3 };
										break;
									}
									case 2: {
										nGrams = new int[] { 2 };
										break;
									}
									case 3: {
										nGrams = new int[] { 2, 3 };
										break;
									}
									case 4: {
										nGrams = new int[] { 3, 4 };
										break;
									}
									default:
										break;
									}
									FeatureUnitConfiguration fuc = new FeatureUnitConfiguration(toBool(norm),
											toBool(stem), toBool(stopwords), nGrams, false, 0, toBool(suffixTrees));
									ExperimentConfiguration expConfig = new ExperimentConfiguration(fuc, fq, classifier,
											trainingDataFile, outputFolder);
//									expConfig.setModelFileName(expConfig.toString().replaceAll(".", ""));
									System.out.println("expConfig: " + expConfig.toString());

									result = FocusSingleExperimentExecutor.crossValidate(expConfig, jobs,
											preClassify, evaluationCategories, focusesFile, 
											studiesFile, degreesFile, safeUnusedUnits);
//									resultList.add(result);
//									System.out.println("++++");
//									List<MLCategoryResult> catResults = result.getMLCategoryEvaluations();
//									for (CategoryResult cr : catResults) {
//										System.out.println("TP: " + cr.getTP() + " - FP: " + cr.getFP() + " - FN: " 
//												+ cr.getFN() + " - TN: " + cr.getTN());
//										System.out.println(cr);			
//									}
//									System.out.println("++++");
//									System.out.println(result.getMacroAveraging());
//									System.out.println(result.getMicroAveraging());
//									
//									System.out.println("Hamming Loss: \t" + result.getHammingLoss());
//									System.out.println("One Error: \t" + result.getOneError());
//									System.out.println("Coverage: \t" + result.getCoverage());
//
//									System.out.println("Average Precision: " + result.getAverPrec());
//									System.out.println("Precision: " + result.getPrecision());
//									System.out.println("Average Recall: " + result.getAverRec());
//									System.out.println("Recall: " + result.getRecall());
//									System.out.println("F-Measure: " + result.getF1Measure());
//									System.out.println("Average F-Measure: " + result.getAverF1());
//									System.out.println("Accuracy: " + result.getAccuracy());
//									System.out.println("Classification Accuracy: " + result.getClassificationAccuracy());
//									System.out.println("---------------------------------------------");
								}
							}
						}
					}
				}
				// write Results...
				jobs.persistExperimentResults(resultList, resultsOutputFile);
				jobs.exportResults(resultList,resultsOutputFile);

				if (classifier instanceof FocusNaiveBayesClassifier) {
					break;
				}
			}
		}
	}

	public static boolean toBool(int i) {
		if (i != 0) {
			return true;
		}
		return false;
	}
		
	

}
