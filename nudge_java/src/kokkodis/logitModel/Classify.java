package kokkodis.logitModel;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeSet;

import kokkodis.db.OdeskDBQueries;
import kokkodis.factory.XYPair;
import kokkodis.utils.Counter;
import kokkodis.utils.Evaluation;
import kokkodis.utils.compare.XYPairComparator;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.InvalidInputDataException;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import de.bwaldvogel.liblinear.Train;

public class Classify {
	public static String basePath = "/Users/mkokkodi/Documents/workspace/nudge_java/";
	public static String curentSolver = "L2R_LR";//"L2R_L2LOSS_SVC";//"L1R_LR"; // "L2R_LR";

	/**
	 * @param args
	 */

	public static void main(String[] args) {

		createDatasets();

		buildModel();
		predict();

	}

	private static void predict() {
		Model ml = null;
		try {
			ml = Linear.loadModel(new File(basePath + "model/" + curentSolver));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(ml.isProbabilityModel()){
			predictProbModel(ml);
		}else
			predictNonProbModel(ml);
	
	}

	private static void predictNonProbModel(Model ml) {
	Counter<String> errorAnalysis = new Counter<String>();
		
		Evaluation eval = new Evaluation();
	
		Problem problem = loadProblem("data/test.txt");
		for (int i = 0; i < problem.x.length; i++) {
			int prediction = Linear.predict(ml, problem.x[i]);
			int actual = problem.y[i];
			eval.updateEvaluation(errorAnalysis, prediction, actual);
			
	
		}
		double positiveInstances = errorAnalysis.getCount("TP")
				+ errorAnalysis.getCount("FN");
		double negativeInstances = errorAnalysis.getCount("FP")
				+ errorAnalysis.getCount("TN");

		System.out.println("majorityVoting:"
				+ Math.max(positiveInstances, negativeInstances)
				/ (positiveInstances + negativeInstances));
		double acc = (errorAnalysis.getCount("TP") + errorAnalysis
				.getCount("TN")) / errorAnalysis.totalCount();
		System.out.println("Our model's acc:" + acc);
		System.out.println("       |     Actual    | ");
		System.out.println("------ |   +   |   -   | ");
		System.out.println("  +    | " + (int) errorAnalysis.getCount("TP")
				+ " | " + (int) errorAnalysis.getCount("FP") + " | ");
		System.out.println("  -    | " + (int) errorAnalysis.getCount("FN")
				+ " | " + (int) errorAnalysis.getCount("TN") + " | ");


		
	}

	private static void predictProbModel(Model ml) {
	Counter<String> errorAnalysis = new Counter<String>();
		
		Evaluation eval = new Evaluation();
		/* For AUC intatiate XYPair. */
		TreeSet<XYPair> xyData = new TreeSet<XYPair>(new XYPairComparator());
		HashMap<Double, Counter<String>> errorCounters = new HashMap<Double, Counter<String>>();
		for (double th = 0.1; th < 0.95; th += 0.05) {
			errorCounters.put(th, new Counter<String>());

		}
		/* end */
	
		Problem problem = loadProblem("data/train.txt");
		for (int i = 0; i < problem.x.length; i++) {
			int prediction = Linear.predict(ml, problem.x[i]);
			int actual = problem.y[i];
			eval.updateEvaluation(errorAnalysis, prediction, actual);
			
			double[] probEstimates = new double[2];
			// probEstimates[1] is the probability of being 1, the probability
			// of being positive.
			Linear.predictProbability(ml, problem.x[i], probEstimates);
			for (double th = 0.1; th < 0.95; th += 0.05) {
				eval.updateEvaluation(errorCounters.get(th), probEstimates[0],
						th, actual); //probEstimates[0] = probability of being positive.

			}

		}
		double positiveInstances = errorAnalysis.getCount("TP")
				+ errorAnalysis.getCount("FN");
		double negativeInstances = errorAnalysis.getCount("FP")
				+ errorAnalysis.getCount("TN");

		System.out.println("majorityVoting:"
				+ Math.max(positiveInstances, negativeInstances)
				/ (positiveInstances + negativeInstances));
		double acc = (errorAnalysis.getCount("TP") + errorAnalysis
				.getCount("TN")) / errorAnalysis.totalCount();
		System.out.println("Our model's acc:" + acc);
		System.out.println("       |     Actual    | ");
		System.out.println("------ |   +   |   -   | ");
		System.out.println("  +    | " + (int) errorAnalysis.getCount("TP")
				+ " | " + (int) errorAnalysis.getCount("FP") + " | ");
		System.out.println("  -    | " + (int) errorAnalysis.getCount("FN")
				+ " | " + (int) errorAnalysis.getCount("TN") + " | ");

		/* for AUC */
		for (double th = 0.1; th < 0.95; th += 0.05) {
			Counter<String> curCounter = errorCounters.get(th);
			eval.calculateRates(curCounter);
			xyData.add(new XYPair(curCounter.getCount("FPRate"), curCounter
					.getCount("TPRate")));
		}
		System.out.println("AUC:" + eval.calculateAUC(xyData));
		
	}

	private static void buildModel() {
		Problem problem = loadProblem("data/train.txt");

		Parameter p = new Parameter(getSolverType(), 1, 0.00001);
		Model ml = Linear.train(problem, p);
		Linear.enableDebugOutput();
		// int [] predicted = new int[problem.l];
		// Linear.crossValidation(problem, p, 5, predicted );
		// for(int i:predicted)
		// System.out.println(i);
		double[] w = ml.getFeatureWeights();
		System.out.println("Weights");
		for (double w1 : w)
			System.out.print(w1 + " ");

		try {
			Linear.saveModel(new File(basePath + "model/" + curentSolver), ml);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * double [] probEstimates = new double[2]; for (int i = 0; i <
		 * problem.x.length; i++){ Linear.predictProbability(ml,
		 * problem.x[i],probEstimates); System.out.println("["+problem.x[i][0]+
		 * " "+problem.x[i][1]+"]"+" Prob label=1:" +probEstimates[0]); }
		 */

	}

	private static SolverType getSolverType() {
		if (curentSolver.equals("L2R_LR"))
			return SolverType.L2R_LR;
		if (curentSolver.equals("L1R_LR"))
			return SolverType.L1R_LR;
		if(curentSolver.equals("L2R_L2LOSS_SVC"))
			return SolverType.L2R_L2LOSS_SVC;
		return null;
	}

	private static Problem loadProblem(String file) {
		Problem problem = null;
		try {
			// problem = Problem.readFromFile(q.createTestFile(), 1);
			System.out.println("Loading");
			problem = Problem.readFromFile(new File(basePath + file), 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidInputDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return problem;
	}

	private static void createDatasets() {
		OdeskDBQueries q = new OdeskDBQueries();
		q.connect();
		q.createTrainingFile();

	}

}
