package kokkodis.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;

import kokkodis.logitModel.Classify;
import kokkodis.logitModel.ProbHolder;
import kokkodis.probInterview.dataClasses.Contractor;

public class Utils {

	private boolean labelIsZero = false; // boolean to make sure that the

	private HashMap<String, Integer> locationToNominal = new HashMap<String, Integer>();
	private HashMap<String, Double> contractorCoversLMscores = new HashMap<String, Double>();
	private HashMap<String, Double> contractorJobsLMscores = new HashMap<String, Double>();

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
		PrintToFile trainHourly = new PrintToFile();
		trainHourly.openFile(Classify.basePath + "trainData/trainHourly" + cat
				+ ".txt");
		PrintToFile trainFixed = new PrintToFile();
		trainFixed.openFile(Classify.basePath + "trainData/trainFixed" + cat
				+ ".txt");

		PrintToFile testHourly = new PrintToFile();
		testHourly.openFile(Classify.basePath + "testData/testHourly" + cat
				+ ".txt");
		PrintToFile testFixed = new PrintToFile();
		testFixed.openFile(Classify.basePath + "testData/testFixed" + cat
				+ ".txt");

		PrintToFile testHourlyInstance = new PrintToFile();
		testHourlyInstance.openFile(Classify.basePath
				+ "testData/testHourlyHolder" + cat + ".csv");
		testHourlyInstance.writeToFile("opening,contractor");
		PrintToFile testFixedInstance = new PrintToFile();
		testFixedInstance.openFile(Classify.basePath
				+ "testData/testFixedHolder" + cat + ".csv");
		testFixedInstance.writeToFile("opening,contractor");

		try {
			BufferedReader input = new BufferedReader(new FileReader(
					Classify.basePath + "/rawData/" + inFile));
			String line;
			line = input.readLine();

			HashSet<String> testingHourlyContractors = new HashSet<String>();
			HashSet<String> trainingHourlyContractors = new HashSet<String>();

			HashSet<String> testingFixedContractors = new HashSet<String>();
			HashSet<String> trainingFixedContractors = new HashSet<String>();

			while ((line = input.readLine()) != null) {

				// System.out.println(line);
				String[] tmpAr = line.split("\",\"");

				for (int i = 0; i < tmpAr.length; i++)
					tmpAr[i] = tmpAr[i].replaceAll("\"", "");

				String contractor = tmpAr[2].trim();
				String opening = tmpAr[1].trim();

				/* Hourly */
				if (tmpAr[0].trim().equals("Hourly")) {
					if (trainingHourlyContractors.contains(contractor))
						createInstance(trainHourly, tmpAr, contractor);
					else if (testingHourlyContractors.contains(contractor)) {
						createInstance(testHourly, tmpAr, contractor);
						testHourlyInstance.writeToFile(opening + ","
								+ contractor);
					} else {
						if (Math.random() < 0.85) {
							trainingHourlyContractors.add(contractor);
							createInstance(trainHourly, tmpAr, contractor);
						} else {
							testingHourlyContractors.add(contractor);
							createInstance(testHourly, tmpAr, contractor);
							testHourlyInstance.writeToFile(opening + ","
									+ contractor);

						}
					}
				}
				/* Fixed */
				else {
					if (trainingFixedContractors.contains(contractor))
						createInstance(trainFixed, tmpAr, contractor);
					else if (testingFixedContractors.contains(contractor)) {
						createInstance(testFixed, tmpAr, contractor);
						testFixedInstance.writeToFile(opening + ","
								+ contractor);
					} else {
						if (Math.random() < 0.85) {
							trainingFixedContractors.add(contractor);
							createInstance(trainFixed, tmpAr, contractor);
						} else {
							testingFixedContractors.add(contractor);
							createInstance(testFixed, tmpAr, contractor);
							testFixedInstance.writeToFile(opening + ","
									+ contractor);

						}
					}
				}

			}
			System.out.println("File created.");
			trainHourly.closeFile();
			testHourly.closeFile();
			testHourlyInstance.closeFile();
			trainFixed.closeFile();
			testFixed.closeFile();
			testFixedInstance.closeFile();
		} catch (IOException e) {
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
	 * application" -> 34,"client_country" -> 35, "job_unigram_score" ->36
	 * 
	 * @param tmpAr
	 * @param contractor
	 */
	private void createInstance(PrintToFile pf, String[] tmpAr,
			String contractor) {

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

		if (tmpAr[6].trim().length() > 0) {
			double hourlyRate = Double.parseDouble(tmpAr[6]);
			if (hourlyRate < 500 && hourlyRate > 0)
				instance += " " + index + ":" + (hourlyRate);// / 500;

		} else if (tmpAr[11].trim().length() > 0) {
			double hourlyRate = Double.parseDouble(tmpAr[11]);
			if (hourlyRate < 500 && hourlyRate > 0)
				instance += " " + index + ":" + (hourlyRate);

		}

		index++;
		if (tmpAr[7].trim().length() > 0) {
			double availHours = Double.parseDouble(tmpAr[7].trim());
			if (availHours < 100 && availHours > 0)
				instance += " " + index + ":" + availHours; // / 100);
		}
		index++;

		if (tmpAr[12].trim().length() > 0) {
			double totalTests = Double.parseDouble(tmpAr[12].trim());
			if (totalTests < 40 && totalTests > 0)
				instance += " " + index + ":" + totalTests;
		}
		index++;
		if (tmpAr[13].trim().length() > 0) {
			double yrExps = Double.parseDouble(tmpAr[13].trim());
			if (yrExps < 40 && yrExps > 0)
				instance += " " + index + ":" + yrExps;
		}
		index++;

		if (tmpAr[14].trim().length() > 0) {
			double noOfQuals = Double.parseDouble(tmpAr[14].trim());
			if (noOfQuals < 100 && noOfQuals > 0)
				instance += " " + index + ":" + noOfQuals;
		}
		index++;
		if (tmpAr[15].trim().length() > 0) {
			double totalHours = Double.parseDouble(tmpAr[15].trim());
			if (totalHours < 10000 && totalHours > 0)
				instance += " " + index + ":" + totalHours;
		}
		index++;
		if (tmpAr[16].trim().length() > 0) {
			adjustedScore = Double.parseDouble(tmpAr[16].trim());
			if (adjustedScore > 0 && adjustedScore <= 5)
				instance += " " + index + ":" + adjustedScore;
		}

		index++;

		if (tmpAr[17].trim().length() > 0) {
			double adjustedScoreRecent = Double.parseDouble(tmpAr[17].trim());
			if (adjustedScoreRecent > 0 && adjustedScoreRecent <= 5)
				instance += " " + index + ":" + adjustedScoreRecent;
		}
		index++;

		if (tmpAr[18].trim().length() > 0) {
			double totalLast90Days = Double.parseDouble(tmpAr[18].trim());
			if (totalLast90Days > 0 && totalLast90Days < 10000)
				instance += " " + index + ":" + totalLast90Days;
		}
		index++;
		/*
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
		index++;

		if (tmpAr[21].trim().length() > 0 && englishScore != -1) {
			double prefEnglishScore = Double.parseDouble(tmpAr[21].trim());
			if (prefEnglishScore > 0)
				instance += " " + index + ":"
						+ ((prefEnglishScore - englishScore));

		}

		index++;

		/*****/
		if (tmpAr[22].trim().length() > 0 && adjustedScore != -1) {
			double prefFeedbackcore = Double.parseDouble(tmpAr[22].trim());
			if (prefFeedbackcore > 0)
				instance += " " + index + ":"
						+ ((prefFeedbackcore - adjustedScore));

		}

		index++;
		/*****/

		if (tmpAr[23].trim().length() > 0) {
			double prcInterviewed = Double.parseDouble(tmpAr[23].trim());
			instance += " " + index + ":" + prcInterviewed;
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

		if (tmpAr[31].length() > 0 && tmpAr[32].length() > 0) {
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

		if (curLocation != null)
			instance += " " + index + ":" + curLocation;
		index++;

		Double prevScore = contractorCoversLMscores.get(contractor);
		if (prevScore == null) {
			if (tmpAr[33].trim().length() > 0) {
				double loglm = Double.parseDouble(tmpAr[33].trim());
				if (loglm > 0) {

					contractorCoversLMscores.put(contractor, loglm);
				}
			}
		} else if (tmpAr[33].trim().length() > 0) {

			double loglm = Double.parseDouble(tmpAr[33].trim());
			if (loglm > 0) {
				contractorCoversLMscores.put(contractor, loglm);
				loglm = Math.abs(loglm - prevScore)
						/ Math.max(loglm, prevScore);
				instance += " " + index + ":" + loglm;

			}
		}
		index++;

		if (tmpAr[33].trim().length() > 0) {

			double loglm = Double.parseDouble(tmpAr[33].trim());
			if (loglm > 0)
				instance += " " + index + ":" + loglm;
		}
		index++;

		if (tmpAr[34].trim().length() > 0) {
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

		if (curClientLocation != null)
			instance += " " + index + ":" + curClientLocation;

		index++;

		if (curLocation != null && curClientLocation != null)
			instance += " " + index + ":"
					+ ((curClientLocation == curLocation) ? 0 : 1);

		index++;
		if (tmpAr[29].trim().length() > 0) {
			double portfolio = ((tmpAr[29].trim().equals("t")) ? 1 : 0);
			instance += " " + index + ":" + portfolio;
		}
		index++;
		if (tmpAr[30].trim().length() > 0) {
			double prevOpenings = Double.parseDouble(tmpAr[30].trim());
			if (prevOpenings > 0 && prevOpenings < 1000)
				instance += " " + index + ":" + prevOpenings;
		}
		index++;
		Double prevJobScore = contractorJobsLMscores.get(contractor);
		if (prevJobScore == null) {
			if (tmpAr[36].trim().length() > 0) {
				double loglm = Double.parseDouble(tmpAr[36].trim());
				if (loglm > 0) {

					contractorJobsLMscores.put(contractor, loglm);
				}
			}
		} else if (tmpAr[36].trim().length() > 0) {

			double loglm = Double.parseDouble(tmpAr[36].trim());
			if (loglm > 0) {
				contractorJobsLMscores.put(contractor, loglm);
				loglm = Math.abs(loglm - prevJobScore)
						/ Math.max(loglm, prevJobScore);
				instance += " " + index + ":" + loglm;

			}
		}
		index++;

		if (tmpAr[36].trim().length() > 0) {

			double loglm = Double.parseDouble(tmpAr[36].trim());
			if (loglm > 0)
				instance += " " + index + ":" + loglm;
		}

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
		String headings = "job_type,english,hourly_rate,hourly_agency_rate,"
				+ "availability_hrs,total_tests,yrs_exp, no_qualifications,"
				+ "total_hours,adjusted_score,adjusted_score_recent,total_last_90_days,"
				+ "prc_skills_matching,english_score_diff, pref_feedback_score_diff, prc_interviewed,"
				+ "timezoneDiff, contr_location, cover_unigram_score_diff, cover_unigram_score,"
				+ "order_of_application,client_country,pref_has_portfolio,number_prev_openings,"
				+ "job_unigram_score_diff, job_unigram_score," + "intercept";

		String[] tmpAr = headings.split(",");
		return tmpAr;
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
