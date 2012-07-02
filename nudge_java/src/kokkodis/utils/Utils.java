package kokkodis.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import kokkodis.logitModel.Classify;
import kokkodis.probInterview.dataClasses.Contractor;

public class Utils {

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

	public void createTrainingFile(String inFile) {
		HashMap<String, Integer> locationToNominal = new HashMap<String, Integer>();
		boolean labelIsZero = false; // boolean to make sure that the
										// probEstimates[0] are for the negative
										// class (i.e. 0).
		int locationIndex = 0;
		Counter<Integer> orderMap = new Counter<Integer>();
		PrintToFile trainFile = new PrintToFile();
		trainFile
				.openFile("/Users/mkokkodi/Desktop/bigFiles/nudge/trainData/train"+Classify.baseFile+".txt");
		PrintToFile testFile = new PrintToFile();
		testFile.openFile("/Users/mkokkodi/Desktop/bigFiles/nudge/testData/test"+Classify.baseFile+".txt");
		try {
			BufferedReader input = new BufferedReader(new FileReader(
					"/Users/mkokkodi/Desktop/bigFiles/nudge/rawData/" + inFile));
			String line;
			line = input.readLine();

			/**
			 * "job_type"-> 0 "reason" -> 1 "opening" -> 2 "contractor" -> 3
			 * "interview_client" ->4 "interview_contractor" -> 5 "english" -> 6
			 * "hourly_rate" -> 7 "availability_hrs" -> 8 "date_created" -> 9
			 * "city" -> 10 "country" -> 11 "hourly_agency_rate" -> 12
			 * "total_tests" -> 13 "yrs_exp" -> 14 "no_qualifications" -> 15
			 * "total_hours" -> 16 "adjusted_score" -> 17
			 * "adjusted_score_recent" -> 18 "total_last_90_days" -> 19
			 * "start_date" -> 20 "prc_skills_matching" -> 21 "job_category" ->
			 * 22 "pref_english_score" -> 23 "pref_feedback_score" -> 24
			 * "prc_interviewed" -> 25 "pref_hourly_rate_max" -> 26
			 * "pref_hourly_rate_min" -> 27 "pref_location" -> 28 "pref_test" ->
			 * 29 "pref_odesk_hours" -> 30
			 */
			while ((line = input.readLine()) != null) {
				String[] tmpAr = line.split("\",");

				for (int i = 0; i < tmpAr.length; i++)
					tmpAr[i] = tmpAr[i].replaceAll("\"", "");
				String instance = "";
				int label = -1;
				try {
					label = (tmpAr[4].trim().equals("accept") && tmpAr[4]
							.trim().equals("accept")) ? 1 : 0;
				} catch (NullPointerException e) {
					label = 0;
				}

				instance += label;

				int jobType = (tmpAr[0].trim().equals("Hourly")) ? 0 : 1;
				instance += " 1:" + jobType;

				double englishScore = -1;
				if (tmpAr[6].trim().length() > 0) {
					englishScore = Double.parseDouble(tmpAr[6].trim());
					if (englishScore >= 0 && englishScore < 6)
						instance += " 2:" + ((double) englishScore); // / 5.0;
				}

				if (tmpAr[7].trim().length() > 0) {
					double hourlyRate = Double.parseDouble(tmpAr[7]);
					if (hourlyRate < 200 && hourlyRate > 0)
						instance += " 3:" + (hourlyRate);// / 500;

				} else if (tmpAr[12].trim().length() > 0) {
					double hourlyRate = Double.parseDouble(tmpAr[12]);
					if (hourlyRate < 200 && hourlyRate > 0)
						instance += " 3:" + (hourlyRate);

				}

				if (tmpAr[8].trim().length() > 0) {
					double availHours = Double.parseDouble(tmpAr[8].trim());
					if (availHours < 100 && availHours > 0)
						instance += " 4:" + availHours; // / 100);
				}

				if (tmpAr[14].trim().length() > 0) {
					double yrExps = Double.parseDouble(tmpAr[14].trim());
					if (yrExps < 40 && yrExps > 0)
						instance += " 5:" + yrExps;
				}

				if (tmpAr[16].trim().length() > 0) {
					double totalHours = Double.parseDouble(tmpAr[16].trim());
					if (totalHours < 10000 && totalHours > 0)
						instance += " 6:" + totalHours;
				}

				if (tmpAr[17].trim().length() > 0) {
					double adjustedScore = Double.parseDouble(tmpAr[17].trim());
					if (adjustedScore >= 0 && adjustedScore < 6)
						instance += " 7:" + adjustedScore;
				}

				if (tmpAr[18].trim().length() > 0) {
					double adjustedScoreRecent = Double.parseDouble(tmpAr[18]
							.trim());
					if (adjustedScoreRecent >= 0 && adjustedScoreRecent < 6)
						instance += " 8:" + adjustedScoreRecent;
				}

				if (tmpAr[19].trim().length() > 0) {
					double totalLast90Days = Double.parseDouble(tmpAr[19]
							.trim());
					if (totalLast90Days >= 0 && totalLast90Days < 10000)
						instance += " 9:" + totalLast90Days;
				}
				if (tmpAr[21].trim().length() > 0) {
					double prcSkills = Double.parseDouble(tmpAr[21].trim());
					if (prcSkills >= 0 && prcSkills <= 1)
						instance += " 10:" + prcSkills;
				}

				if (tmpAr[22].trim().length() > 0) {
					double jobCategory = Double.parseDouble(tmpAr[22].trim());
					if (jobCategory >= 0 && jobCategory <= 100)
						instance += " 11:" + jobCategory;
				}

				if (tmpAr[23].trim().length() > 0 && englishScore != -1) {
					instance += " 12:"
							+ ((Double.parseDouble(tmpAr[23].trim()) - englishScore))
							/ 5;
				}

				if (tmpAr[25].trim().length() > 0) {
					double prcInterviewed = Double
							.parseDouble(tmpAr[25].trim());
					instance += " 13:" + prcInterviewed;
				}

				int opening = Integer.parseInt(tmpAr[2]);
				orderMap.incrementCount(opening, 1);

				instance += " 14:" + (int) orderMap.getCount(opening);

				Integer curLocation = null;
				String curCountry = tmpAr[11];
				if (curCountry != null && curCountry != "") {
					curLocation = locationToNominal.get(curCountry);
					if (curLocation == null) {
						locationIndex++;
						locationToNominal.put(curCountry, locationIndex);
					}
				}

				if (curLocation != null)
					instance += " 15:" + curLocation;

				if (!labelIsZero && label == 0) {
					trainFile.writeToFile(instance);
					labelIsZero = true;
				} else if (labelIsZero && Math.random() < 0.9) {
					trainFile.writeToFile(instance);

				} else
					testFile.writeToFile(instance);

			}
			System.out.println("File created.");
			testFile.closeFile();
		} catch (IOException e) {
		}
	}

}
