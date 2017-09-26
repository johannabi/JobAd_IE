package binnewitt.classification;

import java.io.File;
import org.junit.Test;

import de.uni_koeln.spinfo.ml_classification.workflow.ConfigurableDBClassifier;

/**
 * angepasst an ClassifyJobAdsIntoParagraphs
 * 
 * @author Johanna
 *
 */
public class ClassifyJobAdsIntoFocus {

	//// APP CONFIGURATION //////////////////
	/**
	 * Path to input database
	 */
	static String inputXLSX = "src/test/resources/classification/input/getIn_7JobAds.xlsx";

	/**
	 * path to the output folder for classified Job Ads
	 */
	static String outputFolder = "src/test/resources/classification/output/";

	/**
	 * Path to the trainingdata-file annotated by getin-IT (is used to train the
	 * classifiers)
	 */
	static String trainingdataFile = "src/test/resources/classification/input/trainingSets/getIn_JobAdDB_greatminus7.xlsx";

	/**
	 * Path to the focuses-file with all appearing focus names (used to build training data)
	 */
	static String focusesFile = "src/test/resources/classification/input/getIn_focuses.xlsx";
	
	/**
	 * correctable output database
	 */
	static String outputXLSX = "getIn_classifiedJobAds.xlsx";
	///////// END ////////////////////////////
	
	@Test
	public void classify() {


		File inputDB = new File(inputXLSX);
		File outputDB = new File(outputFolder + outputXLSX);

		// TODO Optionen, falls output schon vorhanden ist, hinzufügen

		// create output-directory if not exists
		if (!new File("classification/output").exists()) { // ?? Pfad korrekt?
			new File("classification/output").mkdirs();
		}
		
		try {
			ConfigurableDBClassifier dbClassify = new ConfigurableDBClassifier(inputDB, 
					outputDB, trainingdataFile, focusesFile);
			dbClassify.classify();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
