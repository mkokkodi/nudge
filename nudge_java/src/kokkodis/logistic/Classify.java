/****************************************************
 * @author Marios Kokkodis                          *
 * comments/questions : mkokkodi@odesk.com     		*
 *													*					  
 *  Class Description : Calls the liblinear         *
 *  classifiers. In particular is built for         *
 *  logistic regression (L1 and L2). From           *
 *  this class you can call functions to            *
 *  create the datasets, to build a model,          *
 *  and to make prdictions. Use the option          *
 *  "-h" to see all the available uses.        		*	
 *													*  
 * 	*************************************************									
 */

package kokkodis.logistic;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;

import kokkodis.utils.BuildUtils;
import kokkodis.utils.PredictUtils;
import kokkodis.utils.CreateUtils;
import kokkodis.utils.Utils;

public class Classify {

	public static String slash; // system slash "/" for unix, "\" for win.
	public static String dataPath;
	public static String currentSolver = "L1R_LR"; // default solver -L1
													// regularized logistic
													// regression.
	public static String basePath;
	public static String category = "10";
	public static String jobType = "Hourly"; // Hourly or Fixed.

	public static String[] features; // array of features.
	public static HashSet<Integer> featuresToRemove; // what features to be
														// removed.
	public static double C = 1; // penalty parameter
	public static double intercept = 0; // whether we include an intercept in
										// our model.
	public static double eps = 0.0000001; // default stopping value of
											// minimizer.

	/* Flags */
	private static boolean createFiles = false;
	private static boolean buildModel = false;
	private static boolean predict = false;
	private static boolean verbal = false;
	private static boolean showWeights = false;
	public static boolean normalize = false;

	/* Additional helping variables */

	public static String Cstr;
	public static String interceptStr;
	public static String removedFeat = "";
	public static CreateUtils createUtils;
	private static PredictUtils predictUtils;
	private static BuildUtils buildUtils;
	private static Utils utils;
	public static HashMap<String, String> intToCat; // maps category number to

	// string path -> for
	// printing output files.

	public static String fileName = "";
	public static String trainTestName = "";

	/**
	 * 
	 * @param args
	 *            Use -h to see all available options.
	 */

	public static void main(String[] args) {
		initialize();
		if (args.length > 0) {

			if (args[0].contains("-h"))
				utils.printHelp();
			else if (args[0].contains("--features")) {
				utils.showFeatures();
			} else {
				for (int i = 0; i < args.length; i++) {
					if (args[i].contains("-f")) {
						jobType = "Fixed";
					} else if (args[i].contains("-c")) {
						category = args[i + 1];
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
					} else if (args[i].contains("-g")) {
						createUtils = new CreateUtils();
						createFiles = true;

					} else if (args[i].contains("-b")) {
						buildUtils = new BuildUtils();
						buildModel = true;

					} else if (args[i].contains("-p")) {
						predictUtils = new PredictUtils();
						predict = true;

					} else if (args[i].contains("-v")) {
						verbal = true;

					} else if (args[i].contains("-w")) {
						showWeights = true;

					} else if (args[i].contains("-e")) {
						eps = Double.parseDouble(args[i + 1].trim());
						i++;
					} else if (args[i].contains("-r")) {

						String[] tmpAr = args[i + 1].split(",");
						i++;
						for (String s : tmpAr) {

							featuresToRemove.add(Integer.parseInt(s.trim()));

						}
						if (Classify.featuresToRemove.size() > 0) {
							for (int l : Classify.featuresToRemove) {
								removedFeat += "-" + l;
							}
						}

					} else if (args[i].contains("-n")) {
						normalize = true;
						createUtils.normalizeData();
					}
				}

				DecimalFormat myFormatter = new DecimalFormat("#.###");
				Cstr = myFormatter.format(C);
				interceptStr = myFormatter.format(intercept);

				fileName = currentSolver + "_C" + Cstr + "_I" + interceptStr
						+ "_" + jobType + "_" + category + removedFeat
						+ ((normalize) ? "_normalized" : "");

				trainTestName = jobType + category + removedFeat
						+ ((normalize) ? "_normalized" : "");

				if (createFiles) {
					appendStartLine("Creating testing and training sets for category "
							+ category);
					createUtils.generateTrainTest(category);
					appendEndLine("Testing and training sets created. ");

				}
				if (buildModel) {

					appendStartLine("Building " + jobType + " Model...");

					buildUtils.buildModel();
					appendEndLine("Model built. Saved in :" + "model/"
							+ fileName);
				}

				if (predict) {
					appendStartLine("Predicting instances in " + jobType
							+ " test set.");
					predictUtils.predictProbModel(verbal);
					appendEndLine("Predictions stored in:" + "data/results/"
							+ intToCat.get(category) + "/testSetProbs_"
							+ currentSolver + "_C" + Cstr + "_I" + interceptStr
							+ "_" + jobType + removedFeat + ".csv");
				}

				if (showWeights) {
					appendStartLine("Printing weights for:" + dataPath
							+ "model/" + currentSolver + "_C" + Cstr + "_I"
							+ interceptStr + "_" + "_" + jobType + "_"
							+ category + removedFeat);
					utils.indexFeatures();
					utils.printWeights();
				}

			}
		} else {
			utils.printHelp();

		}

	}

	private static void initialize() {
		utils = new Utils();
		slash = System.getProperty("file.separator");
		String path = System.getProperty("user.dir");
		dataPath = path.replaceAll("nudge_java", "").replaceAll("lib", "");
		basePath = dataPath;
		dataPath += "data" + slash;
		intToCat = new HashMap<String, String>();
		intToCat.put("0","0_testing_mode");
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

	private static void appendStartLine(String string) {
		System.out.println(string);
		System.out
				.println("---------------------------------------------------------------");

	}

	private static void appendEndLine(String string) {
		System.out.println(string);
		System.out
				.println("---------------------------------------------------------------");
		System.out.println();

	}

}
