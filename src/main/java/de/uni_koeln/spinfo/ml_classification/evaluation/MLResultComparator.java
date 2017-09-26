package de.uni_koeln.spinfo.ml_classification.evaluation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.uni_koeln.spinfo.ml_classification.data.MLExperimentResult;

public class MLResultComparator {
	Set<MLExperimentResult> allResults;

	public MLResultComparator() {
		this.allResults = new HashSet<MLExperimentResult>();
	}

	Comparator<Double> comparator = new Comparator<Double>() {
		public int compare(Double o1, Double o2) {
			// compare
			if (o1 < o2) {
				return 1;
			}
			if (o2 < o1) {
				return -1;
			} else
				return 0;
		}
	};

	/**
	 * @param results
	 */
	public void addResults(List<MLExperimentResult> results) {
		allResults.addAll(results);
	}

	/**
	 * @param objectFile
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public void addResults(File objectFile) throws ClassNotFoundException, IOException {
		readObjects(objectFile);
	}

	/**
	 * @param ev
	 * @param classID
	 *            ( 0 = overall value)
	 * @return ranking
	 * @throws IOException
	 */
	public Map<Double, List<MLExperimentResult>> rankAll(EvaluationValue ev, int classID, String folderName)
			throws IOException {
		Map<Double, List<MLExperimentResult>> ranking = new TreeMap<Double, List<MLExperimentResult>>(comparator);
		for (MLExperimentResult result : allResults) {
			double value = 0.0;
			if (classID == 0) {
				value = result.getEvaluationValue(ev);
			} else {
				// value = result.getCategoryEvaluations().get(classID - 1)
				// .getEvaluationValue(ev);
			}
			List<MLExperimentResult> list = ranking.get(value);
			if (list == null) {
				list = new ArrayList<MLExperimentResult>();
			}
			list.add(result);
			ranking.put(value, list);

		}
		writeRankingFile(folderName + "/" + ev + "_" + classID + "_" + allResults.hashCode() + ".csv", ranking);

		return ranking;
	}

	public Map<Double, List<MLExperimentResult>> rankAll(EvaluationValue ev, String folderName) throws IOException {
		Map<Double, List<MLExperimentResult>> ranking = new TreeMap<Double, List<MLExperimentResult>>(comparator);
		for (MLExperimentResult result : allResults) {
			double value = 0.0;
			// value of specific evaluation measure for specific experiment
			// result
			value = result.getEvaluationValue(ev);

			List<MLExperimentResult> list = ranking.get(value);
			if (list == null) {
				list = new ArrayList<MLExperimentResult>();
			}
			list.add(result);
			ranking.put(value, list);

		}
		writeRankingFile(folderName + "/" + ev + "_" + allResults.hashCode() + ".csv", ranking);

		return ranking;
	}

	/**
	 * @throws IOException
	 */
	public void writeObjects(File file) throws IOException {

		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream out = new ObjectOutputStream(fos);
		for (MLExperimentResult result : allResults) {
			out.writeObject(result);

		}
		out.flush();
		out.close();
	}

	/**
	 * @return resultList
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Set<MLExperimentResult> readObjects(File file) throws IOException, ClassNotFoundException {
		System.out.println("read objects");
		// allResults.clear();
		if (file.isDirectory()) {
			System.out.println("directory");
			File[] listFiles = file.listFiles();
			for (File file2 : listFiles) {
				System.out.println(file2.getName());
				FileInputStream fis = new FileInputStream(file2);
				ObjectInputStream in = new ObjectInputStream(fis);
				try {
					MLExperimentResult list = (MLExperimentResult) in.readObject();
					System.out.println(list.getExperimentConfiguration());
					allResults.add(list);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// while (true) {
				//
				// try {
				// Object o = in.readObject();
				// MLExperimentResult read = (MLExperimentResult) o;
				// System.out.println(read.getExperimentConfiguration());
				//// if (read.getExperimentConfiguration().contains("great"))
				// allResults.add(read);
				// } catch (Exception e) {
				// break;
				// }
				// }
				in.close();
			}
		} else {
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream in = new ObjectInputStream(fis);
			while (true) {
				try {
					Object o = in.readObject();
					MLExperimentResult read = (MLExperimentResult) o;
					if (read.getExperimentConfiguration().contains("great"))
						allResults.add(read);
				} catch (Exception e) {
					break;
				}
			}
			in.close();
		}
		return allResults;
	}

	/**
	 * @param fileName
	 * @param ranking
	 * @throws IOException
	 */
	private void writeRankingFile(String fileName, Map<Double, List<MLExperimentResult>> ranking) throws IOException {
		makePrettyExperimentConf(ranking.get(ranking.keySet().iterator().next()).get(0).getExperimentConfiguration());
		File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		OutputStream os = new FileOutputStream(file);
		PrintWriter out = new PrintWriter(os);
		DecimalFormat f = new DecimalFormat("#0.0000");
		out.print("valu" + "\t");

		out.print("macroPre\t");
		out.print("macroRec\t");
		out.print("macroFme\t");
		out.print("microPre\t");
		out.print("microRec\t");
		out.print("microFme\t");
		out.print("averPre\t");
		out.print("averRec\t");
		out.print("averFme\t");
		out.print("ClassAcc\t");
		out.print("hloss\t");
		out.print("oer\t");
		out.print("cov\t");

		out.print("Classifier\t");
		out.print("Distance\t");
		out.print("Quantifier\t");
		out.print("NGrams\t");
		out.print("MI\t");
		out.print("NoStopwords\t");
		out.print("Normalized\t");
		out.print("Stemmed\t");
		out.print("SuffixeTrees");

		out.println();
		for (Double d : ranking.keySet()) {
			List<MLExperimentResult> list = ranking.get(d);

			for (MLExperimentResult er : list) {
				out.print(f.format(d) + "\t");
						
				out.print(f.format(er.getMacroAveraging().get("Macro Precision: ")) + "\t");
				out.print(f.format(er.getMacroAveraging().get("Macro Recall: ")) + "\t");
				out.print(f.format(er.getMacroAveraging().get("Macro F1: ")) + "\t");
				out.print(f.format(er.getMicroAveraging().get("Micro Precision: ")) + "\t");
				out.print(f.format(er.getMicroAveraging().get("Micro Recall: ")) + "\t");
				out.print(f.format(er.getMicroAveraging().get("Micro F1: ")) + "\t");
				
				out.print(f.format(er.getAverPrec()) + "\t");
				out.print(f.format(er.getAverRec()) + "\t");
				out.print(f.format(er.getAverF1()) + "\t");
				out.print(f.format(er.getClassificationAccuracy()) + "\t");
				
				out.print(f.format(er.getHammingLoss()) + "\t");
				out.print(f.format(er.getOneError()) + "\t");
				out.print(f.format(er.getCoverage()) + "\t");

				out.print(makePrettyExperimentConf(er.getExperimentConfiguration()));
				out.print(er.getID() + "\t");
				out.println();
			}

		}
		out.flush();
		out.close();
	}

	/**
	 * @return resultList
	 */
	public Set<MLExperimentResult> getAllResults() {
		return allResults;
	}

	/**
	 * @param result
	 */
	public void addResult(MLExperimentResult result) {
		allResults.add(result);
	}

	private String makePrettyExperimentConf(String experimentConfiguration) {
		String classifier = null;
		String distance = null;
		String quantifier = null;
		boolean stopwords = false;
		boolean normalize = false;
		boolean suffixTree = false;
		boolean stem = false;
		int mi = 0;
		String nGram = null;
		String k = null;
		StringBuffer buff = new StringBuffer();
		String[] snippets = experimentConfiguration.split("&");
		System.out.println(experimentConfiguration);
		for (String string : snippets) {
			String[] smallSnippets = string.split("_");
			for (String smallS : smallSnippets) {
				if (smallS.equals("swFilter")) {
					stopwords = true;
				}
				if (smallS.equals("norm")) {
					normalize = true;
				}
				if (smallS.equals("stem")) {
					stem = true;
				}
				if (smallS.equals("suffixTrees")) {
					suffixTree = true;
				}
				if (smallS.startsWith("mi:")) {
					mi = Integer.parseInt(smallS.substring(3));

				}
				if (smallS.endsWith("gramms")) {
					nGram = smallS;

				}
				if (smallS.contains("Classifier")) {
					classifier = smallS.substring(0, smallS.length() - 10);
					k = smallS.substring(smallS.length() - 1, smallS.length());
				}
				if (smallS.contains("Quantifier")) {
					quantifier = smallS.substring(0, smallS.length() - 17);
				}
				if (smallS.equals("MANHATTAN") || smallS.equals("COSINUS") || smallS.equals("EUKLID")) {
					distance = smallS;
				}
			}

		}
		if (k == null) {
			buff.append(classifier + "\t");
		} else {
			buff.append(classifier + "(" + k + ")\t");
		}
		buff.append(distance + "\t");
		buff.append(quantifier + "\t");
		buff.append(nGram + "\t");
		buff.append(mi + "\t");
		buff.append(stopwords + "\t");
		buff.append(normalize + "\t");
		buff.append(stem + "\t");
		buff.append(suffixTree + "\t");

		return buff.toString();
	}
}
