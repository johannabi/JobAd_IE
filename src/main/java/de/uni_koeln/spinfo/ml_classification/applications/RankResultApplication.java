package de.uni_koeln.spinfo.ml_classification.applications;

import java.io.File;
import java.io.IOException;

import de.uni_koeln.spinfo.ml_classification.evaluation.EvaluationValue;
import de.uni_koeln.spinfo.ml_classification.evaluation.MLResultComparator;

public class RankResultApplication {

	/////////////////////////////
	// APP-CONFIGURATION
	/////////////////////////////

	static String toRank = "classification/output/defaultResults/17_09_19_preClassified";
	static String output = "classification/output/rankings/defaultResults/17_09_19_preClassified";

	/////////////////////////////
	// END
	/////////////////////////////

	public static void main(String[] args) throws ClassNotFoundException, IOException {

		File inputFolder = new File(toRank);
		File outputFolder = new File(output);
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}

		MLResultComparator rc_filtered = new MLResultComparator();
		rc_filtered.addResults(inputFolder);
		System.out.println(rc_filtered.getAllResults().size());
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}

		getRankings(rc_filtered, outputFolder.getAbsolutePath());
	}

	private static void getRankings(MLResultComparator rc, String folderName) throws IOException {

//		int numberOfClasses = 12; // rc.getAllResults().iterator().next().getNumberOfClasses();

		for (EvaluationValue v : EvaluationValue.values()) {
			System.out.println(v);
			// TODO ergebnis wird nicht mehr für jedes label geranked
			rc.rankAll(v, folderName);

		}
	}

}
