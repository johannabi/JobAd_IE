package de.uni_koeln.spinfo.ml_classification.classifiers;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;


public class KeywordClassifier {//extends FocusAbstractClassifier {

	private Map<String, List<String>> focusKeywords;

	public KeywordClassifier(Map<String, List<String>> focusKeywords) {
		this.focusKeywords = focusKeywords;
	}

	public Map<String, Double> classify(ClassifyUnit cu) {

		Map<String, Double> ranking = new HashMap<String, Double>();

		// search for every keyword in cu.content
		for (Map.Entry<String, List<String>> e : focusKeywords.entrySet()) {

			// if there are no keywords available, Focus gets false
			if (e.getValue().isEmpty()) {
				ranking.put(e.getKey(), -1.0);
				continue;
			}

			List<String> focusKeys = e.getValue();
			double matchSum = 0d;

			for (String string : focusKeys) {
				int index = cu.getFeatureUnits().indexOf(string);
				if (index >= 0){
					double val = cu.getFeatureVector()[index];
					matchSum += val;
				}
				
			}
			ranking.put(e.getKey(), matchSum);

//			if (matches >= (focusKeys.size() / 3)) { // 1/3 der Keywords muss in der Anzeige vorkommen
//				preFocuses.put(e.getKey(), true);
//			}
//
//			else
//				preFocuses.put(e.getKey(), false);

		}
		

		
		
		return ranking;
	}



}
