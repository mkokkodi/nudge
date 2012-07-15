package kokkodis.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.TreeSet;

import kokkodis.logitModel.Classify;
import kokkodis.utils.compare.TSComparator;
import kokodis.holders.ProbHolder;

public class ProbabilisticAnalysis {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	public static void doProbabilisticAnalysis() {
		try {
			BufferedReader input = new BufferedReader(new FileReader(new File(
					Classify.basePath + "probs/prob" + "_"
							+ Classify.currentSolver + "_C" + Classify.Cstr
							+ "_I" + Classify.interceptStr + "_"
							+ Classify.jobType +Classify.testType+ "_" + Classify.baseFile 
							+ Classify.removedFeat + ".csv")));
			String line;
			line = input.readLine();
			TreeSet<ProbHolder> ts = new TreeSet<ProbHolder>(new TSComparator());

			int i = 0;
			while ((line = input.readLine()) != null) {
				String[] tmpAr = line.split(",");
				ProbHolder ph = new ProbHolder(Double.parseDouble(tmpAr[0]
						.trim()), Integer.parseInt(tmpAr[1]));
				ts.add(ph);
				i++;
			}
			System.out.println(i + " tm.size:" + ts.size());

			PrintToFile pf = new PrintToFile();
			pf.openFile(Classify.basePath + "/probs/" + Classify.currentSolver
					+ "_" + Classify.C + "_probsComparison_" + Classify.jobType
					+ Classify.testType + "_" + Classify.baseFile
					+ Classify.removedFeat + ".csv");
			pf.writeToFile("Pr(predicted_positive>threshold), Total_predictions, Pr(actual_positive)");

			PrintToFile liftsFile = new PrintToFile();
			liftsFile.openFile(Classify.basePath + "/probs/"
					+ Classify.currentSolver + "_" + Classify.C + "_lift_"
					+ Classify.jobType + Classify.testType + "_" + Classify.baseFile
					 + Classify.removedFeat + ".csv");
			liftsFile.writeToFile("Predicted_prob_gt, lift");
			for (double th = 0.95; th > 0; th -= 0.05) {
				double positive = 0;
				double total = 0;
				for (ProbHolder ph : ts.headSet(new ProbHolder(th, -1))) {
					if (ph.isPositive())
						positive++;
					total++;

				}
				if (total > 20) {

					double actualProb = positive / total;
					DecimalFormat myFormatter = new DecimalFormat("#.###");
					String prc = myFormatter.format(actualProb);
					String thr = myFormatter.format(th + 0.025);
					double lift = actualProb / Classify.randomProbPositive;
					System.out.println("Threshold:" + thr + " Total:" + total
							+ " Positives:" + positive + " Actual Percentage:"
							+ prc + " Lift:" + lift);
					pf.writeToFile(thr + "," + (int) total + "," + prc);

					liftsFile.writeToFile(th + "," + lift);

				}
			}
			pf.closeFile();
			liftsFile.closeFile();
			// for (ProbHolder ph : ts)
			// System.out.println(ph.getProb() + ":" + ph.getLabel());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
