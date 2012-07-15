package kokkodis.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.TreeSet;

import kokkodis.logitModel.Classify;
import kokkodis.utils.compare.XYPairComparator;

public class Evaluation {

	public Evaluation() {
		super();
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
	public void updateEvaluation(Counter<String> errorsAnalysis,
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
	 * True positive -> label =1; Tre Negative -> label = 0;
	 * 
	 * @param errorsAnalysis
	 * @param predictedLabel
	 * @param trueLabel
	 */
	public void updateEvaluation(Counter<String> errorsAnalysis,
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

	public void printAUCPoints(TreeSet<XYPair> xyData) {
		PrintToFile pf = new PrintToFile();
		pf.openFile(Classify.basePath + "results/"
				+ Classify.intToCat.get(Classify.baseFile) + "/"
				+ Classify.currentSolver + "_aucPoints_C" + Classify.Cstr
				+ "_I" + Classify.interceptStr + "_" + Classify.jobType
				+ ".csv");
		xyData.add(new XYPair(0, 0));
		xyData.add(new XYPair(1, 1));
		for (XYPair pair : xyData)
			pf.writeToFile(pair.getX() + "," + pair.getY());
		pf.closeFile();

	}

	public double calculateAUC(TreeSet<XYPair> xyData) {

		xyData.add(new XYPair(0, 0));
		xyData.add(new XYPair(1, 1));
		double[][] aucPoints = new double[2][xyData.size()];
		int i = 0;
		for (XYPair xypair : xyData) {
			Double x = xypair.getX();
			Double y = xypair.getY();
			// System.out.println(i+" x:"+x+" y:"+y);
			if (!x.isNaN() && !y.isNaN()) {
				// System.out.println("nan");
				aucPoints[0][i] = x;
				aucPoints[1][i] = y;
			} else {
				// System.out.println("zero");
				aucPoints[0][i] = 1;
				aucPoints[1][i] = 1;
			}

			i++;

		}
		double auc = TrapezoidRule.calculate(aucPoints[0], aucPoints[1]);
		return auc;
	}

	public void calculateRates(Counter<String> errorsAnalysis) {
		// System.out.println(errorsAnalysis.getCount("FP") +" "+ errorsAnalysis
		// .getCount("TN"));
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
