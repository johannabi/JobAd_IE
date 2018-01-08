package de.uni_koeln.spinfo.ml_classification.workflow;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.core.data.ExperimentConfiguration;
import de.uni_koeln.spinfo.ml_classification.classifiers.KeywordClassifier;
import de.uni_koeln.spinfo.ml_classification.data.MLExperimentResult;


public class FocusSingleExperimentExecutor {

	/**
	 * cross validates the given experiment configurations
	 * @param expConfig experiment configurations including the training data
	 * @param jobs object that contains methods to process the data
	 * @param preClassify set true if jobAds should be pre-classified with keyword classifier
	 * @param evaluationCategories list of labels that should be recognized in evaluation 
	 * (set null, if all should influence the evaluation)
	 * @param focusesFile file with labels for classification
	 * @param safeUnused set true if unusable jobAds should be collected in a file
	 * @return result of cross validation
	 * @throws IOException
	 */
	public static Map<String, MLExperimentResult> crossValidate(ExperimentConfiguration expConfig, FocusJobs jobs, boolean preClassify,
			List<String> evaluationCategories, File focusesFile,
			File studiesFile, File degreesFile, Boolean safeUnused) throws IOException {
		long before = System.nanoTime();
		// prepare classifyUnits...
		List<ClassifyUnit> paragraphs = null;
		paragraphs = jobs.getCategorizedAdsFromFile(expConfig.getDataFile(),
				expConfig.getFeatureConfiguration().isTreatEncoding(), 
				focusesFile, studiesFile, degreesFile, safeUnused);
		
		System.out.println(paragraphs.size() + " JobsAds in training data");
		long after = System.nanoTime();
//		System.out.println("prepare CUs: " + (after - before) / 1000000000d);
		before = System.nanoTime();
		paragraphs = jobs.initializeClassifyUnits(paragraphs, true);
		after = System.nanoTime();
//		System.out.println("initialize CUs: " + (after - before) / 1000000000d);
		before = System.nanoTime();
		paragraphs = jobs.setFeatures(paragraphs, expConfig.getFeatureConfiguration(), true);

		after = System.nanoTime();
//		System.out.println("setFeatures: " + (after - before) / 1000000000d);
		before = System.nanoTime();
		paragraphs = jobs.setFeatureVectors(paragraphs, expConfig.getFeatureQuantifier(), null);
		after = System.nanoTime();
//		System.out.println("set Vectors: " + (after - before) / 1000000000d);
		// preclassify
		// analyze focus combinations
//		Map<Map<String, Boolean>, Integer> combiCount = Evaluator.analyzeCombinations(paragraphs, 1);
		
		//TODO delete EXPORT DATA TO CSV
		Set<String> focuses = jobs.getFocuses();
		for (String string : focuses) {
			jobs.createCSV(string, paragraphs, "ml_classification/csvs/" + string + ".txt");
		}
		
		
		
		//END
		
		
		

		Map<ClassifyUnit, Map<String, Double>> preClassified = new HashMap<ClassifyUnit, Map<String, Double>>();
		if (preClassify) {
			before = System.nanoTime();
			Map<String,List<String>> keywords = Util.prepareKeywords(jobs.getKeywords(), expConfig.getFeatureConfiguration());
			KeywordClassifier kc = new KeywordClassifier(keywords);
			for (ClassifyUnit cu : paragraphs) {
				// alles auf null gesetzt, was nicht gebraucht wird
				Map<String, Double> labelRanking = kc.classify(cu); 
				preClassified.put(cu, labelRanking);
			}
			after = System.nanoTime();
//			System.out.println("preClassify: " + (after - before) / 1000000000d);
		}

		// classify
		before = System.nanoTime();
		Map<ClassifyUnit, Map<String, Boolean>> classified = jobs.crossvalidate(paragraphs, expConfig);
		after = System.nanoTime();
//		System.out.println("crossvalidate: " + (after - before) / 1000000000d);

		// merge results
		if (preClassify) { 
			before = System.nanoTime();
			classified = jobs.mergeResults(classified, preClassified);
			after = System.nanoTime();
//			System.out.println("merge: " + (after - before) / 1000000000d);
		}

		// serialize
//		FileOutputStream fos = new FileOutputStream("classified.ser");
//		ObjectOutputStream oos = new ObjectOutputStream(fos);
//		oos.writeObject(classified);
//		fos.close();
//		oos.close();
//		fos = new FileOutputStream("evaluationCategories.ser");
//		oos = new ObjectOutputStream(fos);
//		oos.writeObject(evaluationCategories);
//		fos.close();
//		oos.close();
//		fos = new FileOutputStream("expConfig.ser");
//		oos = new ObjectOutputStream(fos);
//		oos.writeObject(expConfig);
//		fos.close();
//		oos.close();
		
		// evaluate
		before = System.nanoTime();
		Map<String,MLExperimentResult> results = jobs.evaluateML(classified, evaluationCategories, expConfig);
		after = System.nanoTime();
//		System.out.println("evaluate: " + (after - before) / 1000000000d);
		before = System.nanoTime();
//		Map<ClassifyUnit, Map<String, Boolean>> misClassified = result.getMisclassified();
//		Util.exportmisclassifiedtoXLSX("misclassified.xlsx", misClassified);
//		System.out.println("export mis-classified: " + (after - before) / 1000000000d);
		after = System.nanoTime();
		return results;
	}

}
