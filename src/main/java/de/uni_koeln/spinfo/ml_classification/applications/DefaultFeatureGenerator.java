package de.uni_koeln.spinfo.ml_classification.applications;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.uni_koeln.spinfo.ml_classification.classifiers.FocusAbstractClassifier;
import de.uni_koeln.spinfo.ml_classification.classifiers.FocusMLKNNClassifier;
import de.uni_koeln.spinfo.ml_classification.workflow.FocusJobs;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.core.data.ExperimentConfiguration;
import de.uni_koeln.spinfo.classification.core.data.FeatureUnitConfiguration;
import de.uni_koeln.spinfo.classification.core.distance.Distance;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.AbstractFeatureQuantifier;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.LogLikeliHoodFeatureQuantifier;

/**
 * tries various experiment configurations and gives back the number of 
 * feature units for each configuration
 * @author Johanna
 *
 */
public class DefaultFeatureGenerator {
	
	static File trainingDataFile = new File("classification/data/trainingSets/getIn_JobAdDB_great.xlsx");
	
	static boolean safeUnusedUnits = false;

	static String outputFolder = "classification/output";
	
	static File focusesFile = new File("classification/data/trainingSets/getIn_focuses.xlsx");
	
	static boolean allowEmptyLabelmap = true;
	
	public static void main(String[] args) throws IOException {
		
		FocusJobs jobs = new FocusJobs(allowEmptyLabelmap);
		AbstractFeatureQuantifier fq = new LogLikeliHoodFeatureQuantifier();
		FocusAbstractClassifier classifier = new FocusMLKNNClassifier(4, Distance.COSINUS);
		
		for (int suffixTrees = 1; suffixTrees <= 1; suffixTrees++) { // nur mit suffix trees

			for (int norm = 0; norm <= 1; norm++) { 

				for (int stem = 0; stem <= 1; stem++) { 

					for (int stopwords = 0; stopwords <= 1; stopwords++) { 

						for (int n = 0; n <= 4; n++) { 

							int[] nGrams = null;
							switch (n) {
							case 1: {
								nGrams = new int[] { 2 };
								break;
							}
							case 2: {
								nGrams = new int[] { 3 };
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
							System.out.println("expConfig: " + expConfig.toString());
							
							List<ClassifyUnit> data = null;
							data = jobs.getCategorizedAdsFromFile(trainingDataFile, 
									fuc.isTreatEncoding(), focusesFile, safeUnusedUnits);
							data = jobs.initializeClassifyUnits(data, true);
							data = jobs.setFeatures(data, fuc, true);
							data = jobs.setFeatureVectors(data, fq, null);
							
							System.out.println(fq.getFeatureUnitOrder().size() + " Features");
							System.out.println("-----------");
						}
					}
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
