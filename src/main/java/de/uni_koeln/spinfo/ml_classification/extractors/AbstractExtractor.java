package de.uni_koeln.spinfo.ml_classification.extractors;


import java.util.Map;

import de.uni_koeln.spinfo.ml_classification.data.FocusClassifyUnit;

public abstract class AbstractExtractor {
	
	public abstract Map<String, Boolean> extract(FocusClassifyUnit toExtract);

}
