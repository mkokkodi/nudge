package kokkodis.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import kokkodis.logitModel.Classify;
import kokodis.holders.ContractorHolder;
import kokodis.holders.ProbHolder;

public class Utils {

	private boolean labelIsZero = false; // boolean to make sure that the

	private HashMap<String, Integer> locationToNominal = new HashMap<String, Integer>();

	private int locationIndex = 0;

	// probEstimates[0] are for the negative
	// class (i.e. 0).
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		createCSV();
	}

	private static void createCSV() {
		try {
			PrintToFile pf = new PrintToFile();
			pf.openFile("/Users/mkokkodi/Documents/workspace/nudge_java/data/seedskills.csv");
			File f = new File(
					"/Users/mkokkodi/Documents/workspace/nudge_java/data/seedskills.json");
			BufferedReader input = new BufferedReader(new FileReader(f));
			String line;
			// line = input.readLine();

			while ((line = input.readLine()) != null) {
				// if(line.contains("11g-troubleshooting"))
				// line = line.split("11g-troubleshooting")[1];
				String[] tmpAr = line.split(",");
				for (String str : tmpAr)
					pf.writeToFile(str.replaceAll("\"", ""));

			}
			pf.closeFile();

		} catch (IOException e) {
		}
	}

	public void createTrainTest(String inFile, String cat) {

		/* train sets */

		HashMap<String, PrintToFile> filesMap = new HashMap<String, PrintToFile>();

		filesMap.put("trainHourly", new PrintToFile());
		filesMap.put("trainFixed", new PrintToFile());

		/* test sets */

		filesMap.put("testHourlyNewClients", new PrintToFile());
		filesMap.put("testHourlyMixedClients", new PrintToFile());
		filesMap.put("testFixedNewClients", new PrintToFile());
		filesMap.put("testFixedMixedClients", new PrintToFile());

		filesMap.put("testHolderHourlyNewClients", new PrintToFile());
		filesMap.put("testHolderHourlyMixedClients", new PrintToFile());
		filesMap.put("testHolderFixedNewClients", new PrintToFile());
		filesMap.put("testHolderFixedMixedClients", new PrintToFile());

		for (Entry<String, PrintToFile> e : filesMap.entrySet()) {
			if (e.getKey().contains("train"))
				e.getValue().openFile(
						Classify.basePath + "trainData/" + e.getKey() + cat
								+ Classify.removedFeat + ".txt");

			else if (e.getKey().contains("Holder")) {
				e.getValue().openFile(
						Classify.basePath + "testData/" + e.getKey() + cat
								+ Classify.removedFeat + ".csv");
				e.getValue().writeToFile("opening,contractor");
			} else {
				e.getValue().openFile(
						Classify.basePath + "testData/" + e.getKey() + cat
								+ Classify.removedFeat + ".txt");
			}
		}

		try {
			BufferedReader input = new BufferedReader(new FileReader(
					Classify.basePath + "/rawData/" + inFile));
			String line;
			line = input.readLine();

			HashMap<String, ContractorHolder> contractors = new HashMap<String, ContractorHolder>();
			HashSet<String> trainingClientsHourly = new HashSet<String>();
			HashSet<String> newClientsHourly = new HashSet<String>();
			HashSet<String> trainingClientsFixed = new HashSet<String>();
			HashSet<String> newClientsFixed = new HashSet<String>();

			while ((line = input.readLine()) != null) {

				// System.out.println(line);
				String[] tmpAr = line.split("\",\"");

				for (int i = 0; i < tmpAr.length; i++)
					tmpAr[i] = tmpAr[i].replaceAll("\"", "");

				String contractorStr = tmpAr[2].trim();

				ContractorHolder contractor = contractors.get(contractorStr);
				if (contractor == null) {
					contractor = new ContractorHolder();
					contractors.put(contractorStr, contractor);
				}

				/* Hourly */
				if (tmpAr[0].trim().equals("Hourly")) {
					createFiles("Hourly", trainingClientsHourly,
							newClientsHourly, tmpAr, filesMap, contractor);
				}
				/* Fixed */
				else {
					createFiles("Fixed", trainingClientsFixed, newClientsFixed,
							tmpAr, filesMap, contractor);

				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Entry<String, PrintToFile> e : filesMap.entrySet()) {
			System.out.println("File " + e.getKey() + " created.");
		}
	}

	private void createFiles(String jobType, HashSet<String> trainingClients,
			HashSet<String> newClients, String[] tmpAr,
			HashMap<String, PrintToFile> filesMap, ContractorHolder contractor) {

		String opening = tmpAr[1].trim();
		String client = tmpAr[37].trim();

		if (trainingClients.contains(client)) {
			if (Math.random() < 0.9) { /*
										 * test set 0.9*0.85 = 0.765 (of
										 * clients)
										 */
				createInstance(filesMap.get("train" + jobType), tmpAr,
						contractor);
			} else { /* mixed test = 0.1 * 0.85 = 0.085 of clients */
				createInstance(filesMap.get("test" + jobType + "MixedClients"),
						tmpAr, contractor);
				filesMap.get("testHolder" + jobType + "MixedClients")
						.writeToFile(opening + "," + contractor);
			}
		} else if (newClients.contains(client)) { /*
												 * clean test = 0.15 of clients
												 */
			createInstance(filesMap.get("test" + jobType + "NewClients"),
					tmpAr, contractor);
			filesMap.get("testHolder" + jobType + "NewClients").writeToFile(
					opening + "," + contractor);
			if (Math.random() <= 0.43) { /*
										 * 0.43 * 0.15 = 0.065 of clients in
										 * mixed
										 */
				createInstance(filesMap.get("test" + jobType + "MixedClients"),
						tmpAr, contractor);
				filesMap.get("testHolder" + jobType + "MixedClients")
						.writeToFile(opening + "," + contractor);
			}
		} else {
			if (Math.random() < 0.85) {
				trainingClients.add(client);
				if (Math.random() < 0.9) { /*
											 * test set 0.9*0.85 = 0.765 (of
											 * clients)
											 */
					createInstance(filesMap.get("train" + jobType), tmpAr,
							contractor);
				} else { /*
						 * mixed test = 0.1 * -.85 = 0.085 of clients
						 */
					createInstance(
							filesMap.get("test" + jobType + "MixedClients"),
							tmpAr, contractor);
					filesMap.get("testHolder" + jobType + "MixedClients")
							.writeToFile(opening + "," + contractor);
				}
			} else {
				newClients.add(client);
				createInstance(filesMap.get("test" + jobType + "NewClients"),
						tmpAr, contractor);
				filesMap.get("testHolder" + jobType + "NewClients")
						.writeToFile(opening + "," + contractor);
				if (Math.random() <= 0.43) { /*
											 * 0.43 * 0.15 = 0.065 of lients in
											 * mixed
											 */
					createInstance(
							filesMap.get("test" + jobType + "MixedClients"),
							tmpAr, contractor);
					filesMap.get("testHolder" + jobType + "MixedClients")
							.writeToFile(opening + "," + contractor);

				}
			}
		}
	}

	/**
	 * "job_type"-> 0 "opening" -> 1 "contractor" -> 2 "interview_client" ->3
	 * "interview_contractor" -> 4 "english" -> 5 "hourly_rate" -> 6
	 * "availability_hrs" -> 7 "date_created" -> 8 "city" -> 9 "country" -> 10
	 * "hourly_agency_rate" -> 11 "total_tests" -> 12 "yrs_exp" -> 13
	 * "no_qualifications" -> 14 "total_hours" -> 15 "adjusted_score" -> 16
	 * "adjusted_score_recent" -> 17 "total_last_90_days" -> 18 "start_date" ->
	 * 19 "prc_skills_matching" -> 20 "pref_english_score" -> 21
	 * "pref_feedback_score" -> 22 "prc_interviewed" -> 23
	 * "pref_hourly_rate_max" -> 24 "pref_hourly_rate_min" -> 25 "pref_location"
	 * -> 26 "pref_test" -> 27 "pref_odesk_hours" -> 28 "pref_has_portfolio" ->
	 * 29, "number_prev_openings" -> 30 "contr_timezone" -> 31 "client_timezone"
	 * -> 32 " "cover_unigram_score" -> 33 , "order of
	 * application" -> 34,"client_country" -> 35, "job_unigram_score" ->36,
	 * client ->37
	 * 
	 * @param tmpAr
	 * @param contractor
	 */
	private void createInstance(PrintToFile pf, String[] tmpAr,
			ContractorHolder contractor) {

		String instance = "";
		int label = -1;
		try {
			label = (tmpAr[3].trim().equals("accept") && tmpAr[3].trim()
					.equals("accept")) ? 1 : 0;
		} catch (NullPointerException e) {
			label = 0;
		}

		instance += label;

		int index = 1;
		double adjustedScore = -1;

		double englishScore = -1;
		if (tmpAr[5].trim().length() > 0) {
			englishScore = Double.parseDouble(tmpAr[5].trim());
			if (englishScore > 0 && englishScore < 6)
				instance += " " + index + ":" + ((double) englishScore); // /
																			// 5.0;
		}

		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[6].trim().length() > 0) {
			double hourlyRate = Double.parseDouble(tmpAr[6]);
			if (hourlyRate < 500 && hourlyRate > 0)
				instance += " " + index + ":" + (hourlyRate);// / 500;

		} else if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[11].trim().length() > 0) {
			double hourlyRate = Double.parseDouble(tmpAr[11]);
			if (hourlyRate < 500 && hourlyRate > 0)
				instance += " " + index + ":" + (hourlyRate);

		}

		index++;
		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[7].trim().length() > 0) {
			double availHours = Double.parseDouble(tmpAr[7].trim());
			if (availHours < 100 && availHours > 0)
				instance += " " + index + ":" + availHours; // / 100);
		}
		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[12].trim().length() > 0) {
			double totalTests = Double.parseDouble(tmpAr[12].trim());
			if (totalTests < 40 && totalTests > 0)
				instance += " " + index + ":" + totalTests;
		}
		index++;
		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[13].trim().length() > 0) {
			double yrExps = Double.parseDouble(tmpAr[13].trim());
			if (yrExps < 40 && yrExps > 0)
				instance += " " + index + ":" + yrExps;
		}
		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[14].trim().length() > 0) {
			double noOfQuals = Double.parseDouble(tmpAr[14].trim());
			if (noOfQuals < 100 && noOfQuals > 0)
				instance += " " + index + ":" + noOfQuals;
		}
		index++;
		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[15].trim().length() > 0) {
			double totalHours = Double.parseDouble(tmpAr[15].trim());
			if (totalHours < 10000 && totalHours > 0)
				instance += " " + index + ":" + totalHours;
		}
		index++;
		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[16].trim().length() > 0) {
			adjustedScore = Double.parseDouble(tmpAr[16].trim());
			if (adjustedScore > 0 && adjustedScore <= 5)
				instance += " " + index + ":" + adjustedScore;
		}

		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[17].trim().length() > 0) {
			double adjustedScoreRecent = Double.parseDouble(tmpAr[17].trim());
			if (adjustedScoreRecent > 0 && adjustedScoreRecent <= 5)
				instance += " " + index + ":" + adjustedScoreRecent;
		}
		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[18].trim().length() > 0) {
			double totalLast90Days = Double.parseDouble(tmpAr[18].trim());
			if (totalLast90Days > 0 && totalLast90Days < 10000)
				instance += " " + index + ":" + totalLast90Days;
		}
		index++;


		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[21].trim().length() > 0 && englishScore != -1) {
			double prefEnglishScore = Double.parseDouble(tmpAr[21].trim());
			if (prefEnglishScore > 0)
				instance += " " + index + ":"
						+ ((prefEnglishScore - englishScore));

		}

		index++;

		/*****/
		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[22].trim().length() > 0 && adjustedScore != -1) {
			double prefFeedbackcore = Double.parseDouble(tmpAr[22].trim());
			if (prefFeedbackcore > 0)
				instance += " " + index + ":"
						+ ((prefFeedbackcore - adjustedScore));

		}

		index++;
		/*****/

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[23].trim().length() > 0) {
			double prcInterviewed = Double.parseDouble(tmpAr[23].trim());
			instance += " " + index + ":" + prcInterviewed;
		}

		index++;


		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[31].length() > 0 && tmpAr[32].length() > 0) {
			int t1 = 0;
			int t2 = 0;
			try {
				String tmpStr = tmpAr[31].split("\\s+")[0];
				// System.out.println(tmpStr);
				tmpStr = tmpStr.replace("UTC", "");
				t1 = (tmpStr.contains("+")) ? Integer.parseInt(tmpStr
						.replace("+", "").replaceAll(":[0-9]+", "").trim())
						: -Integer.parseInt(tmpStr.replaceAll("-", "")
								.replaceAll(":[0-9]+", "").trim());
				tmpStr = tmpAr[32].split("\\s+")[0];
				// System.out.println(tmpStr);
				tmpStr = tmpStr.replace("UTC", "");

				t2 = (tmpStr.contains("+")) ? Integer.parseInt(tmpStr
						.replace("+", "").replaceAll(":[0-9]+", "").trim())
						: -Integer.parseInt(tmpStr.replace("-", "")
								.replaceAll(":[0-9]+", "").trim());
				// System.out.println("t1:" + t1 + " t2:" + t2);
				instance += " " + index + ":" + (t1 - t2);
			} catch (NumberFormatException e) {
				if (tmpAr[31].contains("UTC") && tmpAr[32].contains("UTC"))
					instance += " " + index + ":" + (t1 - t2);
			}
		}

		index++;


		Integer curLocation = null;
		String curCountry = tmpAr[10];
		if (curCountry != null && curCountry != "") {
			curLocation = locationToNominal.get(curCountry);
			if (curLocation == null) {
				locationIndex++;
				locationToNominal.put(curCountry, locationIndex);
			}
		}
		if (!Classify.featuresToRemove.contains(index) && curLocation != null)
			instance += " " + index + ":" + curLocation;
		index++;

		Double prevScore = contractor.getCoverLM();
		if (prevScore == -1) {
			if (!Classify.featuresToRemove.contains(index)
					&& tmpAr[33].trim().length() > 0) {
				double loglm = Double.parseDouble(tmpAr[33].trim());
				if (loglm > 0) {

					contractor.setCoverLM(loglm);
				}
			}
		} else if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[33].trim().length() > 0) {

			double loglm = Double.parseDouble(tmpAr[33].trim());
			if (loglm > 0) {
				loglm = Math.abs(loglm - prevScore)
						/ Math.max(loglm, prevScore);
				instance += " " + index + ":" + loglm;
				contractor.setCoverLM(loglm);

			}
		}
		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[33].trim().length() > 0) {

			double loglm = Double.parseDouble(tmpAr[33].trim());
			if (loglm > 0)
				instance += " " + index + ":" + loglm;
		}
		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[34].trim().length() > 0) {
			double order = Double.parseDouble(tmpAr[34].trim());

			instance += " " + index + ":" + order;
		}

		index++;
		String curClientCountry = tmpAr[35];
		Integer curClientLocation = null;
		if (curClientCountry != null && curClientCountry != "") {
			curClientLocation = locationToNominal.get(curClientCountry);
			if (curClientLocation == null) {
				locationIndex++;
				locationToNominal.put(curClientCountry, locationIndex);
			}
		}

		if (!Classify.featuresToRemove.contains(index)
				&& curClientLocation != null)
			instance += " " + index + ":" + curClientLocation;

		index++;

		if (!Classify.featuresToRemove.contains(index) && curLocation != null
				&& curClientLocation != null)
			instance += " " + index + ":"
					+ ((curClientLocation == curLocation) ? 0 : 1);

		index++;
		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[29].trim().length() > 0) {
			double portfolio = ((tmpAr[29].trim().equals("t")) ? 1 : 0);
			instance += " " + index + ":" + portfolio;
		}
		index++;
		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[30].trim().length() > 0) {
			double prevOpenings = Double.parseDouble(tmpAr[30].trim());
			if (prevOpenings > 0 && prevOpenings < 1000)
				instance += " " + index + ":" + prevOpenings;
		}
		index++;
		Double prevJobScore = contractor.getJobsLM();
		if (prevJobScore == -1) {
			if (!Classify.featuresToRemove.contains(index)
					&& tmpAr[36].trim().length() > 0) {
				double loglm = Double.parseDouble(tmpAr[36].trim());
				if (loglm > 0) {

					contractor.setJobsLM(loglm);
				}
			}
		} else if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[36].trim().length() > 0) {

			double loglm = Double.parseDouble(tmpAr[36].trim());
			if (loglm > 0) {
				contractor.setJobsLM(loglm);
				loglm = Math.abs(loglm - prevJobScore)
						/ Math.max(loglm, prevJobScore);
				instance += " " + index + ":" + loglm;

			}
		}
		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[36].trim().length() > 0) {

			double loglm = Double.parseDouble(tmpAr[36].trim());
			if (loglm > 0)
				instance += " " + index + ":" + loglm;
		}

		/*index++;
		 * if (tmpAr[20].trim().length() > 0 && tmpAr[9].trim().length() > 0) {
		 * double starDate = Double.parseDouble(tmpAr[20].trim()); double apDate
		 * = Double.parseDouble(tmpAr[9].trim());
		 * 
		 * instance += " " + index + ":" + (starDate - apDate); } index++;
		 */
		// if (tmpAr[20].trim().length() > 0) {
		// double prcSkills = Double.parseDouble(tmpAr[20].trim());
		// if (prcSkills >= 0 && prcSkills <= 1)
		// instance += " " + index + ":" + prcSkills;
		// }
		// else
		// instance += " " + index + ":0";
		
		if (!labelIsZero && label == 0) {
			pf.writeToFile(instance);
			labelIsZero = true;
		} else if (labelIsZero)
			pf.writeToFile(instance);

	}

	/**
	
	 * 
	 */
	public String[] getFeatures() {
		String headings = "english,hourly_rate (or agency rate),"
				+ "availability_hrs,total_tests,yrs_exp, no_qualifications,"
				+ "total_hours,adjusted_score,adjusted_score_recent,total_last_90_days,"
				+"english_score_diff, pref_feedback_score_diff, prc_interviewed,"
				+ "timezoneDiff, contr_location, cover_unigram_score_diff, cover_unigram_score,"
				+ "order_of_application,client_country,same_client_contr_country, pref_has_portfolio,number_prev_openings,"
				+ "job_unigram_score_diff, job_unigram_score," + "intercept";

		String[] tmpAr = headings.split(",");
		//System.out.println("Size b4:"+tmpAr.length);
		for (int ind : Classify.featuresToRemove) {
			System.out.println("we are removing feature "+tmpAr[ind - 1]+" with index "+ind);
			tmpAr[ind - 1] = null;
			
			
		}
		String [] res = new String[tmpAr.length - Classify.featuresToRemove.size()];
		int j=0;
		for(int i=0; i< tmpAr.length; i++){
			if(tmpAr[i] != null){
				res[j] = tmpAr[i];
				j++;
			}
		}
		//System.out.println(" after:"+res.length);
		return res;

	}

	public ArrayList<ProbHolder> loadHolders(String f) {
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
}
