package kokkodis.utils;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.TreeSet;

import kokkodis.holders.ProbHolder;
import kokkodis.logistic.Classify;
import kokkodis.utils.compare.TSComparator;
import kokkodis.utils.compare.XYPairComparator;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Problem;

public class PredictUtils extends Utils {

	private static double baseline;

	/**
	 * 
	 * The function estimates AUC (if verbose) and accuracies.
	 * 
	 * @param model
	 *            : produced model
	 * @param verbal
	 *            : flag to verbose mode.
	 */
	public void predictProbModel(boolean verbal) {
		Counter<String> errorAnalysis = new Counter<String>();

		Model model = null;
		try {

			model = Linear.loadModel(new File(Classify.dataPath + "model/"
					+ Classify.fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		/* For AUC intatiate XYPair. */
		TreeSet<ProbHolder> sortedTreeForEstimatingLift = null;
		TreeSet<XYPair> xyData = null;
		HashMap<Double, Counter<String>> errorCounters = new HashMap<Double, Counter<String>>();
		if (verbal) {
			sortedTreeForEstimatingLift = new TreeSet<ProbHolder>(
					new TSComparator());
			xyData = new TreeSet<XYPair>(new XYPairComparator());
			for (double th = 0.1; th < 0.95; th += 0.05) {
				errorCounters.put(th, new Counter<String>());

			}
		}
		/* end */

		PrintToFile outputProbabilities = new PrintToFile();
		outputProbabilities.openFile(Classify.dataPath + "results"
				+ Classify.slash + "probs" + Classify.slash + ""
				+ Classify.intToCat.get(Classify.category) + Classify.slash
				+ "" + Classify.fileName + ".csv");
		outputProbabilities
				.writeToFile("cat,jobType,opening,contractor,pr_interview,true_label");

		Problem problem = loadProblem("testData" + Classify.slash + "test"
				+ Classify.trainTestName + ".txt");

		ArrayList<ProbHolder> testHolder = loadHolders(Classify.dataPath
				+ "testData" + Classify.slash + "test" + "Holder"
				+ Classify.trainTestName + ".csv");
		ListIterator<ProbHolder> it = testHolder.listIterator();
		for (int i = 0; i < problem.x.length; i++) {
			ProbHolder tempHolder = it.next();

			int predictedLabel = Linear.predict(model, problem.x[i]);

			tempHolder.setActuaLabel(problem.y[i]);

			updateEvaluation(errorAnalysis, predictedLabel,
					tempHolder.getActualabel());

			double[] probEstimates = new double[2];
			// probEstimates[1] is the probability of being 1, the probability
			// of being positive.
			Linear.predictProbability(model, problem.x[i], probEstimates);

			tempHolder.setProb(probEstimates[1]);

			outputProbabilities.writeToFile(Classify.category + ","
					+ Classify.jobType + "," + tempHolder.getOpening() + ","
					+ tempHolder.getConractor() + "," + tempHolder.getProb()
					+ "," + tempHolder.getActualabel());

			if (verbal) {
				sortedTreeForEstimatingLift.add(tempHolder); // this is for
																// lifts
																// analysis.
				for (double th = 0.1; th < 0.95; th += 0.05) {
					updateEvaluation(errorCounters.get(th), probEstimates[1],
							th, tempHolder.getActualabel());
				}
			}

		}

		printAccuracies(errorAnalysis);

		/* for AUC */
		if (verbal) {

			for (double th = 0.1; th < 0.95; th += 0.05) {
				Counter<String> curCounter = errorCounters.get(th);
				calculateRates(curCounter);
				xyData.add(new XYPair(curCounter.getCount("FPRate"), curCounter
						.getCount("TPRate")));
			}
			System.out.println("-----------------------------");
			DecimalFormat myFormatter = new DecimalFormat("#.###");
			String out = myFormatter.format(calculateAUC(xyData));
			System.out.println("|       AUC:" + out + "          |");
			System.out.println("-----------------------------");
			printAUCPoints(xyData);
			liftanalysis(sortedTreeForEstimatingLift);
		}
		outputProbabilities.closeFile();
	}

	/**
	 * 
	 * prints analysis.
	 * 
	 * @param errorAnalysis
	 */
	private void printAccuracies(Counter<String> errorAnalysis) {
		double positiveInstances = errorAnalysis.getCount("TP")
				+ errorAnalysis.getCount("FN");
		double negativeInstances = errorAnalysis.getCount("FP")
				+ errorAnalysis.getCount("TN");

		baseline = +Math.max(positiveInstances, negativeInstances)
				/ (positiveInstances + negativeInstances);
		System.out.println();
		System.out.println("Accuracies");
		System.out.println("Baseline (major class):"+baseline);
		double acc = (errorAnalysis.getCount("TP") + errorAnalysis
				.getCount("TN")) / errorAnalysis.totalCount();
		System.out.println(Classify.currentSolver + " (current model):" + acc);
		System.out.println();
		System.out.println("Confusion Matrix:");
		System.out.println("-------------------------");
		System.out.println("       |     Actual    | ");
		System.out.println("-------------------------");
		System.out.println("       |   +   |   -   | ");
		System.out.println("  +    | " + (int) errorAnalysis.getCount("TP")
				+ " | " + (int) errorAnalysis.getCount("FP") + " | ");
		System.out.println("  -    | " + (int) errorAnalysis.getCount("FN")
				+ " | " + (int) errorAnalysis.getCount("TN") + " | ");
		System.out.println("-------------------------");
		System.out.println();

	}

	private void liftanalysis(TreeSet<ProbHolder> sortedTreeForEstimatingLift) {

		PrintToFile liftsFile = new PrintToFile();
		liftsFile.openFile(Classify.dataPath + Classify.slash + "results"
				+ Classify.slash + "lifts" + Classify.slash + ""
				+ Classify.intToCat.get(Classify.category) + Classify.slash
				+ "" + Classify.fileName + ".csv");
		liftsFile.writeToFile("top_prc_ranked, lift");

		System.out.println();
		System.out
				.println("--------------------------------------------------------");
		System.out
				.println("| Top Ranked % | # Instances | # Positives |  %  | Lift |");
		System.out
				.println("--------------------------------------------------------");
		DecimalFormat myFormatter = new DecimalFormat("#.###");
		for (double th = 0.95; th > 0; th -= 0.05) {
			double positive = 0;
			double total = 0;
			for (ProbHolder ph : sortedTreeForEstimatingLift
					.headSet(new ProbHolder(th, -1))) {
				if (ph.isPositive())
					positive++;
				total++;

			}
			if (total > 20) {

				double actualProb = positive / total;
				String prc = myFormatter.format(actualProb);
				double lift = actualProb / (1 - baseline);
				String topStr = myFormatter.format(1 - th);
				String liftStr = myFormatter.format(lift);
				System.out.println("|     " + topStr + "     |    "
						+ (int) total + "    |    " + (int) positive
						+ "    |   " + prc + "   | " + liftStr + " |");

				liftsFile.writeToFile(topStr + "," + lift);

			}

		}
		System.out
				.println("--------------------------------------------------------");
		liftsFile.closeFile();
	}

	/**
	 * Assumes predicted prob > threshold -> positive instance.
	 * 
	 * @param errorsAnalysis
	 *            counter
	 * @param predictedProbabilityOfBeingPositive
	 * @param threshold
	 *            probability threshold for classification
	 */
	private void updateEvaluation(Counter<String> errorsAnalysis,
			double predictedProbabilityOfBeingPositive, double threshold,
			int trueLabel) {
		if (predictedProbabilityOfBeingPositive >= threshold && trueLabel == 1)
			errorsAnalysis.incrementCount("TP", 1);
		else if (predictedProbabilityOfBeingPositive >= threshold
				&& trueLabel == 0) {
			errorsAnalysis.incrementCount("FP", 1);
		} else if (predictedProbabilityOfBeingPositive < threshold
				&& trueLabel == 1)
			errorsAnalysis.incrementCount("FN", 1);
		else
			errorsAnalysis.incrementCount("TN", 1);

	}

	/**
	 * True positive -> label =1; True Negative -> label = 0;
	 * 
	 * @param errorsAnalysis
	 * @param predictedLabel
	 * @param trueLabel
	 */
	private void updateEvaluation(Counter<String> errorsAnalysis,
			int predictedLabel, int trueLabel) {
		if (predictedLabel == 1 && trueLabel == 1)
			errorsAnalysis.incrementCount("TP", 1);
		else if (predictedLabel == 1 && trueLabel == 0) {
			errorsAnalysis.incrementCount("FP", 1);
		} else if (predictedLabel == 0 && trueLabel == 1)
			errorsAnalysis.incrementCount("FN", 1);
		else
			errorsAnalysis.incrementCount("TN", 1);

	}

	private void printAUCPoints(TreeSet<XYPair> xyData) {
		PrintToFile pf = new PrintToFile();
		pf.openFile(Classify.dataPath + "results" + Classify.slash + "auc"
				+ Classify.slash + ""
				+ Classify.intToCat.get(Classify.category) + Classify.slash
				+ "" + Classify.fileName + "_aucPoints.csv");
		xyData.add(new XYPair(0, 0));
		xyData.add(new XYPair(1, 1));
		for (XYPair pair : xyData)
			pf.writeToFile(pair.getX() + "," + pair.getY());
		pf.closeFile();

	}

	private double calculateAUC(TreeSet<XYPair> xyData) {

		xyData.add(new XYPair(0, 0));
		xyData.add(new XYPair(1, 1));
		double[][] aucPoints = new double[2][xyData.size()];
		int i = 0;
		for (XYPair xypair : xyData) {
			Double x = xypair.getX();
			Double y = xypair.getY();
			if (!x.isNaN() && !y.isNaN()) {
				aucPoints[0][i] = x;
				aucPoints[1][i] = y;
			} else {
				aucPoints[0][i] = 1;
				aucPoints[1][i] = 1;
			}

			i++;

		}
		double auc = TrapezoidRule.calculate(aucPoints[0], aucPoints[1]);
		return auc;
	}

	private void calculateRates(Counter<String> errorsAnalysis) {
		errorsAnalysis.setCount(
				"FPRate",
				errorsAnalysis.getCount("FP")
						/ (errorsAnalysis.getCount("FP") + errorsAnalysis
								.getCount("TN")));
		errorsAnalysis.setCount(
				"TPRate",
				errorsAnalysis.getCount("TP")
						/ (errorsAnalysis.getCount("FN") + errorsAnalysis
								.getCount("TP")));

	}
}
