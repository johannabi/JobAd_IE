package de.uni_koeln.spinfo.ml_classification.workflow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uni_koeln.spinfo.ml_classification.evaluation.Evaluator;
import de.uni_koeln.spinfo.classification.core.classifier.model.Model;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.core.data.ExperimentConfiguration;

public class ConfigurableDBClassifier {

	private File inputFile, outputFile;
	private String trainingFileName, focusesFileName, inputFileName;
	private FocusJobs jobs;
	private boolean outputRanking = true;
	



	/**
	 * contructor for classification
	 * @param inputFile
	 * @param outputFile
	 * @param trainingFileName
	 * @param focusesFile
	 * @throws IOException
	 */
	public ConfigurableDBClassifier(File inputFile, File outputFile, String trainingFileName,
			String focusesFile) throws IOException {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.trainingFileName = trainingFileName;
		this.focusesFileName = focusesFile;
		this.jobs = new FocusJobs();
	}


	/**
	 * constructor for evaluation
	 * @param inputDB
	 * @param inputDBName
	 * @param outputDB
	 * @param focusesFile
	 * @throws IOException
	 */
	public ConfigurableDBClassifier(File inputDB, String inputDBName,
			File outputDB, String focusesFile) throws IOException {
		this.inputFile = inputDB;
		this.outputFile = outputDB;
		this.focusesFileName = focusesFile;
		this.inputFileName = inputDBName;
		this.jobs = new FocusJobs();
	}


	public void classify() throws ClassNotFoundException, IOException {
		// get Experiment Configuration
		SetupUI ui = new SetupUI();
		ExperimentConfiguration expConfig = ui.getExperimentConfiguration(trainingFileName);
		classify(expConfig);
	}

	private void classify(ExperimentConfiguration expConfig) throws IOException {
		System.out.println("Classify..........");
		// create training File
		File trainingDataFile = new File(trainingFileName);
		File focusesFile = new File(focusesFileName);
		File studiesFile = new File("ml_classification/data/studysubjects.xlsx");
		File degreesFile = new File("ml_classification/data/degrees.xlsx");
		List<ClassifyUnit> trainingData = null; 
		trainingData = jobs.getCategorizedAdsFromFile(trainingDataFile, 
				expConfig.getFeatureConfiguration().isTreatEncoding(), 
				focusesFile, studiesFile, degreesFile, true);
		System.out.println("added " + trainingData.size() + " training-Ads");
		

		
		//initialize Classify Units, Features, Vectors...
		trainingData = jobs.initializeClassifyUnits(trainingData, true);
		trainingData = jobs.setFeatures(trainingData,expConfig.getFeatureConfiguration(), true);
		trainingData = jobs.setFeatureVectors(trainingData, expConfig.getFeatureQuantifier(), null);
		
		//analyze focus combinations
		Map<Map<String,Boolean>,Integer> combiCount = Evaluator.analyzeCombinations(trainingData, 1);
		
		// build model
		Model model = jobs.getNewModelForClassifier(trainingData, expConfig);
		// TODO export Model
		model.setCombiCount(combiCount);
		
		// get data to annotate
		List<ClassifyUnit> toAnnotate = new ArrayList<ClassifyUnit>();
		toAnnotate.addAll(jobs.getNewAdsFromFile(inputFile, 
				expConfig.getFeatureConfiguration().isTreatEncoding()));
		System.out.println("added " + toAnnotate.size() + " new Ads to annotate");
		
		//initialize Classify Unity, Features, Vectors....
		toAnnotate = jobs.initializeClassifyUnits(toAnnotate, false);
		toAnnotate = jobs.setFeatures(toAnnotate, expConfig.getFeatureConfiguration(), false);
		toAnnotate = jobs.setFeatureVectors(toAnnotate, expConfig.getFeatureQuantifier(), model.getFUOrder());
		
		// classify
		Map<ClassifyUnit, Map<String, Boolean>> classifiedFocus = jobs.classify(toAnnotate, expConfig, model); 	
		
		// output
		jobs.exportClassifiedData(classifiedFocus, outputFile, outputRanking);
		
		
	}
}
