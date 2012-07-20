package kokkodis.logistic;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import kokkodis.holders.ProbHolder;
import kokkodis.utils.Counter;
import kokkodis.utils.Evaluation;
import kokkodis.utils.PrintToFile;
import kokkodis.utils.ProbabilisticAnalysis;
import kokkodis.utils.Utils;
import kokkodis.utils.XYPair;
import kokkodis.utils.compare.XYPairComparator;
import de.bwaldvogel.liblinear.InvalidInputDataException;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class Classify {
	public static String dataPath; // =
									// "/Users/mkokkodi/Desktop/bigFiles/nudge/";
	public static String currentSolver = "L1R_LR";// "L2R_L2LOSS_SVC";//"L1R_LR";
													// //
													// "L2R_LR"//L2R_L1LOSS_SVC_DUAL
													// ;L1R_LR
	public static String basePath;
	private static Utils u;
	public static String baseFile = "10";

	public static double randomProbPositive;
	public static double C = 1;
	public static String Cstr;
	public static String interceptStr;
	public static String jobType;
	public static HashMap<String, String> intToCat;
	public static String[] features;
	public static HashSet<Integer> featuresToRemove;
	public static String removedFeat = "";

	private static double intercept = 0;

	/* Flags */
	private static boolean createFiles = false;
	private static boolean buildModel = false;
	private static boolean predict = false;
	private static boolean verbal = false;
	private static boolean showWeights = false;
	private static String slash;
	private static double eps = 0.0000001;
	private static boolean hourly = true;
	private static boolean fixed = false;

	/**
	 * @param args
	 *            param 1: basefile -f param 2: solver (L1R_LR,L2R_LR) -s param
	 *            3: Penalty -C param 4: Intercept - I creaateFiles 5: -c build
	 *            model 6: -b predict : -p probabilistic analysis: -v
	 *            printWeights: -w
	 */

	public static void main(String[] args) {

		if (args.length > 0) {
			initialize();
			if (args[0].contains("-h"))
				printHelp();
			else if (args[0].contains("-features")) {
				showFeatures();
			} else {
				for (int i = 0; i < args.length; i++) {
					if (args[i].contains("-fixed")){
						fixed = true;
						hourly = false;
				}
					else if (args[i].contains("-f")) {
						baseFile = args[i + 1];
						i++;
					} else if (args[i].contains("-s")) {
						currentSolver = args[i + 1];
						i++;
					} else if (args[i].contains("-C")) {
						C = Double.parseDouble(args[i + 1].trim());
						i++;
						System.out.println("C=" + C);
					} else if (args[i].contains("-I")) {
						intercept = Double.parseDouble(args[i + 1]);
						i++;
						System.out.println("Intercept:" + intercept);
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

					} else if (args[i].contains("-e")) {
						eps = Double.parseDouble(args[i + 1].trim());
						i++;
					} 
					else if (args[i].contains("-r")) {

						String[] tmpAr = args[i + 1].split(",");
						i++;
						for (String s : tmpAr) {

							featuresToRemove.add(Integer.parseInt(s.trim()));

						}
						if (Classify.featuresToRemove.size() > 0) {
							for (int l : Classify.featuresToRemove) {
								removedFeat += "-" + l;
								// System.out.println(l);
							}
						}

					} else if (args[i].contains("-n")) {
						u.normalizeData();
					}
				}
				if (!fixed && !hourly && !createFiles) {
					System.out.println("You have to specify whether you want "
							+ "to build models for hourly (-hourly) or "
							+ "for fixed (-fixed) types of jobs.");

				} else {
					DecimalFormat myFormatter = new DecimalFormat("#.###");
					Cstr = myFormatter.format(C);
					interceptStr = myFormatter.format(intercept);

					if (createFiles) {
						System.out
								.println("Creating testing and training sets for category "
										+ baseFile);

						createDatasets();
						System.out
								.println("Testing and training sets created. ");
						System.out.println();
					}
					if (buildModel && hourly) {
						jobType = "Hourly";
						System.out.println("Building " + jobType 
								+ " Model...");

						buildModel();
						System.out
								.println("---------------------------------------------------------------");
						System.out.println("Model built. Saved in :" + "model/"
								+ currentSolver + "_C" + Cstr + "_I"
								+ interceptStr + "_" + jobType +  "_"
								+ baseFile + removedFeat);
						System.out.println();
					}
					if (buildModel && fixed) {
						jobType = "Fixed";
						System.out.println("Building " + jobType 
								+ " Model...");

						buildModel();
						System.out
								.println("---------------------------------------------------------------");
						System.out.println("Model built. Saved in :" + "model/"
								+ currentSolver + "_C" + Cstr + "_I"
								+ interceptStr + "_" + jobType  + "_"
								+ baseFile + removedFeat);
						System.out.println();
					}

					if (predict && hourly) {
						jobType = "Hourly";
						System.out
								.println("---------------------------------------------------------------");
						predict();
						System.out
								.println("---------------------------------------------------------------");
						System.out.println("Predictions stored in:"
								+ "data/results/" + intToCat.get(baseFile)
								+ "/testSetProbs_" + currentSolver + "_C"
								+ Cstr + "_I" + interceptStr + "_" + jobType
								+  removedFeat + ".csv");
						System.out.println();
					}
					if (predict && fixed) {
						jobType = "Fixed";
						System.out.println("Predicting instances in " + jobType
								+  " test set.");
						System.out
								.println("---------------------------------------------------------------");
						predict();
						System.out
								.println("---------------------------------------------------------------");
						System.out.println("Predictions stored in:"
								+ "data/results/" + intToCat.get(baseFile)
								+ "/testSetProbs_" + currentSolver + "_C"
								+ Cstr + "_I" + interceptStr + "_" + jobType
								+  removedFeat + ".csv");
						System.out.println();
					}
					if (verbal && hourly) {
						jobType = "Hourly";
						System.out
								.println("Starting analyzing probabilities...");
						System.out
								.println("---------------------------------------------------------------");
						ProbabilisticAnalysis.doProbabilisticAnalysis();
						System.out
								.println("---------------------------------------------------------------");
						System.out.println();

					}
					if (verbal && fixed) {
						jobType = "Fixed";
						System.out
								.println("Starting analyzing probabilities...");
						System.out
								.println("---------------------------------------------------------------");
						ProbabilisticAnalysis.doProbabilisticAnalysis();
						System.out
								.println("---------------------------------------------------------------");
						System.out.println();

					}
					if (showWeights && hourly) {
						System.out.println("Printing weights for:" + dataPath
								+ "model/" + currentSolver + "_C" + Cstr + "_I"
								+ interceptStr + "_" + "_" + jobType + 							 "_" + baseFile + removedFeat);
						System.out
								.println("---------------------------------------------------------------");
						indexFeatures();
						printWeights();
					}
					if (showWeights && fixed) {
						System.out.println("Printing weights for:" + dataPath
								+ "model/" + currentSolver + "_C" + Cstr + "_I"
								+ interceptStr + "_" + "_" + jobType 
								+ "_" + baseFile + removedFeat);
						System.out
								.println("---------------------------------------------------------------");
						indexFeatures();
						printWeights();
					}
				}

			}
		} else {
			printHelp();

		}

	}

	private static void showFeatures() {
		indexFeatures();
		for (int i = 0; i < features.length; i++)
			System.out.println((i + 1) + " : " + features[i]);

	}

	private static void initialize() {
		u = new Utils();
		slash = System.getProperty("file.separator");
		String path = System.getProperty("user.dir");
		// System.out.println(slash);
		dataPath = path.replaceAll("nudge_java", "").replaceAll("lib", "");
		basePath = dataPath;
		dataPath += "data" + slash;
		// System.out.println(basePath);
		intToCat = new HashMap<String, String>();
		intToCat.put("10", "10_web_dev");
		intToCat.put("20", "20_soft_dev");
		intToCat.put("30", "30_net_is");
		intToCat.put("40", "40_writing_translation");
		intToCat.put("50", "50_admin_support");
		intToCat.put("60", "60_design_mult");
		intToCat.put("70", "70_cust_service");
		intToCat.put("80", "80_sales_marketing");
		intToCat.put("90", "90_bus_services");
		featuresToRemove = new HashSet<Integer>();

	}

	private static void printHelp() {
		String s = "-f		basefile : 10,20,30 >>"
				+ "-s 		solver type: L1R_LR or L2R_LR  >>"
				+ "-C		Penalty parameter (double) >>"
				+ "-I		Intercept: 0 default. 1 for intercept. >>"
				+ "-c		create training and test files "
				+ "(0.85 - 0.15, vertical on contractors). Cretates training "
				+ "and test files for both hourly and fixed  instances.>>"
				+ "-b		Build model from training data. >>"
				+ "-p		Load model and predict. >>"
				+ "-v		Show probabilistic analysis. Also computes auc points: you can find them in ./results/ >>"
				+ "-w		print weights >>"
				+ "-e		eps: difference between objective function to stop itearting. Default:0.0000001 >>"
				+ "-fixed		Builds, predicts for fixed job-types data. Deafult is hourly. >>"
				+ "-r		Features to remove (-features to see the feature indices). >>"
				+ "-features		show features' indices. >>"
				+ "-n		normalize data. ";
		;

		System.out.println("Parameters:");
		System.out.println("---------------------------------------");
		for (String str : s.split(">>"))
			System.out.println(str);
		System.out.println("---------------------------------------");
	}

	private static void printWeights() {
		try {
			Model ml = Linear.loadModel(new File(dataPath + "model/"
					+ currentSolver + "_C" + Cstr + "_I" + interceptStr + "_"
					+ jobType + "_" + baseFile));

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

			int j = 0;
			//System.out.println(":" + w.length + "vs mine:"
				//	+ features.length);
			for (int i = 0; i < w.length; i++) {
			//	System.out.print(w[i]+" ");
				if (!featuresToRemove.contains(i + 1)) {
					tm.put(w[i], features[j]);
				//	System.out.println(features[j]);
					j++;
				}
			}

			for (Entry<Double, String> e : tm.entrySet()){
				if(e.getKey()>0)
					System.out.println(e.getValue() + " : " + e.getKey());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void predict() {
		Model ml = null;
		try {

			ml = Linear.loadModel(new File(dataPath + "model/" + currentSolver
					+ "_C" + Cstr + "_I" + interceptStr + "_" + jobType + "_"
					+ baseFile + removedFeat));
		} catch (IOException e) {
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

		Problem problem = loadProblem("testData/test" + baseFile + removedFeat
				+ ".txt");
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
		TreeSet<XYPair> xyData = null;
		HashMap<Double, Counter<String>> errorCounters = new HashMap<Double, Counter<String>>();
		if (verbal) {
			xyData = new TreeSet<XYPair>(new XYPairComparator());
			for (double th = 0.1; th < 0.95; th += 0.05) {
				errorCounters.put(th, new Counter<String>());

			}
		}
		/* end */

		// System.out.println("Predicting...");
		PrintToFile fileForProbAnalysis = new PrintToFile();

		fileForProbAnalysis.openFile(dataPath + "probs/prob_" + currentSolver
				+ "_C" + Cstr + "_I" + interceptStr + "_" + jobType 
				+ "_" + baseFile + removedFeat + ".csv");
		fileForProbAnalysis.writeToFile("probPositive,actual");

		PrintToFile testProbabilities = new PrintToFile();
		testProbabilities.openFile(dataPath + "results/"
				+ intToCat.get(baseFile) + "/testSetProbs_" + currentSolver
				+ "_C" + Cstr + "_I" + interceptStr + "_"  + jobType
				+ baseFile + removedFeat + ".csv");
		testProbabilities
				.writeToFile("cat,jobType,opening,contractor,pr_interview,true_label");

		Problem problem = loadProblem("testData/test" + jobType 
				+ baseFile + removedFeat + ".txt");

		ArrayList<ProbHolder> testHolder = u.loadHolders(dataPath
				+ "testData/test" + "Holder" + jobType  + baseFile
				+ removedFeat + ".csv");
		ListIterator<ProbHolder> it = testHolder.listIterator();
		for (int i = 0; i < problem.x.length; i++) {
			ProbHolder tempHolder = it.next();

			int prediction = Linear.predict(ml, problem.x[i]);
			int actual = problem.y[i];
			eval.updateEvaluation(errorAnalysis, prediction, actual);

			double[] probEstimates = new double[2];
			// probEstimates[1] is the probability of being 1, the probability
			// of being positive.
			Linear.predictProbability(ml, problem.x[i], probEstimates);
			testProbabilities.writeToFile(baseFile+","+jobType+","+tempHolder.getOpening() + ","
					+ tempHolder.getConractor() + "," + probEstimates[1] + ","
					+ actual);
			fileForProbAnalysis.writeToFile(probEstimates[1] + "," + actual);
			// for(int k=0; k<probEstimates.length; k++)
			// System.out.println(k+" "+probEstimates[k]);
			if (verbal) {
				for (double th = 0.1; th < 0.95; th += 0.05) {
					eval.updateEvaluation(errorCounters.get(th),
							probEstimates[1], th, actual);
				}
			}

		}

		fileForProbAnalysis.closeFile();
		double positiveInstances = errorAnalysis.getCount("TP")
				+ errorAnalysis.getCount("FN");
		double negativeInstances = errorAnalysis.getCount("FP")
				+ errorAnalysis.getCount("TN");

		System.out.println();
		System.out.println("Accuracies:");
		System.out.println("Baseline (major class):"
				+ Math.max(positiveInstances, negativeInstances)
				/ (positiveInstances + negativeInstances));
		double acc = (errorAnalysis.getCount("TP") + errorAnalysis
				.getCount("TN")) / errorAnalysis.totalCount();
		System.out.println(currentSolver + " (current model):" + acc);
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

		double baseline = Math.max(positiveInstances, negativeInstances)
				/ (positiveInstances + negativeInstances);
		randomProbPositive = 1 - baseline;
		/* for AUC */
		if (verbal) {
			for (double th = 0.1; th < 0.95; th += 0.05) {
				Counter<String> curCounter = errorCounters.get(th);
				eval.calculateRates(curCounter);
				xyData.add(new XYPair(curCounter.getCount("FPRate"), curCounter
						.getCount("TPRate")));
			}
			System.out.println("AUC:" + eval.calculateAUC(xyData));
			eval.printAUCPoints(xyData);
		}
		testProbabilities.closeFile();
	}

	private static void buildModel() {

		Problem problem = loadProblem("trainData/train" + jobType + baseFile
				+ removedFeat + ".txt");

		Parameter p = new Parameter(getSolverType(), C, eps);
		Model ml = Linear.train(problem, p);
		Linear.enableDebugOutput();
		// int [] predicted = new int[problem.l];
		// Linear.crossValidation(problem, p, 5, predicted );
		// for(int i:predicted)
		// System.out.println(i);

		try {  
			Linear.saveModel(new File(dataPath + "model/" + currentSolver
					+ "_C" + Cstr + "_I" + interceptStr + "_" + jobType + "_"
					+ baseFile + removedFeat), ml);

		} catch (IOException e) {
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
			System.out.println("Loading file " + file);
			problem = Problem
					.readFromFile(new File(dataPath + file), intercept);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidInputDataException e) {
			e.printStackTrace();
		}
		return problem;
	}

	private static void createDatasets() {
		u.createTrainTest(baseFile);

	}

	private static void indexFeatures() {

		features = u.getFeatures();

	}

}
