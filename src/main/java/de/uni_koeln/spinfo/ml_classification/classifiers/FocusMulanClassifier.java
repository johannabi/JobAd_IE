package de.uni_koeln.spinfo.ml_classification.classifiers;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koeln.spinfo.ml_classification.classifiers.model.FocusMulanModel;
import de.uni_koeln.spinfo.classification.core.classifier.model.Model;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.core.data.FeatureUnitConfiguration;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.AbstractFeatureQuantifier;
import mulan.classifier.lazy.MLkNN;
import mulan.classifier.meta.RAkEL;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.MultiLabelInstances;
import mulan.evaluation.Evaluator;
import mulan.evaluation.MultipleEvaluation;
import weka.classifiers.trees.J48;

public class FocusMulanClassifier extends FocusAbstractClassifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * http://mulan.sourceforge.net/starting.html
	 * @throws Exception
	 */
	public void classify() throws Exception {

//		String[] args = new String[2];
//		args[0] = "jobfocus.xml";
//		args[1] = "jobfocus.arff";

		String arffFileName = "jobfocus.arff";
		String xmlFileName = "jobfocus.xml";
		
		MultiLabelInstances dataset = new MultiLabelInstances(arffFileName, xmlFileName);
		
		System.out.println("Instances: " + dataset.getNumInstances());
		
		RAkEL rakel = new RAkEL(new LabelPowerset(new J48()));
		MLkNN mlknn = new MLkNN(); 
		
		rakel.build(dataset);
		System.out.println("DONE building");
		
		Evaluator eval = new Evaluator();
		MultipleEvaluation results;
		
		int numFolds = 10;
		try{
			results = eval.crossValidate(rakel, dataset, numFolds);
			System.out.println("********");
			System.out.println("RAkEL");
			System.out.println(results);
			results = eval.crossValidate(mlknn, dataset, numFolds);
			System.out.println("********");
			System.out.println("MLkNN");
			System.out.println(results);
		} catch (Exception e){
			System.out.println("++++++");
		}
		
		
	}

	@Override
	public Map<String, Boolean> classify(ClassifyUnit cu, Model model, List<String> focusNames, Set<String> allTokens) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Model buildModel(List<ClassifyUnit> cus, FeatureUnitConfiguration fuc, AbstractFeatureQuantifier fq,
			File trainingDataFile) {
		Model model = new FocusMulanModel();
		model.setClassifierName(this.getClass().getSimpleName());
		model.setDataFile(trainingDataFile);
		model.setFuc(fuc);
		model.setFUOrder(fq.getFeatureUnitOrder());
		return model;
	}

}
