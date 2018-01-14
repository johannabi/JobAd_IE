package de.uni_koeln.spinfo.ml_classification.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.uni_koeln.spinfo.ml_classification.data.FocusClassifyUnit;
import de.uni_koeln.spinfo.ml_classification.data.MLCategoryResult;
import de.uni_koeln.spinfo.ml_classification.workflow.Util;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;

/**
 * class to store evaluation values for focus classification
 * @author Johanna
 *
 */
public class FocusEvaluator {

	/** number of labels */
	private int numberOfClasses;
	/** maximal number of wrong classified labels per classify unit */
	private int maxLoss = 0;
//	/** number of totally correct labeled classify units */
//	private int totalCorrect = 0;
	/** overall hamming loss */
	private double hammingLoss = 0d;
	/** overall one-error */
	private double oneError = 0d;
	/** overall coverage */
	private double coverage = 0d;
	/** average precision (Tsoumakas, 678) */
	private double averPrec = 0d;
	/** average recall */
	private double averRec = 0d;
	/** average f1score */
	private double averF1 = 0d;
	/** classification accuracy */
	private double classAccuracy = 0d;
	// overall falseNegatives
	private int overallFNs = 0;
	// overall FalsePositives
	private int overallFPs = 0;
	// overall trueNegatives
	private int overallTNs = 0;
	// overall truePositives
	private int overallTPs = 0;
	// results for each label
	private List<MLCategoryResult> categoryResults;
	// distribution in gold standard of labels per unit
	private Map<Integer, Integer> distGold = new HashMap<Integer, Integer>();
	// distribution of false negatives per unit
	private Map<Integer, Integer> distFN = new HashMap<Integer, Integer>();
	// distribution of false positives per unit
	private Map<Integer, Integer> distFP = new HashMap<Integer, Integer>();

	private Map<ClassifyUnit, Map<String, Boolean>> misclassified = new HashMap<ClassifyUnit, Map<String, Boolean>>();

	public Map<ClassifyUnit, Map<String, Boolean>> getMisclassified() {
		return misclassified;
	}

	public List<MLCategoryResult> getCategoryEvaluations() {
		return categoryResults;
	}

	/**
	 * @return overall false-negative
	 */
	public int getOverallFNs() {
		return overallFNs;
	}

	/**
	 * @return overall false-positives
	 */
	public int getOverallFPs() {
		return overallFPs;
	}

	/**
	 * @return overall true-negatives
	 */
	public int getOverallTNs() {
		return overallTNs;
	}

	/**
	 * @return overall true-positives
	 */
	public int getOverallTPs() {
		return overallTPs;
	}

	/**
	 * creates evaluation measures for the classified data and
	 * writes the values on the object's attributes (use getters)
	 * @param classified
	 * @param focusList
	 */
	public void evaluate(Map<ClassifyUnit, Map<String, Boolean>> classified, List<String> focusList) {
		numberOfClasses = ((FocusClassifyUnit) classified.keySet().iterator().next()).getInFocus().size();

		List<String> classesList = new ArrayList<String>();
		for (int i = 0; i < numberOfClasses; i++) {
			classesList.add(focusList.get(i));
		}
		evaluate(classified, classesList, focusList);
	}
	
	/**
	 * creates evaluation measures for the classified data and
	 * writes the values on the object's attributes (use getters)
	 * @param classified
	 * @param categories categories that should be integrated in evaluation
	 * @param focusList
	 */
	public void evaluate(Map<ClassifyUnit, Map<String, Boolean>> classified, List<String> categories,
			List<String> focusList) {
		
		if (categories == null) {
			evaluate(classified, focusList);
			return;
		}
		numberOfClasses = ((FocusClassifyUnit) classified.keySet().iterator().next()).getInFocus().size();
//		System.out.println(numberOfClasses + " Focuses");
		this.categoryResults = new ArrayList<MLCategoryResult>();
		for (int i = 0; i < focusList.size(); i++) {
			MLCategoryResult catEv = new MLCategoryResult(i + 1, focusList.get(i));
			categoryResults.add(catEv);
		}

		for (ClassifyUnit cu : classified.keySet()) {
			Map<String, Boolean> goldClasses = ((FocusClassifyUnit) cu).getInFocus();
			if(goldClasses.isEmpty())
				continue;
			Map<String, Boolean> predicted = classified.get(cu);
			
			if(goldClasses.equals(predicted))
				classAccuracy++;
			
			List<Entry<String, Double>> rankedLabels = Util.sortByComparator(((FocusClassifyUnit) cu).getRanking());
			int currentFP = 0;
			int currentFN = 0;
			// hamming loss: counts the labels that differ in gold standard and
			// predicted
			int intersection = 0;
			int goldLabels = 0;
			int predictLabels = 0;
			for (int i = 0; i < numberOfClasses; i++) {
				String currentLabel = focusList.get(i);
				// compute tp, tn, fp, fn
				if (goldClasses.get(currentLabel)) {
					goldLabels++;
					if (predicted.get(currentLabel)) {
						predictLabels++;
						intersection++;
						if (categories.contains(currentLabel))
							overallTPs++;
						categoryResults.get(i).raiseTP();
					} else {
						if (categories.contains(currentLabel))
							overallFNs++;
						categoryResults.get(i).raiseFN();
						currentFN++;
					}
				} else {
					if (!predicted.get(currentLabel)) {
						if (categories.contains(currentLabel))
							overallTNs++;
						categoryResults.get(i).raiseTN();
					} else {
						predictLabels++;
						if (categories.contains(currentLabel))
							overallFPs++;
						categoryResults.get(i).raiseFP();
						currentFP++;
					}
				}
			}
//			System.out.println("Gold: " + goldLabels);
//			System.out.println("Predicted: " + predictLabels);
//			System.out.println("Schnitt: " + schnittmenge);
			if (goldLabels != 0)
				averPrec = averPrec + ((double) intersection / (double) goldLabels);
			if (predictLabels != 0)
				averRec = averRec + ((double) intersection / (double) predictLabels);
			if (predictLabels != 0 && goldLabels != 0)
				averF1 = averF1 + (2 * intersection)/ (goldLabels + predictLabels);
//			System.out.println("Average Rec: " + averRec);
//			System.out.println("Average Prec: " + averPrec);
			int freq = 0;
			if (distFN.containsKey(currentFN))
				freq = distFN.get(currentFN);
			distFN.put(currentFN, freq + 1);
			freq = 0;
			if (distFP.containsKey(currentFP))
				freq = distFP.get(currentFP);
			distFP.put(currentFP, freq + 1);
			int currentLoss = currentFN + currentFP;
			if (currentLoss >= 4)
				misclassified.put(cu, predicted);
			if (currentLoss > maxLoss)
				maxLoss = currentLoss;
			hammingLoss = hammingLoss + ((double) currentLoss / (double) numberOfClasses);

			// one error: gets most probable label and counts if it isn't in
			// gold standard
			String mostProbLabel = rankedLabels.get(0).getKey();

			if (!goldClasses.get(mostProbLabel))
				oneError++;
			Set<String> toComplete = new HashSet<String>();
			int lastIndex = 0;
			int currentIndex = 0;
			int cNumberOfLabels = 0;
			for (Entry<String, Double> e : rankedLabels) {
//				System.out.println(e.getKey());
//				System.out.println(goldClasses.get(e.getKey()));
				// lastIndex raises if rankedLabel is in gold standard
				if (goldClasses.get(e.getKey())) {
					cNumberOfLabels++;
					toComplete.add(e.getKey());
					lastIndex = currentIndex;
				}
				currentIndex++;
			}
			Integer count = 0;
			if (distGold.containsKey(cNumberOfLabels))
				count = distGold.get(cNumberOfLabels);
			distGold.put(cNumberOfLabels, count + 1);

			coverage = coverage + lastIndex + 1;

		}

		hammingLoss = hammingLoss / (double) classified.size();
		oneError = oneError / (double) classified.size();
		coverage = coverage / (double) classified.size();
		averPrec = averPrec / (double) classified.size();
		averRec = averRec / (double) classified.size();
		averF1 = averF1 / (double) classified.size();
		classAccuracy = classAccuracy / (double) classified.size();

	}



	/**
	 * returns overall coverage
	 * 
	 * @return
	 */
	public double getCoverage() {
		return coverage;
	}

	/**
	 * returns overall hamming loss
	 * 
	 * @return
	 */
	public double getHammingLoss() {
		return hammingLoss;
	}


	/**
	 * returns maximal number of wrong classified labels per classify unit
	 * 
	 * @return
	 */
	public int getMaxLoss() {
		return maxLoss;
	}

	/**
	 * returns overall one-error
	 * 
	 * @return
	 */
	public double getOneError() {
		return oneError;
	}

	/**
	 * @return overall accuracy
	 */
	public double getOverallAccuracy() {
		double toReturn = ((double) (overallTPs + overallTNs)) / (overallTPs + overallTNs + overallFPs + overallFNs);
		return toReturn;
	}

	/**
	 * @return overall precision
	 */
	public double getOverallPrecision() {
		double toReturn = ((double) overallTPs) / (overallTPs + overallFPs);
		if (toReturn == Double.NaN) {
			System.out.println("NAN:   " + overallTPs + " / (" + overallTPs + " + " + overallFPs + " ) ");
		}
		return toReturn;
	}

	/**
	 * @return overall recall
	 */
	public double getOverallRecall() {
		double toReturn = ((double) overallTPs) / (overallTPs + overallFNs);
		return toReturn;
	}

	/**
	 * @return overall f1-score
	 */
	public double getOverallF1Score() {
		double toReturn = (2 * getOverallPrecision() * getOverallRecall())
				/ (getOverallPrecision() + getOverallRecall());
		return toReturn;
	}

	public double getAverPrec() {
		return averPrec;
	}

	public double getAverRec() {
		return averRec;
	}

	public double getAverF1() {
		return averF1;
	}
	
	public double getClassAccuracy() {
		return classAccuracy;
	}

}
