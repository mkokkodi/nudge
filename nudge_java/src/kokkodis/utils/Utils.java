/****************************************************
 * @author Marios Kokkodis                          *
 * comments/questions : mkokkodi@odesk.com     		*
 *													*					  
 *  Class Description    - Utils              		*	
 *													*  
 * 	*************************************************									
 */



package kokkodis.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import kokkodis.holders.ProbHolder;
import kokkodis.logistic.Classify;

import de.bwaldvogel.liblinear.InvalidInputDataException;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;

public class Utils {

	
	protected static HashMap<String, Integer> countriesProductMap;
	protected static HashMap<Integer, String> indexToCountriesProduct;
	protected int locationProductIndex = 0;
	
	public Utils() {
	}
	
	
	protected HashMap<String, Integer> readContriesProducts() {
		HashMap<String, Integer> hm = new HashMap<String, Integer>();
		indexToCountriesProduct = new HashMap<Integer, String>();
		try {

			BufferedReader input = new BufferedReader(new FileReader(
					Classify.basePath
							+ "nudge_java/lexicon/countriesProduct.csv"));
			String line;
			line = input.readLine();

			while ((line = input.readLine()) != null) {
				String[] tmpAr = line.split("\",\"");

				for (int i = 0; i < tmpAr.length; i++) {
					tmpAr[i] = tmpAr[i].replaceAll("\"", "");
				}
				hm.put(tmpAr[1] + ">" + tmpAr[0], locationProductIndex);
				indexToCountriesProduct.put(locationProductIndex, tmpAr[1]
						+ ">" + tmpAr[0]);
				locationProductIndex++;

			}
			return hm;
		} catch (IOException e) {
		}
		return null;
	}
	
	public String[] getFeatures() {
		if (indexToCountriesProduct == null)
			readContriesProducts();
		String headings = "english,hourly_rate (or agency rate),"
				+ "availability_hrs,total_tests,yrs_exp, no_qualifications,"
				+ "total_hours,adjusted_score,adjusted_score_recent,total_last_90_days,"
				+ "english_score_diff, pref_feedback_score_diff, prc_interviewed,"
				+ "timezoneDiff, contr_location, "
				+
				// "cover_unigram_score_diff,
				"cover_unigram_score,"
				+ "order_of_application,client_country,same_client_contr_country, pref_has_portfolio,number_prev_openings,"
				// + "job_unigram_score_diff,"
				+ "job_unigram_score,pref_test_diff, number_prev_applications, "
				+ "contractor_hired_from_current_country, worked_together_before";
		// + "intercept";

		String[] tmpAr = headings.split(",");
		// System.out.println("Size b4:"+tmpAr.length);
		for (int ind : Classify.featuresToRemove) {
			System.out.println("we are removing feature " + tmpAr[ind - 1]
					+ " with index " + ind);
			tmpAr[ind - 1] = null;

		}
		String[] res = new String[tmpAr.length
				- Classify.featuresToRemove.size()
				+ indexToCountriesProduct.size() + 1];// +1 for the intercept.
		int j = 0;
		for (int i = 0; i < tmpAr.length; i++) {
			if (tmpAr[i] != null) {
				res[j] = tmpAr[i];
				j++;
			}
		}

		/*
		 * Print Product features
		 */
		int startingInd = tmpAr.length - Classify.featuresToRemove.size();
		// System.out.println(" after:"+res.length);

		for (int i = startingInd; i < res.length; i++) {
			res[i] = indexToCountriesProduct.get(i - startingInd);
		}
		res[res.length - 1] = "intercept";
		return res;

	}

	
	public void printHelp() {
		String s = "-c			category : 10,20,30... >>"
				+ "-s 			solver type: L1R_LR or L2R_LR  >>"
				+ "-C			Penalty parameter (double) >>"
				+ "-I			Intercept: 0 default. 1 for intercept. >>"
				+ "-g			generate training and test files >>"
				+ "-b			Build model from training data. >>"
				+ "-p			Load model and predict. >>"
				+ "-v			Show probabilistic analysis. Also computes auc points: you can find them in ./results/ >>"
				+ "-w			print weights >>"
				+ "-e			eps: difference between objective function to stop itearting. Default:0.0000001 >>"
				+ "-f			Builds, predicts for fixed job-types data. Deafult is hourly. >>"
				+ "-r			Features to remove (-features to see the feature indices). >>"
				+ "--features		show features' indices. >>"
				+ "-n			normalize data. ";
		;

		System.out.println("Available options:");
		System.out.println("------------------------------------------------------------------");
		for (String str : s.split(">>"))
			System.out.println(str);
		System.out.println("------------------------------------------------------------------");
	}
	
	
	public  void printWeights() {
		try {
			Model ml = Linear.loadModel(new File(Classify.dataPath + "model/"
					+ Classify.currentSolver + "_C" + Classify.Cstr + "_I" + Classify.interceptStr + "_"
					+ Classify.jobType + "_" + Classify.category));

			double[] w = ml.getFeatureWeights();
			TreeMap<Double, String> tm = new TreeMap<Double, String>(
					new Comparator<Double>() {

						@Override
						public int compare(Double o1, Double o2) {
							return (Math.abs(o1.doubleValue()) > Math.abs(o2
									.doubleValue()) ? -1 : 1);
						}
					});

			int j = 0;
			for (int i = 0; i < w.length; i++) {
				if (!Classify.featuresToRemove.contains(i + 1)) {
					tm.put(w[i], Classify.features[j]);
					j++;
				}
			}

			System.out.println("| Feature \t|\t Weight |");
			System.out.println("------------------------------------");
			for (Entry<Double, String> e : tm.entrySet()) {
				if (e.getKey() > 0)
					System.out.println(e.getValue() + " | " + e.getKey());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public  void indexFeatures() {

		Classify.features = Classify.createUtils.getFeatures();

	}
	public  void showFeatures() {
		indexFeatures();
		for (int i = 0; i < Classify.features.length; i++)
			System.out.println((i + 1) + " : " + Classify.features[i]);

	}
	
	protected ArrayList<ProbHolder> loadHolders(String f) {
		System.out.println("Loading holders:"+f);
		ArrayList<ProbHolder> l = new java.util.ArrayList<ProbHolder>();
		try {
			BufferedReader input = new BufferedReader(new FileReader(f));
			String line;
			line = input.readLine();

			while ((line = input.readLine()) != null) {
				String[] tmpAr = line.split(",");
				ProbHolder ph = new ProbHolder(0, 0);
				ph.setOpening(tmpAr[0]);
				ph.setConractor(tmpAr[1]);
				l.add(ph);
			}
		} catch (IOException e) {
		}
		return l;
	}

	protected Problem loadProblem(String file) {
		Problem problem = null;
		try {
			// problem = Problem.readFromFile(q.createTestFile(), 1);
			System.out.println("Loading file " + file);
			problem = Problem.readFromFile(new File(Classify.dataPath + file),
					Classify.intercept);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidInputDataException e) {
			e.printStackTrace();
		}
		return problem;
	}

	protected SolverType getSolverType() {
		if (Classify.currentSolver.equals("L2R_LR"))
			return SolverType.L2R_LR;
		if (Classify.currentSolver.equals("L1R_LR"))
			return SolverType.L1R_LR;
		else {
			System.out
					.println("Unrecognized solver name. Using L1R_LR insread.");
			return SolverType.L1R_LR;
		}
	}
}
