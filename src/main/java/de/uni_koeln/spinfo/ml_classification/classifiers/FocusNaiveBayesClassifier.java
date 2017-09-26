package de.uni_koeln.spinfo.ml_classification.classifiers;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koeln.spinfo.ml_classification.classifiers.model.FocusNaiveBayesModel;
import de.uni_koeln.spinfo.ml_classification.data.FocusClassifyUnit;
import de.uni_koeln.spinfo.classification.core.classifier.model.Model;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.core.data.FeatureUnitConfiguration;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.AbstractFeatureQuantifier;
import de.uni_koeln.spinfo.classification.zoneAnalysis.classifier.model.NaiveBayesClassModel;

public class FocusNaiveBayesClassifier extends FocusAbstractClassifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private double threshold = 0.5;
	
	public FocusNaiveBayesClassifier(double threshold) {
		this.threshold = threshold;
	}
	
	public FocusNaiveBayesClassifier() {

	}

	@Override
	public Model buildModel(List<ClassifyUnit> cus, FeatureUnitConfiguration fuc, AbstractFeatureQuantifier fq,
			File trainingDataFile) {
		Model model = new FocusNaiveBayesModel();

		// TODO model für jedes "nicht in Fokus" bauen?
		// für jeden Fokus prüfen welche cus drin sind und welche nicht bzw
		// welche Features drin sind und welche nicht
		Set<String> focuses = ((FocusClassifyUnit) cus.get(0)).getInFocus().keySet();

		for (String focus : focuses) {
			NaiveBayesClassModel focusModel = new NaiveBayesClassModel();
			int membersInFocus = 0;
			int membersNotInFocus = 0;
			/** DocFrequencies in Focus */
			Map<String, Integer> inFocusDF = new HashMap<String, Integer>();
			/** DocFrequencies not in Focus */
			Map<String, Integer> notInFocusDF = new HashMap<String, Integer>();

			for (ClassifyUnit cu : cus) {
				Set<String> uniqueFUs = new HashSet<String>(cu.getFeatureUnits());
				Boolean inFocus = ((FocusClassifyUnit) cu).getInFocus().get(focus);
				if (inFocus) {
					membersInFocus++;
					for (String fu : uniqueFUs) {
						int df = 0;
						if (inFocusDF.containsKey(fu))
							df = inFocusDF.get(fu);
						inFocusDF.put(fu, df + 1);
					}
				} else {
					membersNotInFocus++;
					// TODO notInFocusDF ++
					for (String fu : uniqueFUs) {
						int df = 0;
						if (notInFocusDF.containsKey(fu))
							df = notInFocusDF.get(fu);
						notInFocusDF.put(fu, df + 1);
					}
				}
			}
//			System.out.println("Members in Class: " + membersInFocus + ", Not: " + membersNotInFocus);
			/** prob. for focus f */
			double classProbability = (float) membersInFocus / cus.size();
			focusModel.setClassProbability(classProbability);
			focusModel.setMembersInClass(membersInFocus);
			focusModel.setMembersNotInClass(membersNotInFocus);
			focusModel.setInClassDFs(inFocusDF);
			focusModel.setNotInClassDFs(notInFocusDF);
			focusModel.setFocusName(focus);
			((FocusNaiveBayesModel) model).addClassModel(focusModel);

			// System.out.println(focus);
			// System.out.println("In Focus: " + membersInFocus);
			// System.out.println("Not in Focus: "+ membersNotInFocus);
		}
		model.setClassifierName(this.getClass().getSimpleName());
		if (fq != null) {
			model.setFQName(fq.getClass().getSimpleName());
			model.setFUOrder(fq.getFeatureUnitOrder());
		}

		model.setDataFile(trainingDataFile);
		model.setFuc(fuc);

		return model;
	}

	@Override
	public Map<String, Boolean> classify(ClassifyUnit cu, Model model, List<String> focusNames, Set<String> allTokens) {
		int numberOfFocuses = focusNames.size();
		Map<String, Boolean> toReturn = new HashMap<String, Boolean>();
		Map<String, Double> ranking = new HashMap<String, Double>();
		for (int focus = 1; focus <= numberOfFocuses; focus++) {
			NaiveBayesClassModel classModel = ((FocusNaiveBayesModel) model).getClassModels().get(focus - 1);
			// boolean inFocus = classifyBernoulli(cu, allTokens, classModel,
						// model);
			double probInFocus = classify(cu, numberOfFocuses, classModel, model);

			ranking.put(focusNames.get(focus - 1), probInFocus);		
			toReturn.put(focusNames.get(focus - 1), (probInFocus > threshold));

		}
		((FocusClassifyUnit) cu).setRanking(ranking);
		return toReturn;

	}

	private boolean classifyBernoulli(ClassifyUnit cu, Set<String> allTokens, NaiveBayesClassModel classModel,
			Model model) {

		double product = 1d;
		List<String> featureUnits = cu.getFeatureUnits();
		Map<String, Integer> modelInClassDFs = classModel.getInClassDFs();
		Map<String, Integer> modelNotInClassDFs = classModel.getNotInClassDFs();
		int membersInClass = classModel.getMembersInClass();
		int membersNotInClass = classModel.getMembersNotInClass();
		int occurenceOfToken = 0;
		// computes the Probs for (token|Focus) etc. ....
		for (String token : allTokens) {
			if (featureUnits.contains(token))
				occurenceOfToken = 1;
			Integer tokenDF = modelInClassDFs.get(token);
			if (tokenDF == null)
				tokenDF = 0;
			double tokenFocus = tokenDF / membersInClass;

			double tokenProb = occurenceOfToken * tokenFocus + (1 - occurenceOfToken) * (1 - tokenFocus); // (Shimondaira
																											// 2017)
			product = product * tokenProb;
		}

		double focusProb = membersInClass / (membersInClass + membersNotInClass);

		double probCUInClass = focusProb * product;

		product = 1d;
		occurenceOfToken = 0;
		// computes the Probs for (token|notFocus) etc. ....
		for (String token : allTokens) {
			if (featureUnits.contains(token))
				occurenceOfToken = 1;
			Integer tokenDF = modelNotInClassDFs.get(token);
			if (tokenDF == null)
				tokenDF = 0;
			double tokenNotFocus = tokenDF / membersNotInClass;

			double tokenProb = occurenceOfToken * tokenNotFocus + (1 - occurenceOfToken) * (1 - tokenNotFocus); // (Shimondaira
																												// 2017)
			product = product * tokenProb;
		}
		double notFocusProb = membersNotInClass / (membersInClass + membersNotInClass);

		double probCUNotInClass = notFocusProb * product;

		return probCUInClass > probCUNotInClass;
	}

	private double classify(ClassifyUnit cu, int numberOfFocuses, NaiveBayesClassModel classModel, Model model) {

		int allMembers = classModel.getMembersInClass() + classModel.getMembersNotInClass();
		int membersInFocus = classModel.getMembersInClass();
		int membersNotInFocus = classModel.getMembersNotInClass();
		Map<String, Integer> inFocusDFs = classModel.getInClassDFs();
		Map<String, Integer> notInFocusDFs = classModel.getNotInClassDFs();

		// P (Focus|cu)
		double probFocusCU = 1d;
		// P (notFocus|cu)
		double probNotFocusCU = 1d;
		// P (cu|Focus)
		double probCUFocus = 1d;
		// P (cu|notFocus)
		double probCUNotFocus = 1d;
		// P (cu)
		double probCU = 1d; 
		// P (focus)
		double probFocus = (double) membersInFocus / (double)allMembers;
		// P (notFocus)
		double probNotFocus = (double)membersNotInFocus / (double)allMembers;
//		System.out.println("Prob Focus: " + probFocus + ", Prob not Focus: " + probNotFocus);
		for (String fu : cu.getFeatureUnits()) {
			// number of docs in Focus with feature unit
			Integer dfInFocus = inFocusDFs.get(fu);
			if (dfInFocus == null)
				dfInFocus = 0;
			probCUFocus = probCUFocus * (dfInFocus + 1) / membersInFocus; 
			// number of docs not in Focus with feature unit
			Integer dfNotInFocus = notInFocusDFs.get(fu);
			if (dfNotInFocus == null)
				dfNotInFocus = 0;
			probCUNotFocus = probCUNotFocus * (dfNotInFocus + 1) / membersNotInFocus; 

		}
		
		probFocusCU = (probCUFocus * probFocus) / probCU;
		probNotFocusCU = (probCUNotFocus * probNotFocus) / probCU;
		double rank = probFocusCU / (probFocusCU + probNotFocusCU);
		
		return rank;

	}

}
