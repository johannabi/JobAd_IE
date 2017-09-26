package de.uni_koeln.spinfo.ml_classification.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.uni_koeln.spinfo.ml_classification.data.FocusClassifyUnit;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.zoneAnalysis.data.CategoryResult;


public class FocusEvaluator {
	private int numberOfClasses;
	// overall falseNegatives of each class
	private int overallFNs = 0;
	// overall FalsePositives of each class
	private int overallFPs = 0;
	// overall trueNegatives of each class;
	private int overallTNs = 0;
	// overall truePositives of each class;
	private int overallTPs = 0;

	// categoryResult-objects for each category
	private List<CategoryResult> categoryResults;

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
	 * @param classified
	 *            - Map of annotated classIDs (key) and classified classIDs of
	 *            each cross-valid.-group (value)
	 */
	public void evaluate(Map<ClassifyUnit, Map<String, Boolean>> classified, List<String> focusList) {
		numberOfClasses = ( (FocusClassifyUnit) classified.keySet().iterator().next()).getInFocus().size();
		List<Integer> classesList = new ArrayList<Integer>();
		for(int i = 1; i <= numberOfClasses; i++){
			classesList.add(i);
		}
		evaluate(classified, classesList, focusList);
	}

	/**
	 * @param classified
	 * @param categories
	 * @param focusList 
	 */
	public void evaluate(Map<ClassifyUnit, Map<String, Boolean>> classified, List<Integer> categories, List<String> focusList) {
		if(categories == null){
			evaluate(classified, focusList);
			return;
		}
		numberOfClasses = ( (FocusClassifyUnit) classified.keySet().iterator().next()).getInFocus().size();
		this.categoryResults = new ArrayList<CategoryResult>();
		for (int c = 1; c <= focusList.size(); c++) {
			CategoryResult catEv = new CategoryResult(c);
			categoryResults.add(catEv);
		}
		for (ClassifyUnit cu : classified.keySet()) {
			Map<String, Boolean> goldClasses = ( (FocusClassifyUnit) cu).getInFocus();

			for (int c = 1; c <= numberOfClasses; c++) {
				boolean useForEvaluation = categories.contains(c);
				if (goldClasses.get(focusList.get(c - 1))) {
					if(classified.get(cu).get(focusList.get(c - 1))){
						if(useForEvaluation){
							overallTPs++;
						}	
						categoryResults.get(c - 1).raiseTP();
					} else {
						if(useForEvaluation){
							overallFNs++;
						}	
						categoryResults.get(c - 1).raiseFN();
					}
				} else {
					if(!classified.get(cu).get(focusList.get(c - 1))){
						if(useForEvaluation){
							overallTNs++;
						}

						categoryResults.get(c - 1).raiseTN();
					} else {
						if(useForEvaluation){
							overallFPs++;
						}	
						categoryResults.get(c - 1).raiseFP();
					}
				}
			}
		}
	}

	/**
	 * @return all category results
	 */
	public List<CategoryResult> getCategoryEvaluations() {
		return categoryResults;
	}

	/**
	 * @return overall accuracy
	 */
	public double getOverallAccuracy() {
		double toReturn = ((double) (overallTPs + overallTNs))
				/ (overallTPs + overallTNs + overallFPs + overallFNs);
		return toReturn;
	}

	/**
	 * @return overall precision
	 */
	public double getOverallPrecision() {
		double toReturn = ((double) overallTPs) / (overallTPs + overallFPs);
		if(toReturn == Double.NaN){
			System.out.println("NAN:   "+overallTPs + " / (" + overallTPs+ " + " + overallFPs+" ) ");
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

	/**
	 * @return number of classes
	 */
	public int getNumberOfClasses() {
		return numberOfClasses;
	}

}
