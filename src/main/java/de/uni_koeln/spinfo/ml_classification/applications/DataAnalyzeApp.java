package de.uni_koeln.spinfo.ml_classification.applications;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.uni_koeln.spinfo.ml_classification.workflow.FocusJobs;
import de.uni_koeln.spinfo.ml_classification.workflow.Util;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;

/**
 * application to analyze the classified job-Ads in terms of label distribution and label combinations.
 * stores the result in .txt-File
 * @author Johanna
 *
 */
public class DataAnalyzeApp {
	
	static File trainingData = new File("ml_classification/data/trainingSets/getIn_JobAdDB_great.xlsx");
	static File focusesFile = new File("ml_classification/data/getIn_focuses.xlsx");
	static boolean allowEmptyLabelmap = true;

	public static void main(String[] args) throws IOException {

		FocusJobs jobs = new FocusJobs();
		
		List<ClassifyUnit> data = jobs.getCategorizedAdsFromFile(trainingData, true, focusesFile, false);
		String result = jobs.analyzeData(data);
		Util.writeTXTFile(result, "dataStatistics.txt");
	}

}
