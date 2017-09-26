package de.uni_koeln.spinfo.ml_classification.classifiers.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_koeln.spinfo.ml_classification.classifiers.FocusMLKNNClassifier;
import de.uni_koeln.spinfo.classification.core.classifier.AbstractClassifier;
import de.uni_koeln.spinfo.classification.core.classifier.model.Model;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;

public class FocusMLKNNModel extends Model {

	private static final long serialVersionUID = 1L;

	private List<MLKNNClassModel> classModels = new ArrayList<MLKNNClassModel>();
	
	private Map<String, Double> priorProbsL1 = new HashMap<String, Double>();
	
	private Map<String, Double> priorProbsL0 = new HashMap<String, Double>();
	
	private Map<double[], HashMap<String, Boolean>> trainingData = new HashMap<double[], HashMap<String, Boolean>>();
	
	private List<ClassifyUnit> trainingUnits = new ArrayList<ClassifyUnit>();

	public Map<double[], HashMap<String, Boolean>> getTrainingData() {
		return trainingData;
	}

	public void setTrainingData(Map<double[], HashMap<String, Boolean>> trainingData) {
		this.trainingData = trainingData;
	}
	
	public void addClassModel(MLKNNClassModel classModel){
		classModels.add(classModel);
	}
	
	public List<MLKNNClassModel> getClassModels(){
		return classModels;
	}
	
	public AbstractClassifier getClassifier(){
		return new FocusMLKNNClassifier();
	}

	public Map<String, Double> getPriorProbsL1() {
		return priorProbsL1;
	}

	public void setPriorProbsL1(Map<String, Double> priorProbsL1) {
		this.priorProbsL1 = priorProbsL1;
	}

	public Map<String, Double> getPriorProbsL0() {
		return priorProbsL0;
	}

	public void setPriorProbsL0(Map<String, Double> priorProbsL0) {
		this.priorProbsL0 = priorProbsL0;
	}

	public List<ClassifyUnit> getTrainingUnits() {
		return trainingUnits;
	}

	public void setTrainingUnits(List<ClassifyUnit> trainingUnits) {
		this.trainingUnits = trainingUnits;
	}
}
