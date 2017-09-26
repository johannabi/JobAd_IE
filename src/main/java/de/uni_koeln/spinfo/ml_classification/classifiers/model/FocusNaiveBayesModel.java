package de.uni_koeln.spinfo.ml_classification.classifiers.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.uni_koeln.spinfo.ml_classification.classifiers.FocusNaiveBayesClassifier;
import de.uni_koeln.spinfo.classification.core.classifier.AbstractClassifier;
import de.uni_koeln.spinfo.classification.core.classifier.model.Model;
import de.uni_koeln.spinfo.classification.zoneAnalysis.classifier.model.NaiveBayesClassModel;

/**
 * @author Johanna
 *
 * model-object based on NaiveBayesClassifier (adapted from
 * ZoneNaiveBayesModel)
 */
public class FocusNaiveBayesModel extends Model implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<NaiveBayesClassModel> classModels = new ArrayList<NaiveBayesClassModel>();
	
	public List<NaiveBayesClassModel> getClassModels() {
		return classModels;
	}
	
	public void addClassModel(NaiveBayesClassModel classModel) {
		classModels.add(classModel);
	}
	
	public AbstractClassifier getClassifier(){
		return new FocusNaiveBayesClassifier();
	}
	
	

}
