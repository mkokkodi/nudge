/****************************************************
 * @author Marios Kokkodis                          *
 * comments/questions : mkokkodi@odesk.com     		*
 *													*					  
 *  Class Description - class for creating
 *  train and test sets.                      		*	
 *													*  
 * 	*************************************************									
 */

package kokkodis.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import kokkodis.holders.ClientHolder;
import kokkodis.holders.ContractorHolder;
import kokkodis.holders.MinMaxHolder;
import kokkodis.logistic.Classify;

public class CreateUtils extends Utils {

	private boolean labelIsZero = false; // boolean to make sure that 0 is
											// "not interviewed".

	private HashMap<String, Integer> locationToNominal = new HashMap<String, Integer>();
	// Mapping locations to categorical values.

	private int locationIndex = 0;

	

	HashMap<String, MinMaxHolder> normalizingMap;

	private static HashMap<String, ClientHolder> clientHistory;

	/**
	 * Creates the train and test sets in the liblinear format.
	 * 
	 * @param cat
	 */
	public void generateTrainTest(String cat) {
		clientHistory = new HashMap<String, ClientHolder>();

		countriesProductMap = readContriesProducts();
		/* train sets */

		HashMap<String, PrintToFile> filesMap = new HashMap<String, PrintToFile>();

		filesMap.put("trainHourly", new PrintToFile());
		filesMap.put("trainFixed", new PrintToFile());

		/* test sets */

		filesMap.put("testHourly", new PrintToFile());
		filesMap.put("testFixed", new PrintToFile());

		filesMap.put("testHolderHourly", new PrintToFile());
		filesMap.put("testHolderFixed", new PrintToFile());

		for (Entry<String, PrintToFile> e : filesMap.entrySet()) {
			if (e.getKey().contains("train"))
				e.getValue().openFile(
						Classify.dataPath + "trainData/" + e.getKey() + cat
								+ Classify.removedFeat + ".txt");

			else if (e.getKey().contains("Holder")) {
				e.getValue().openFile(
						Classify.dataPath + "testData/" + e.getKey() + cat
								+ Classify.removedFeat + ".csv");
				e.getValue().writeToFile("opening,contractor");
			} else {
				e.getValue().openFile(
						Classify.dataPath + "testData/" + e.getKey() + cat
								+ Classify.removedFeat + ".txt");
			}
		}

		/* Train */
		createFiles(false, filesMap, cat);
		/* Test */
		createFiles(true, filesMap, cat);

		for (Entry<String, PrintToFile> e : filesMap.entrySet()) {
			System.out.println("File " + e.getKey() + " created.");
		}
	}

	/**
	 * 
	 * @param test
	 *            : true if dealing with test set.
	 * @param filesMap
	 * @param cat
	 */
	private void createFiles(boolean test,
			HashMap<String, PrintToFile> filesMap, String cat) {
		try {
			BufferedReader input = new BufferedReader(new FileReader(
					Classify.dataPath + "/rawData/train" + cat + ".csv"));
			String line;
			line = input.readLine();

			HashMap<String, ContractorHolder> contractors = new HashMap<String, ContractorHolder>();

			while ((line = input.readLine()) != null) {

				// System.out.println(line);
				String[] tmpAr = line.split("\",\"");

				for (int i = 0; i < tmpAr.length; i++) {
					tmpAr[i] = tmpAr[i].replaceAll("\"", "");
					// System.out.println(i +" "+tmpAr[i]);
				}

				String contractor = tmpAr[2].trim();

				ContractorHolder contractorHolder = contractors.get(contractor);
				if (contractorHolder == null) {
					contractorHolder = new ContractorHolder();
					contractors.put(contractor, contractorHolder);
				}

				String jobType = tmpAr[0].trim();
				String opening = tmpAr[1].trim();
				String client = tmpAr[30].trim();

				ClientHolder curClientHolder = clientHistory.get(client);
				if (curClientHolder == null) {
					curClientHolder = new ClientHolder();
					clientHistory.put(client, curClientHolder);

				}
				createInstance(
						filesMap.get((test ? "test" : "train") + jobType),
						tmpAr, contractorHolder, curClientHolder, contractor);
				if (test)
					filesMap.get("testHolder" + jobType).writeToFile(
							opening + "," + contractor);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * "job_type"-> 0 "opening" -> 1 "contractor" -> 2 "interview_client" ->3
	 * "interview_contractor" -> 4 "english" -> 5 "hourly_rate" -> 6
	 * "availability_hrs" -> 7 "country" -> 8 "hourly_agency_rate" -> 9
	 * "total_tests" -> 10 "yrs_exp" -> 11 "no_qualifications" -> 12
	 * "total_hours" -> 13 "adjusted_score" -> 14 "adjusted_score_recent" -> 15
	 * "total_last_90_days" -> 16 "pref_english_score" -> 17
	 * "pref_english_score" -> 18 "prc_interviewed" -> 19 "pref_test" -> 20
	 * "pref_has_portfolio" -> 21, "number_prev_openings" -> 22 "contr_timezone"
	 * -> 23 "client_timezone" -> 24 " "cover_unigram_score" -> 25 , "order of
	 * application" -> 26,"client_country" -> 27, "job_unigram_score" ->28,
	 * number_prev_applications ->29, client -> 30
	 * 
	 * @param tmpAr
	 * @param contractorHolder
	 * @param curClientHolder
	 * @param contractor2
	 */
	private void createInstance(PrintToFile pf, String[] tmpAr,
			ContractorHolder contractorHolder, ClientHolder curClientHolder,
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
		double totalTests = -1;

		if (tmpAr[5].trim().length() > 0) {
			englishScore = Double.parseDouble(tmpAr[5].trim());
			if (englishScore > 0 && englishScore < 6) {
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize(((double) englishScore),
									normalizingMap.get("english"));
				else
					instance += " " + index + ":" + ((double) englishScore);

			}// /
				// 5.0;
		}

		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[6].trim().length() > 0) {
			double hourlyRate = Double.parseDouble(tmpAr[6]);
			if (hourlyRate < 500 && hourlyRate > 0) {
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize(((double) hourlyRate),
									normalizingMap.get("hourly_rate"));
				else
					instance += " " + index + ":" + ((double) hourlyRate);
			}

		} else if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[9].trim().length() > 0) {
			double hourlyRate = Double.parseDouble(tmpAr[9]);
			if (hourlyRate < 500 && hourlyRate > 0) {
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize(((double) hourlyRate),
									normalizingMap.get("hourly_rate"));
				else
					instance += " " + index + ":" + ((double) hourlyRate);
			}
		}

		index++;
		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[7].trim().length() > 0) {
			double availHours = Double.parseDouble(tmpAr[7].trim());
			if (availHours < 100 && availHours > 0)
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize(((double) availHours),
									normalizingMap.get("availability"));
				else
					instance += " " + index + ":" + ((double) availHours);
		}
		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[10].trim().length() > 0) {
			totalTests = Double.parseDouble(tmpAr[10].trim());
			if (totalTests < 40 && totalTests > 0)
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize(((double) totalTests),
									normalizingMap.get("total_tests"));
				else
					instance += " " + index + ":" + ((double) totalTests);
		}
		index++;
		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[11].trim().length() > 0) {
			double yrExps = Double.parseDouble(tmpAr[11].trim());
			if (yrExps < 30 && yrExps > 0)
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize(((double) yrExps),
									normalizingMap.get("yrs_exp"));
				else
					instance += " " + index + ":" + ((double) yrExps);
		}
		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[12].trim().length() > 0) {
			double noOfQuals = Double.parseDouble(tmpAr[12].trim());
			if (noOfQuals < 50 && noOfQuals > 0)
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize(((double) noOfQuals),
									normalizingMap.get("no_qualifications"));
				else
					instance += " " + index + ":" + ((double) noOfQuals);
		}
		index++;
		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[13].trim().length() > 0) {
			double totalHours = Double.parseDouble(tmpAr[13].trim());
			if (totalHours < 40000 && totalHours > 0)
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize(((double) totalHours),
									normalizingMap.get("total_hours"));
				else
					instance += " " + index + ":" + ((double) totalHours);
		}
		index++;
		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[14].trim().length() > 0) {
			adjustedScore = Double.parseDouble(tmpAr[14].trim());
			if (adjustedScore > 0 && adjustedScore <= 5)
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize(((double) adjustedScore),
									normalizingMap.get("adjusted_score"));
				else
					instance += " " + index + ":" + ((double) adjustedScore);
		}

		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[15].trim().length() > 0) {
			double adjustedScoreRecent = Double.parseDouble(tmpAr[15].trim());
			if (adjustedScoreRecent > 0 && adjustedScoreRecent <= 5)
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize(((double) adjustedScoreRecent),
									normalizingMap.get("adjusted_score_recent"));
				else
					instance += " " + index + ":"
							+ ((double) adjustedScoreRecent);
		}
		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[16].trim().length() > 0) {
			double totalLast90Days = Double.parseDouble(tmpAr[16].trim());
			if (totalLast90Days > 0 && totalLast90Days < 10000)
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize(((double) totalLast90Days),
									normalizingMap.get("total_last_90_days"));
				else
					instance += " " + index + ":" + ((double) totalLast90Days);
		}
		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[17].trim().length() > 0 && englishScore != -1) {
			double prefEnglishScore = Double.parseDouble(tmpAr[17].trim());
			if (prefEnglishScore > 0)
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize(
									((double) (prefEnglishScore - englishScore)),
									normalizingMap.get("english"));
				else
					instance += " " + index + ":"
							+ ((double) (prefEnglishScore - englishScore));

		}

		index++;

		/*****/
		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[18].trim().length() > 0 && adjustedScore != -1) {
			double prefFeedbackcore = Double.parseDouble(tmpAr[18].trim());
			if (prefFeedbackcore > 0)
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize(
									((double) prefFeedbackcore - adjustedScore),
									normalizingMap.get("adjusted_score"));
				else
					instance += " " + index + ":"
							+ ((double) prefFeedbackcore - adjustedScore);

		}

		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[19].trim().length() > 0) {
			double prcInterviewed = Double.parseDouble(tmpAr[19].trim());
			instance += " " + index + ":" + prcInterviewed;
		}

		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[23].length() > 0 && tmpAr[24].length() > 0) {
			int t1 = 0;
			int t2 = 0;
			try {
				String tmpStr = tmpAr[23].split("\\s+")[0];
				// System.out.println(tmpStr);
				tmpStr = tmpStr.replace("UTC", "");
				t1 = (tmpStr.contains("+")) ? Integer.parseInt(tmpStr
						.replace("+", "").replaceAll(":[0-9]+", "").trim())
						: -Integer.parseInt(tmpStr.replaceAll("-", "")
								.replaceAll(":[0-9]+", "").trim());
				tmpStr = tmpAr[24].split("\\s+")[0];
				// System.out.println(tmpStr);
				tmpStr = tmpStr.replace("UTC", "");

				t2 = (tmpStr.contains("+")) ? Integer.parseInt(tmpStr
						.replace("+", "").replaceAll(":[0-9]+", "").trim())
						: -Integer.parseInt(tmpStr.replace("-", "")
								.replaceAll(":[0-9]+", "").trim());
				// System.out.println("t1:" + t1 + " t2:" + t2);
				instance += " " + index + ":" + (t1 - t2);
			} catch (NumberFormatException e) {
				if (tmpAr[23].contains("UTC") && tmpAr[24].contains("UTC"))
					if (Classify.normalize)
						instance += " "
								+ index
								+ ":"
								+ normalize((t1 - t2),
										normalizingMap.get("utc"));
					else
						instance += " " + index + ":" + (t1 - t2);
			}
		}

		index++;

		Integer curLocation = null;
		String curCountry = tmpAr[8];
		if (curCountry != null && curCountry != "") {
			curLocation = locationToNominal.get(curCountry);
			if (curLocation == null) {
				locationIndex++;
				locationToNominal.put(curCountry, locationIndex);
			}
		}
		if (!Classify.featuresToRemove.contains(index) && curLocation != null)
			if (Classify.normalize)
				instance += " "
						+ index
						+ ":"
						+ normalize((curLocation),
								normalizingMap.get("country"));
			else
				instance += " " + index + ":" + (curLocation);
		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[25].trim().length() > 0) {

			double loglm = Double.parseDouble(tmpAr[25].trim());
			if (loglm > 0)
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize((loglm),
									normalizingMap.get("cover_unigram_score"));
				else
					instance += " " + index + ":" + (loglm);
		}
		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[26].trim().length() > 0) {
			double order = Double.parseDouble(tmpAr[26].trim());
			if (order > 0 && order < 1000)
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize((order),
									normalizingMap.get("order_of_application"));
				else
					instance += " " + index + ":" + (order);
		}

		index++;
		String curClientCountry = tmpAr[27];
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
			if (Classify.normalize)
				instance += " "
						+ index
						+ ":"
						+ normalize((curClientLocation),
								normalizingMap.get("country"));
			else
				instance += " " + index + ":" + (curClientLocation);

		index++;

		if (!Classify.featuresToRemove.contains(index) && curLocation != null
				&& curClientLocation != null)
			instance += " " + index + ":"
					+ ((curClientLocation == curLocation) ? 0 : 1);

		index++;
		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[21].trim().length() > 0) {
			double portfolio = ((tmpAr[21].trim().equals("t")) ? 1 : 0);
			instance += " " + index + ":" + portfolio;
		}
		index++;
		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[22].trim().length() > 0) {
			double prevOpenings = Double.parseDouble(tmpAr[22].trim());
			if (prevOpenings > 0 && prevOpenings < 200)
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize((prevOpenings),
									normalizingMap.get("prev_openings"));
				else
					instance += " " + index + ":" + (prevOpenings);

		}
		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[28].trim().length() > 0) {

			double loglm = Double.parseDouble(tmpAr[28].trim());
			if (loglm > 0)
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize((loglm),
									normalizingMap.get("job_unigram_score"));
				else
					instance += " " + index + ":" + (loglm);
		}
		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[20].trim().length() > 0 && totalTests != -1) {
			double prefTotaleTests = Double.parseDouble(tmpAr[20].trim());
			if (prefTotaleTests > 0 && prefTotaleTests <= 40)
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize((prefTotaleTests - totalTests),
									normalizingMap.get("pref_test"));
				else
					instance += " " + index + ":"
							+ (prefTotaleTests - totalTests);

		}

		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& tmpAr[29].trim().length() > 0 && totalTests != -1) {
			double noPrevApps = Double.parseDouble(tmpAr[29].trim());
			if (noPrevApps > 0)
				if (Classify.normalize)
					instance += " "
							+ index
							+ ":"
							+ normalize((noPrevApps),
									normalizingMap
											.get("number_prev_applications"));
				else
					instance += " " + index + ":" + (noPrevApps);

		}
		index++;

		if (!Classify.featuresToRemove.contains(index)
				&& curCountry.length() > 0) {
			if (curClientHolder.getWorkedWithCountries().contains(curCountry))
				instance += " " + (index) + ":1";
			else {
				curClientHolder.getWorkedWithCountries().add(curCountry);
			}

		}
		index++;
		if (!Classify.featuresToRemove.contains(index)) {
			if (curClientHolder.getWorkedWithContractors().containsKey(
					contractor))
				instance += " "
						+ (index)
						+ ":"
						+ curClientHolder.getWorkedWithContractors().getCount(
								contractor);
			curClientHolder.getWorkedWithContractors().incrementCount(
					contractor, 1);

		}
		index++;
		if (curCountry.length() > 0 && curClientCountry.length() > 0) {
			String key = curClientCountry + ">" + curCountry;
			int tmpInd = countriesProductMap.get(key);
			instance += " " + (index + tmpInd + 1) + ":1";
		}

		if (!labelIsZero && label == 0) {
			pf.writeToFile(instance);
			labelIsZero = true;
		} else if (labelIsZero)
			pf.writeToFile(instance);

		/**
		 * cover scores diff. Double prevScore = contractor.getCoverLM(); if
		 * (prevScore == -1) { if (!Classify.featuresToRemove.contains(index) &&
		 * tmpAr[25].trim().length() > 0) { double loglm =
		 * Double.parseDouble(tmpAr[25].trim()); if (loglm > 0) {
		 * 
		 * contractor.setCoverLM(loglm); } } } else if
		 * (!Classify.featuresToRemove.contains(index) &&
		 * tmpAr[25].trim().length() > 0) {
		 * 
		 * double loglm = Double.parseDouble(tmpAr[25].trim()); if (loglm > 0) {
		 * loglm = Math.abs(loglm - prevScore) / Math.max(loglm, prevScore); if
		 * (Classify.normalize) instance += " " + index + ":" + normalize((loglm),
		 * normalizingMap.get("cover_unigram_score")); else instance += " " +
		 * index + ":" + (loglm); contractor.setCoverLM(loglm);
		 * 
		 * } } index++;
		 */
		/*
		 * index++; if (tmpAr[20].trim().length() > 0 &&
		 * tmpAr[9].trim().length() > 0) { double starDate =
		 * Double.parseDouble(tmpAr[20].trim()); double apDate =
		 * Double.parseDouble(tmpAr[9].trim());
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
		/**
		 * jobs difference. Double prevJobScore = contractor.getJobsLM(); if
		 * (prevJobScore == -1) { if (!Classify.featuresToRemove.contains(index)
		 * && tmpAr[28].trim().length() > 0) { double loglm =
		 * Double.parseDouble(tmpAr[28].trim()); if (loglm > 0) {
		 * 
		 * contractor.setJobsLM(loglm); } } } else if
		 * (!Classify.featuresToRemove.contains(index) &&
		 * tmpAr[28].trim().length() > 0) {
		 * 
		 * double loglm = Double.parseDouble(tmpAr[28].trim()); if (loglm > 0) {
		 * contractor.setJobsLM(loglm); loglm = Math.abs(loglm - prevJobScore) /
		 * Math.max(loglm, prevJobScore); instance += " " + index + ":" + loglm;
		 * 
		 * } } index++;
		 */

	}

	private double normalize(double d, MinMaxHolder minMaxHolder) {
		return (d - minMaxHolder.getMin())
				/ (minMaxHolder.getMax() - minMaxHolder.getMin());
	}

	public void normalizeData() {

		normalizingMap = new HashMap<String, MinMaxHolder>();

		String values = "english,0,5 " + ">> availability,0,100 >>"
				+ "hourly_rate,0,500 >>" + "total_tests, 0, 100 >>"
				+ "yrs_exp,0,30 >>" + "no_qualifications, 0, 50 >>"
				+ "total_hours,0, 40000 >> " + "adjusted_score, 0 , 5 >> "
				+ "adjusted_score_recent, 0 , 5 >> "
				+ "total_last_90_days, 0, 4500 >> "
				+ "pref_english_score, 0, 5 >> " + "pref_test, 0, 40 >> "
				+ "utc, 0, 24 >> " + "number_prev_openings, 0, 200 >> "
				+ "cover_unigram_score, 0 , 9113 >> "
				+ "order_of_application, 1, 1000 >> "
				+ "job_unigram_score, 0 , 7000 >> " + "country, 1, 210 >> "
				+ "prev_openings, 0, 200 >> "
				+ "number_prev_applications, 1,7000 ";

		String[] tmpAr = values.split(">>");
		System.out.println("Normalizing according to the following values:");
		for (String s : tmpAr) {
			MinMaxHolder h = new MinMaxHolder();
			String[] tmpAr2 = s.split(",");
			h.setMax(Double.parseDouble(tmpAr2[2].trim()));
			h.setMin(Double.parseDouble(tmpAr2[1].trim()));
			normalizingMap.put(tmpAr2[0].trim(), h);
			System.out.println(tmpAr2[0].trim() + "," + h.getMin() + ","
					+ h.getMax());
		}
	}
}
