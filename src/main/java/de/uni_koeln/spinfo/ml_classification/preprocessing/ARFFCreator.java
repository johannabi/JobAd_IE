package de.uni_koeln.spinfo.ml_classification.preprocessing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_koeln.spinfo.ml_classification.data.FocusClassifyUnit;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;

/**
 * Creates an ARFF file for weka (meka)
 * 
 * @author Johanna
 *
 */
public class ARFFCreator {

	private Set<String> labels;
	private List<ClassifyUnit> trainingData;
	private List<String> attributes;

	public ARFFCreator(List<ClassifyUnit> trainingData, List<String> attributes, Set<String> labels) {
		this.labels = labels;
		this.trainingData = trainingData;
		this.attributes = attributes;
	}

	public void exportARFF() throws IOException {

		exportXML();
		FileWriter fw = new FileWriter("jobfocus.arff");
		BufferedWriter bw = new BufferedWriter(fw);

		bw.write("@relation MultiLabelFocus\n\n");
		// append attributes
		for (String attribute : attributes) {
			bw.write("@attribute " + attribute + " {0, 1}\n");
		}
		// System.out.println(sb.length());
		// append labels
		for (String label : labels) {
			bw.write("@attribute TAG_" + label + " {0, 1}\n");
		}
		// append data
		bw.write("\n@data\n");
		for (ClassifyUnit cu : trainingData) {
			bw.write("{");
			FocusClassifyUnit fcu = (FocusClassifyUnit) cu;
			double[] values = fcu.getFeatureVector();
			Map<String, Boolean> focuses = fcu.getInFocus();
			String comma = "";
			for (int i = 0; i < values.length; i++) {

				double value = values[i];
				if (value != 0.0) {
					bw.write(comma + i + " 1");
					comma = ",";
				}

			}
			// bw.write(values.length + " "); // TODO komma einbauen?
			StringBuilder sb = new StringBuilder();
			int index = values.length - 1;

			for (Map.Entry<String, Boolean> focus : focuses.entrySet()) {
				index++;
				if (focus.getValue()) {
					sb.append(comma + index + " 1");
					comma = ",";
				}

			}
			bw.write(sb.toString());
			bw.write("}\n");

		}

		bw.close();
		fw.close();

	}

	private void exportXML() throws IOException {

		FileWriter fw = new FileWriter("jobfocus.xml");

		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		sb.append("<labels xmlns=\"http://mulan.sourceforge.net/labels\">\n");
		for (String label : labels) {
			sb.append("<label name=\"TAG_" + label + "\"></label>\n");
		}
		sb.append("</labels>");

		fw.write(sb.toString());
		fw.close();
	}

}
