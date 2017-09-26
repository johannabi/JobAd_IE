package de.uni_koeln.spinfo.ml_classification.classifiers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.uni_koeln.spinfo.ml_classification.classifiers.model.FocusMLKNNModel;
import de.uni_koeln.spinfo.ml_classification.classifiers.model.MLKNNClassModel;
import de.uni_koeln.spinfo.ml_classification.data.FocusClassifyUnit;
import de.uni_koeln.spinfo.classification.core.classifier.model.Model;
import de.uni_koeln.spinfo.classification.core.data.ClassifyUnit;
import de.uni_koeln.spinfo.classification.core.data.FeatureUnitConfiguration;
import de.uni_koeln.spinfo.classification.core.distance.Distance;
import de.uni_koeln.spinfo.classification.core.distance.DistanceCalculator;
import de.uni_koeln.spinfo.classification.core.featureEngineering.featureWeighting.AbstractFeatureQuantifier;

public class FocusMLKNNClassifier extends FocusAbstractClassifier {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// number of nearest neighbors
	private int k = 5;
	// threshold of probability to tag with specific label
	private double threshold = 0.5;
	private List<Double> nearestDistances = new ArrayList<Double>();

	public FocusMLKNNClassifier(int k, Distance distance) {
		this.k = k;
		this.distance = distance;
	}

	public FocusMLKNNClassifier(int k, Distance distance, double threshold) {
		this.k = k;
		this.distance = distance;
		this.threshold = threshold;
	}

	public FocusMLKNNClassifier() {

	}

	public List<Double> getNearestDistances() {
		return nearestDistances;
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

	@Override
	public Model buildModel(List<ClassifyUnit> cus, FeatureUnitConfiguration fuc, AbstractFeatureQuantifier fq,
			File trainingDataFile) {
		nearestDistances = new ArrayList<Double>();
		FocusMLKNNModel model = new FocusMLKNNModel();
		Map<double[], HashMap<String, Boolean>> trainingData = new HashMap<double[], HashMap<String, Boolean>>();
		// write trainingData
		for (ClassifyUnit cu : cus) {
			trainingData.put(cu.getFeatureVector(), (HashMap<String, Boolean>) ((FocusClassifyUnit) cu).getInFocus());
		}

		List<String> focuses = new ArrayList<String>(((FocusClassifyUnit) cus.get(0)).getInFocus().keySet());
		int labelNum = focuses.size();
		// prior probabilities for each label (l=1 and l=0)
		Map<String, Double> pHl1 = new HashMap<String, Double>();
		Map<String, Double> pHl0 = new HashMap<String, Double>();
		double members = cus.size();
		double smooth = 1d;

		// compute prior probabilities for all labels (1-2)
		for (int l = 0; l < labelNum; l++) {

			double membersWithLabel = 0;

			for (ClassifyUnit cu : cus) {
				Boolean cuInFocus = ((FocusClassifyUnit) cu).getInFocus().get(focuses.get(l));
				if (cuInFocus)
					membersWithLabel++;
			}
			double labelProb = (smooth + membersWithLabel) / (smooth * 2 + members);
			pHl1.put(focuses.get(l), labelProb);
			pHl0.put(focuses.get(l), (1 - labelProb));

		}

		// compute posterior probabilities P (E lj | H lb)

		// for every cu identify knn (3)
		for (ClassifyUnit cu : cus) {
			// identify nearest neighbors
			cu = identifyKNNs(cu, cus);
		}

		// compute posterior probabilities for every label (4-13)
		for (int l = 0; l < labelNum; l++) {

			// initialize counter (5-6)
			int[] sameFocusAsKNNs = new int[k + 1];
			int[] notSameFocusAsKNNs = new int[k + 1];

			for (int i = 0; i <= k; i++) {
				sameFocusAsKNNs[i] = 0;
				notSameFocusAsKNNs[i] = 0;
			}

			// count focuses in neighbors of every classify unit(7)
			for (ClassifyUnit cu : cus) {

				/* nfc = neighborfocuscount */
				/* */int nfc = ((FocusClassifyUnit) cu).getNeighborFocusCount().get(focuses.get(l));

				if (((FocusClassifyUnit) cu).getInFocus().get(focuses.get(l)))
					sameFocusAsKNNs[nfc] = (sameFocusAsKNNs[nfc] + 1);
				else
					notSameFocusAsKNNs[nfc] = (notSameFocusAsKNNs[nfc] + 1);

			}

			// TEST
			// System.out.println(focuses.get(l));
			// System.out.println("Not Same Focus");
			// for (int i = 0; i < notSameFocusAsKNNs.length; i++) {
			// System.out.println(i + ": " + notSameFocusAsKNNs[i]);
			// }
			// System.out.println("Same Focus");
			// for (int i = 0; i < sameFocusAsKNNs.length; i++) {
			// System.out.println(i + ": " + sameFocusAsKNNs[i]);
			// }
			// System.out.println("**************************+");
			// ENDE

			// create sums for (11-13)
			int sumSameFocus = 0;
			int sumNotSameFocus = 0;

			for (int i = 0; i < sameFocusAsKNNs.length; i++) {
				sumSameFocus = sumSameFocus + sameFocusAsKNNs[i];
				sumNotSameFocus = sumNotSameFocus + notSameFocusAsKNNs[i];
			}

			// compute probs for every k (in specific label) (11-13)
			// P(E lj | H l1) = (s + c[j]) / (s * (k + 1 ) + sum(0 bis k ) c
			// [p])
			double kWhenFocus[] = new double[k + 1];
			double kWhenNoFocus[] = new double[k + 1];

			for (int i = 0; i <= k; i++) {
				kWhenFocus[i] = (double) (smooth + sameFocusAsKNNs[i]) / (double) (smooth * (k + 1) + sumSameFocus);
				kWhenNoFocus[i] = (double) (smooth + notSameFocusAsKNNs[i])
						/ (double) (smooth * (k + 1) + sumNotSameFocus);
			}

			MLKNNClassModel classModel = new MLKNNClassModel();
			classModel.setFocusName(focuses.get(l));
			classModel.setProbKWhenFocus(kWhenFocus);
			classModel.setProbKWhenNoFocus(kWhenNoFocus);

			model.addClassModel(classModel);

		}
		model.setTrainingData(trainingData);
		model.setTrainingUnits(cus);
		model.setPriorProbsL0(pHl0);
		model.setPriorProbsL1(pHl1);
		model.setFUOrder(fq.getFeatureUnitOrder());

		// TEST AUSGABE
		// for(MLKNNClassModel m : model.getClassModels()){
		// System.out.println("Focus: " + m.getFocusName());
		// for(int i = 0; i < m.getProbKWhenFocus().length; i++){
		// System.out.println(i + " K - Prob Same Label: " +
		// m.getProbKWhenFocus()[i]);
		// }
		// }
		// ENDE

		return model;
	}

	/**
	 * identifies the K nearest neighbors and counts the occurence of each label
	 * 
	 * @param cu
	 *            specific classify unit
	 * @param cus
	 *            training data
	 * @return specific classify unit with identified neighbors
	 */
	private ClassifyUnit identifyKNNs(ClassifyUnit cu, List<ClassifyUnit> cus) {
		Map<Double, ArrayList<FocusClassifyUnit>> focusesByDistance = new TreeMap<Double, ArrayList<FocusClassifyUnit>>();
		List<ClassifyUnit> currentCUS = new ArrayList<ClassifyUnit>(cus);
		currentCUS.remove(cu);
		for (ClassifyUnit currentCU : currentCUS) {
			double[] featureVector = currentCU.getFeatureVector();
			double dist = DistanceCalculator.getDistance(featureVector, cu.getFeatureVector(), distance);
			if (dist < 0)
				System.out.println("** " + dist);
			List<FocusClassifyUnit> distFocus;
			if (focusesByDistance.containsKey(dist))
				distFocus = focusesByDistance.get(dist);
			else
				distFocus = new ArrayList<FocusClassifyUnit>();
			distFocus.add((FocusClassifyUnit) currentCU);

			focusesByDistance.put(dist, (ArrayList<FocusClassifyUnit>) distFocus);
		}

		// find k nearest vectors
		List<FocusClassifyUnit> KNNs = new ArrayList<FocusClassifyUnit>();
		Double nearestDist = null;
		for (Map.Entry<Double, ArrayList<FocusClassifyUnit>> e : focusesByDistance.entrySet()) {
			if (e.getKey() < 0)
				System.out.println(e.getKey() + "**");
			if (nearestDist == null)
				nearestDist = e.getKey();
			if (KNNs.size() < k)
				KNNs.addAll(e.getValue());
			else {
				// number of neighbors has to fit k
				while (KNNs.size() > k) {
					KNNs.remove(KNNs.size() - 1);
				}

				break;
			}

		}
		nearestDistances.add(nearestDist);

		((FocusClassifyUnit) cu).setNeighbors(KNNs);

		Map<String, Integer> knnFocusCount = new HashMap<String, Integer>();
		for (FocusClassifyUnit knn : KNNs) {
			for (Map.Entry<String, Boolean> e : knn.getInFocus().entrySet()) {
				Integer count = 0;
				if (knnFocusCount.containsKey(e.getKey()))
					count = knnFocusCount.get(e.getKey());
				if (e.getValue())
					knnFocusCount.put(e.getKey(), count + 1);
				else
					knnFocusCount.put(e.getKey(), count);
			}
		}
		((FocusClassifyUnit) cu).setNeighborFocusCount(knnFocusCount);
		return cu;
	}

	@Override
	public Map<String, Boolean> classify(ClassifyUnit cu, Model model, List<String> focusNames, Set<String> allTokens) {

		FocusMLKNNModel mlknnModel = (FocusMLKNNModel) model;
		FocusClassifyUnit fcu = (FocusClassifyUnit) cu;
		if (fcu.getNeighbors() == null)
			fcu = (FocusClassifyUnit) identifyKNNs(cu, mlknnModel.getTrainingUnits());
		Map<String, Integer> nfc = fcu.getNeighborFocusCount();
		Map<String, Double> priorL0 = mlknnModel.getPriorProbsL0();
		Map<String, Double> priorL1 = mlknnModel.getPriorProbsL1();
		List<MLKNNClassModel> classModels = mlknnModel.getClassModels();
		Map<String, Boolean> toReturn = new HashMap<String, Boolean>();
		Map<String, Double> ranking = new HashMap<String, Double>();

		for (MLKNNClassModel classModel : classModels) {
			String labelName = classModel.getFocusName();
			// compute arg max

			Double cPriorL0 = priorL0.get(labelName);
			Double cPriorL1 = priorL1.get(labelName);

			Double postL0 = classModel.getProbKWhenNoFocus()[nfc.get(labelName)];
			Double postL1 = classModel.getProbKWhenFocus()[nfc.get(labelName)];

			Double label = cPriorL1 * postL1;
			Double noLabel = cPriorL0 * postL0;

			Double rankLabel = label / (label + noLabel);

			toReturn.put(labelName, (rankLabel > threshold)); // TODO if-abfrage gelöscht

			// label + noLabel = postL1

			ranking.put(labelName, rankLabel);

		}
		// System.out.println("-------------------------------------------------------------------");
		fcu.setRanking(ranking);
		return toReturn;
	}

}
