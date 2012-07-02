package kokkodis.logitModel;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.TreeSet;

import org.postgresql.ds.common.BaseDataSource;

import kokkodis.db.OdeskDBQueries;
import kokkodis.factory.XYPair;
import kokkodis.utils.Counter;
import kokkodis.utils.Evaluation;
import kokkodis.utils.PrintToFile;
import kokkodis.utils.Utils;
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
	public static String basePath = "/Users/mkokkodi/Desktop/bigFiles/nudge/";
	public static String currentSolver = "L2R_LR";// "L2R_L2LOSS_SVC";//"L1R_LR";
													// //
													// "L2R_LR"//L2R_L1LOSS_SVC_DUAL
													// ;L1R_LR
	
	public static String baseFile="2_5m";
	public static double randomProbPositive;
	public static double C=1;
	public static String Cstr;

	/**
	 * @param args
	 */

	public static void main(String[] args) {

		String[] baseNames = {"500k", "1m", "2m","2_5m","3m" };
		String[] probSolvers = {"L2R_LR_DUAL"};
		for (String str : baseNames) {
			baseFile = str;
		//for(String str: probSolvers){
			//currentSolver = str;
	//	baseFile = "3m";
		System.out.println("Running "+baseFile);
		//	if(str.equals("2_5m")){
		//	createDatasets();
		//for(double c=0.5; c>0.01; c *= c){
		//for(double c=2; c<65; c*=c ){
	//	C=c;
	
		DecimalFormat myFormatter = new DecimalFormat("#.###");
			Cstr = myFormatter.format(C);

	//		buildModel();
			predict();
		
		//	ProbabilisticAnalysis.doProbabilisticAnalysis();
			//}
			//printWeights();
		}

	}

	private static void printWeights() {
		try {
			Model ml = Linear.loadModel(new File(basePath + "model/" + currentSolver
					+ "_" + baseFile));
			double[] w = ml.getFeatureWeights();
			System.out.print(baseFile);
			for (double w1 : w)
				System.out.print(","+w1 );
			System.out.println();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void predict() {
		Model ml = null;
		try {
			ml = Linear.loadModel(new File(basePath + "model/" + currentSolver + "_C"+Cstr
					+ "_" + baseFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (ml.isProbabilityModel()) {
			predictProbModel(ml);
		} else
			predictNonProbModel(ml);

	}

	private static void predictNonProbModel(Model ml) {
		Counter<String> errorAnalysis = new Counter<String>();

		Evaluation eval = new Evaluation();

		Problem problem = loadProblem("testData/test" + baseFile + ".txt");
		for (int i = 0; i < problem.x.length; i++) {
			int prediction = Linear.predict(ml, problem.x[i]);
			int actual = problem.y[i];
			eval.updateEvaluation(errorAnalysis, prediction, actual);

		}
		double positiveInstances = errorAnalysis.getCount("TP")
				+ errorAnalysis.getCount("FN");
		double negativeInstances = errorAnalysis.getCount("FP")
				+ errorAnalysis.getCount("TN");

		System.out.println("majorityClass:"
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
		// System.out.println("Predicting...");
		PrintToFile pf = new PrintToFile();
		pf.openFile(basePath + "probs/prob_" +   currentSolver+"_C"+Cstr+"_"+baseFile + ".csv");
		pf.writeToFile("probPositive,actual");

		Problem problem = loadProblem("testData/test" + baseFile + ".txt");
		for (int i = 0; i < problem.x.length; i++) {
			int prediction = Linear.predict(ml, problem.x[i]);
			int actual = problem.y[i];
			eval.updateEvaluation(errorAnalysis, prediction, actual);

			double[] probEstimates = new double[2];
			// probEstimates[1] is the probability of being 1, the probability
			// of being positive.
			Linear.predictProbability(ml, problem.x[i], probEstimates);
			pf.writeToFile(probEstimates[1] + "," + actual);
			// for(int k=0; k<probEstimates.length; k++)
			// System.out.println(k+" "+probEstimates[k]);
			for (double th = 0.1; th < 0.95; th += 0.05) {
				eval.updateEvaluation(errorCounters.get(th), probEstimates[1],
						th, actual);

			}

		}
		pf.closeFile();
		double positiveInstances = errorAnalysis.getCount("TP")
				+ errorAnalysis.getCount("FN");
		double negativeInstances = errorAnalysis.getCount("FP")
				+ errorAnalysis.getCount("TN");

//		System.out.println("majorityClass:"
	//			+ Math.max(positiveInstances, negativeInstances)
		//		/ (positiveInstances + negativeInstances));
		double acc = (errorAnalysis.getCount("TP") + errorAnalysis
				.getCount("TN")) / errorAnalysis.totalCount();
/*		System.out.println("Our model's acc:" + acc);
		System.out.println("       |     Actual    | ");
		System.out.println("------ |   +   |   -   | ");
		System.out.println("  +    | " + (int) errorAnalysis.getCount("TP")
				+ " | " + (int) errorAnalysis.getCount("FP") + " | ");
		System.out.println("  -    | " + (int) errorAnalysis.getCount("FN")
				+ " | " + (int) errorAnalysis.getCount("TN") + " | ");
*/
		/* for AUC */
		for (double th = 0.1; th < 0.95; th += 0.05) {
			Counter<String> curCounter = errorCounters.get(th);
			eval.calculateRates(curCounter);
			xyData.add(new XYPair(curCounter.getCount("FPRate"), curCounter
					.getCount("TPRate")));
		}
		//System.out.println("AUC:" + eval.calculateAUC(xyData));
		double baseline = Math.max(positiveInstances, negativeInstances)
				/ (positiveInstances + negativeInstances);
		randomProbPositive = 1 - baseline;
		System.out.println(baseFile+","+baseline+","+acc+","+eval.calculateAUC(xyData)+"," 
				+errorAnalysis.getCount("TP")+","+errorAnalysis.getCount("FP")+","
				+errorAnalysis.getCount("TN")+","+errorAnalysis.getCount("FN"));
		eval.printAUCPoints(xyData);
	}

	private static void buildModel() {
		Problem problem = loadProblem("trainData/train" + baseFile + ".txt");

		Parameter p = new Parameter(getSolverType(), C, 0.0000001);
		Model ml = Linear.train(problem, p);
		Linear.enableDebugOutput();
		// int [] predicted = new int[problem.l];
		// Linear.crossValidation(problem, p, 5, predicted );
		// for(int i:predicted)
		// System.out.println(i);
		
		try {
			Linear.saveModel(new File(basePath + "model/" + currentSolver + "_C" + Cstr+"_"
					+ baseFile), ml);
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
		if (currentSolver.equals("L2R_LR"))
			return SolverType.L2R_LR;
		if (currentSolver.equals("L1R_LR"))
			return SolverType.L1R_LR;
		if (currentSolver.equals("L2R_L2LOSS_SVC"))
			return SolverType.L2R_L2LOSS_SVC;
		if (currentSolver.equals("L2R_L1LOSS_SVC_DUAL"))
			return SolverType.L2R_L1LOSS_SVC_DUAL;
		if (currentSolver.equals("L2R_LR_DUAL"))
				return SolverType.L2R_LR_DUAL;
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
		Utils u = new Utils();
		u.createTrainingFile("train" + baseFile + ".csv");

	}

}
