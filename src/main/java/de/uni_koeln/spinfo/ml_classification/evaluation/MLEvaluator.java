package de.uni_koeln.spinfo.ml_classification.evaluation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.core.data.ExperimentConfiguration;
import de.uni_koeln.spinfo.ml_classification.data.FocusClassifyUnit;
import de.uni_koeln.spinfo.ml_classification.data.MLExperimentResult;

public class MLEvaluator {
	
	private FocusEvaluator focusEvaluator;
	private StudyEvaluator studyEvaluator;	
	private DegreeEvaluator degreeEvaluator;
	
	public MLEvaluator(){
		focusEvaluator = new FocusEvaluator();
		studyEvaluator = new StudyEvaluator();
		degreeEvaluator = new DegreeEvaluator();
	}
	
	public Map<String, MLExperimentResult> evaluate(Map<ClassifyUnit, Map<String, Boolean>> classified, ExperimentConfiguration expConfig,
			List<String> categories, List<String> focusList, List<String> studyList, List<String> degreeList){
		
		Map<String, MLExperimentResult> toReturn = new HashMap<String, MLExperimentResult>();
		
		
		focusEvaluator.evaluate(classified, focusList);
		studyEvaluator.evaluate(classified, studyList);
		degreeEvaluator.evaluate(classified, degreeList);
		
		MLExperimentResult focusER = new MLExperimentResult();
		focusER.setExperimentConfiguration(expConfig.toString());
		focusER.setHammingLoss(focusEvaluator.getHammingLoss());
		focusER.setMaxLoss(focusEvaluator.getMaxLoss());
		focusER.setOneError(focusEvaluator.getOneError());
		focusER.setCoverage(focusEvaluator.getCoverage());
		// er.setTotalCorrect(focusEvaluator.getTotalCorrect());
		focusER.setNumberOfEvalData(classified.size());
		focusER.setAccuracy(focusEvaluator.getOverallAccuracy());
		focusER.setF1Measure(focusEvaluator.getOverallF1Score());
		focusER.setPrecision(focusEvaluator.getOverallPrecision());
		focusER.setAverPrec(focusEvaluator.getAverPrec());
		focusER.setAverRec(focusEvaluator.getAverRec());
		focusER.setAverF1(focusEvaluator.getAverF1());
		focusER.setClassificationAccuracy(focusEvaluator.getClassAccuracy());
		focusER.setRecall(focusEvaluator.getOverallRecall());
		focusER.setTN(focusEvaluator.getOverallTNs());
		focusER.setTP(focusEvaluator.getOverallTPs());
		focusER.setFN(focusEvaluator.getOverallFNs());
		focusER.setFP(focusEvaluator.getOverallFPs());
		focusER.setMisClassified(focusEvaluator.getMisclassified());
		focusER.setMLCategoryEvaluations(focusEvaluator.getCategoryEvaluations());
		toReturn.put("Focuses", focusER);
		
		MLExperimentResult studyER = new MLExperimentResult();
		studyER.setExperimentConfiguration(expConfig.toString());
		studyER.setHammingLoss(studyEvaluator.getHammingLoss());
		studyER.setMaxLoss(studyEvaluator.getMaxLoss());
		studyER.setNumberOfEvalData(classified.size());
		studyER.setAccuracy(studyEvaluator.getOverallAccuracy());
		studyER.setF1Measure(studyEvaluator.getOverallF1Score());
		studyER.setPrecision(studyEvaluator.getOverallPrecision());
		studyER.setAverPrec(studyEvaluator.getAverPrec());
		studyER.setAverRec(studyEvaluator.getAverRec());
		studyER.setAverF1(studyEvaluator.getAverF1());
		studyER.setClassificationAccuracy(studyEvaluator.getClassAccuracy());
		studyER.setRecall(studyEvaluator.getOverallRecall());
		studyER.setTN(studyEvaluator.getOverallTNs());
		studyER.setTP(studyEvaluator.getOverallTPs());
		studyER.setFN(studyEvaluator.getOverallFNs());
		studyER.setFP(studyEvaluator.getOverallFPs());
		studyER.setMisClassified(studyEvaluator.getMisclassified());
		studyER.setMLCategoryEvaluations(studyEvaluator.getCategoryEvaluations());
		toReturn.put("Studies", studyER);
		
		MLExperimentResult degreeER = new MLExperimentResult();
		degreeER.setExperimentConfiguration(expConfig.toString());
		degreeER.setHammingLoss(degreeEvaluator.getHammingLoss());
		degreeER.setMaxLoss(degreeEvaluator.getMaxLoss());
		degreeER.setNumberOfEvalData(classified.size());
		degreeER.setAccuracy(degreeEvaluator.getOverallAccuracy());
		degreeER.setF1Measure(degreeEvaluator.getOverallF1Score());
		degreeER.setPrecision(degreeEvaluator.getOverallPrecision());
		degreeER.setAverPrec(degreeEvaluator.getAverPrec());
		degreeER.setAverRec(degreeEvaluator.getAverRec());
		degreeER.setAverF1(degreeEvaluator.getAverF1());
		degreeER.setClassificationAccuracy(degreeEvaluator.getClassAccuracy());
		degreeER.setRecall(degreeEvaluator.getOverallRecall());
		degreeER.setTN(degreeEvaluator.getOverallTNs());
		degreeER.setTP(degreeEvaluator.getOverallTPs());
		degreeER.setFN(degreeEvaluator.getOverallFNs());
		degreeER.setFP(degreeEvaluator.getOverallFPs());
		degreeER.setMisClassified(degreeEvaluator.getMisclassified());
		degreeER.setMLCategoryEvaluations(degreeEvaluator.getCategoryEvaluations());
		toReturn.put("Degrees", degreeER);
		
		return toReturn;
		
	}

}
