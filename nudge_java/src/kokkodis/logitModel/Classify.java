package kokkodis.logitModel;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
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
	public static String basePath;  //= "/Users/mkokkodi/Desktop/bigFiles/nudge/";
	public static String currentSolver = "L1R_LR";// "L2R_L2LOSS_SVC";//"L1R_LR";
													// //
													// "L2R_LR"//L2R_L1LOSS_SVC_DUAL
													// ;L1R_LR

	public static String baseFile = "10";

	public static double randomProbPositive;
	public static double C = 1;
	public static String Cstr;
	public static String interceptStr;

	private static double intercept = 0;
	public static String[] features;

	/* Flags */
	private static boolean createFiles = false;
	private static boolean buildModel = false;
	private static boolean predict = false;
	private static boolean verbal = false;
	private static boolean showWeights = false;
	private static String slash;

	/**
	 * @param args
	 *            param 1: basefile -f param 2: solver (L1R_LR,L2R_LR) -s param
	 *            3: Penalty -C param 4: Intercept - I creaateFiles 5: -c build
	 *            model 6: -b predict : -p probabilistic analysis: -v
	 *            printWeights: -w
	 */

	public static void main(String[] args) {

		initializePaths();

		if (args.length > 0) {

			if (args[0].contains("-h"))
				printHelp();
			else {
				for (int i = 0; i < args.length; i++) {
					if (args[i].contains("-f")) {
						baseFile = args[i + 1];
						i++;
					} else if (args[i].contains("-s")) {
						currentSolver = args[i + 1];
						i++;
					} else if (args[i].contains("-C")) {
						C = Double.parseDouble(args[i + 1].trim());
						i++;
					} else if (args[i].contains("-I")) {
						intercept = Double.parseDouble(args[i + 1]);
						i++;
					} else if (args[i].contains("-c")) {
						createFiles = true;

					} else if (args[i].contains("-b")) {
						buildModel = true;

					} else if (args[i].contains("-p")) {
						predict = true;

					} else if (args[i].contains("-v")) {
						verbal = true;

					} else if (args[i].contains("-w")) {
						showWeights = true;

					}
				}
				DecimalFormat myFormatter = new DecimalFormat("#.###");
				Cstr = myFormatter.format(C);
				interceptStr = myFormatter.format(intercept);

				System.out.println("Running file cat" + baseFile);
				if (createFiles)
					createDatasets();
				if (buildModel)
					buildModel();

				if (predict)
					predict();
				if (verbal)
					ProbabilisticAnalysis.doProbabilisticAnalysis();

				if (showWeights) {
					indexFeatures();
					printWeights();
				}

			}
		} else {
			printHelp();

		}

	}

	private static void initializePaths() {
		slash = System.getProperty("file.separator");
		String path =Thread.currentThread().getContextClassLoader().getResource(".").getPath();
	//	System.out.println(slash);
		basePath = path.replaceAll("nudge_java"+slash,  "").replaceAll("bin"+slash,"");
		basePath += "data"+slash;
		//System.out.println(basePath);

		
	}

	private static void printHelp() {
		String s = "-f		basefile : 10,20,30 >>"
				+ "-s 		solver type: L1R_LR or L2R_LR  >>"
				+ "-C		Penalty parameter (double) >>"
				+ "-I		Intercept: 0 default. 1 for intercept. >>"
				+ "-c		create training and test files (0.85 - 0.15, vertical on contractors) >>"
				+ "-b		Build model from training data. >>"
				+ "-p		Load model and predict. >>"
				+ "-v		Show probabilistic analysis >>" + "-w		print weights";
		System.out.println("Parameters:");
		System.out.println("---------------------------------------");
		for (String str : s.split(">>"))
			System.out.println(str);
		System.out.println("---------------------------------------");
	}

	private static void printWeights() {
		try {
			Model ml = Linear.loadModel(new File(basePath + "model/"
					+ currentSolver + "_C" + Cstr + "_I" + interceptStr + "_"
					+ baseFile));
			System.out.println("Printing weights for:" + basePath + "model/"
					+ currentSolver + "_C" + Cstr + "_I" + interceptStr + "_"
					+ baseFile);
			double[] w = ml.getFeatureWeights();
			// System.out.print(baseFile);
			TreeMap<Double, String> tm = new TreeMap<Double, String>(
					new Comparator<Double>() {

						@Override
						public int compare(Double o1, Double o2) {
							return (Math.abs(o1.doubleValue()) > Math.abs(o2
									.doubleValue()) ? -1 : 1);
						}
					});

			for (int i = 0; i < w.length; i++) {
				tm.put(w[i], features[i]);
			}

			for (Entry<Double, String> e : tm.entrySet())
				System.out.println(e.getValue() + " : " + e.getKey());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void predict() {
		Model ml = null;
		try {

			ml = Linear.loadModel(new File(basePath + "model/" + currentSolver
					+ "_C" + Cstr + "_I" + interceptStr + "_" + baseFile));
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
		pf.openFile(basePath + "probs/prob_" + currentSolver + "_C" + Cstr
				+ "_I" + interceptStr + "_" + baseFile + ".csv");
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

		/* for AUC */
		for (double th = 0.1; th < 0.95; th += 0.05) {
			Counter<String> curCounter = errorCounters.get(th);
			eval.calculateRates(curCounter);
			xyData.add(new XYPair(curCounter.getCount("FPRate"), curCounter
					.getCount("TPRate")));
		}
		// System.out.println("AUC:" + eval.calculateAUC(xyData));
		double baseline = Math.max(positiveInstances, negativeInstances)
				/ (positiveInstances + negativeInstances);
		randomProbPositive = 1 - baseline;
		/*
		 * System.out.println(baseFile+","+baseline+","+acc+","+eval.calculateAUC
		 * (xyData)+","
		 * +errorAnalysis.getCount("TP")+","+errorAnalysis.getCount("FP")+","
		 * +errorAnalysis.getCount("TN")+","+errorAnalysis.getCount("FN"));
		 */
		System.out.println("AUC:" + eval.calculateAUC(xyData));
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
			Linear.saveModel(new File(basePath + "model/" + currentSolver
					+ "_C" + Cstr + "_I" + interceptStr + "_" + baseFile), ml);
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
			problem = Problem
					.readFromFile(new File(basePath + file), intercept);
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
		u.createTrainTest("cat" + baseFile + ".csv", baseFile);

	}

	private static void indexFeatures() {
		Utils u = new Utils();
		features = u.getFeatures();

	}

}
