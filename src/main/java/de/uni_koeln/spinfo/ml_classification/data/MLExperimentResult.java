package de.uni_koeln.spinfo.ml_classification.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_koeln.spinfo.ml_classification.evaluation.EvaluationValue;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.zoneAnalysis.data.ExperimentResult;

/**
 * contains the evaluation of a multilabel classification
 * @author Johanna
 *
 */
public class MLExperimentResult extends ExperimentResult {

	private static final long serialVersionUID = 1L;
	private double hammingLoss, oneError, coverage, averPrec, averRec, averF1, classAccuracy;
	private int /** totalCorrect, */
	numberOfEvalData, maxLoss;
	private Map<ClassifyUnit, Map<String, Boolean>> badlymisclassified;
	private List<MLCategoryResult> categoryEvaluations;

	public double getHammingLoss() {
		return hammingLoss;
	}

	public void setHammingLoss(double hammingLoss) {
		this.hammingLoss = hammingLoss;
	}

	public void setOneError(double oneError) {
		this.oneError = oneError;
	}

	public double getOneError() {
		return oneError;
	}

	public double getCoverage() {
		return coverage;
	}

	public void setCoverage(double coverage) {
		this.coverage = coverage;
	}

	public void setNumberOfEvalData(int numberOfEvalData) {
		this.numberOfEvalData = numberOfEvalData;
	}

	public int getNumberOfEvalData() {
		return numberOfEvalData;
	}

	public void setMaxLoss(int maxLoss) {
		this.maxLoss = maxLoss;
	}

	public int getMaxLoss() {
		return maxLoss;
	}

	/**
	 * @param ev
	 *            the evaluation type
	 * @return the evaluation value
	 */
	public double getEvaluationValue(EvaluationValue ev) {
		switch (ev) {
		case HAMMINGLOSS:
			return getHammingLoss();
		case ONEERROR:
			return getOneError();
		case COVERAGE:
			return getCoverage();
		case PRECISION:
			return getPrecision();
		case RECALL:
			return getRecall();
		case FMEASURE:
			return getF1Measure();
		case ACCURACY:
			return getAccuracy();
		}
		return 0.0;
	}

	public void setMisClassified(Map<ClassifyUnit, Map<String, Boolean>> map) {
		this.badlymisclassified = map;
	}

	public Map<ClassifyUnit, Map<String, Boolean>> getMisclassified() {
		return badlymisclassified;
	}

	public List<MLCategoryResult> getMLCategoryEvaluations() {
		return categoryEvaluations;
	}
	
	/**
	 * returns Micro-Precision, -Recall and F-Measure
	 * @return
	 */
	public Map<String, Double> getMicroAveraging() {
		Map<String, Double> toReturn = new HashMap<String, Double>();
		double microTP = 0d;
		double microFP = 0d;
		double microFN = 0d;
		// summarize all tp, tn, fp, fn
		for (MLCategoryResult result : categoryEvaluations) {
			microTP += result.getTP();
			microFP += result.getFP();
			microFN += result.getFN();
		}

		// double microTP = ((double) tp)/ (categoryEvaluations.size());
		// double microFP = ((double) fp)/ (categoryEvaluations.size());
		// double microFN = ((double) fn)/ (categoryEvaluations.size());

		double prec = microTP / (microTP + microFP);
		double rec = microTP / (microTP + microFN);
		double f1 = (2.0 * microTP) / (2.0 * microTP + microFP + microFN);

		toReturn.put("Micro Precision: ", prec);
		toReturn.put("Micro Recall: ", rec);
		toReturn.put("Micro F1: ", f1);

		return toReturn;
	}

	/**
	 * returns Macro-Precision, -Recall and F-Measure
	 * @return
	 */
	public Map<String, Double> getMacroAveraging() {
		Map<String, Double> toReturn = new HashMap<String, Double>();

		double macroPrec = 0d;
		double macroRec = 0d;
		double macroF1 = 0d;

		for (MLCategoryResult result : categoryEvaluations) {
			double currPrec = result.getPrecision();
			if (!Double.isNaN(currPrec))
				macroPrec += currPrec;
			macroRec += result.getRecall();
			macroF1 += result.getF1Score();
		}

		macroPrec = macroPrec / categoryEvaluations.size();
		macroRec = macroRec / categoryEvaluations.size();
		macroF1 = macroF1 / categoryEvaluations.size();

		toReturn.put("Macro Precision: ", macroPrec);
		toReturn.put("Macro Recall: ", macroRec);
		toReturn.put("Macro F1: ", macroF1);

		return toReturn;
	}

	public void setMLCategoryEvaluations(List<MLCategoryResult> catRes) {
		this.categoryEvaluations = catRes;
	}

	public void setAverPrec(double averPrec) {
		this.averPrec = averPrec;
	}

	public double getAverPrec() {
		return averPrec;
	}

	public void setAverRec(double averRec) {
		this.averRec = averRec;
	}

	public double getAverRec() {
		return averRec;
	}

	public double getAverF1() {
		return averF1;
	}

	public void setAverF1(double averF1) {
		this.averF1 = averF1;
	}

	public double getClassificationAccuracy() {
		return classAccuracy;
	}

	public void setClassificationAccuracy(double classAccuracy) {
		this.classAccuracy = classAccuracy;
	}

}
