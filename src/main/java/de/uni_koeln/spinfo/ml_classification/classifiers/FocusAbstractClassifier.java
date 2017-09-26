package de.uni_koeln.spinfo.ml_classification.classifiers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koeln.spinfo.classification.core.classifier.AbstractClassifier;
import de.uni_koeln.spinfo.classification.core.classifier.model.Model;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;

public abstract class FocusAbstractClassifier extends AbstractClassifier {


	private static final long serialVersionUID = 1L;
	
	
	public abstract Map<String, Boolean> classify(ClassifyUnit cu, Model model, List<String> focusNames, Set<String> allTokens);

}
