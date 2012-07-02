package kokkodis.db;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import kokkodis.probInterview.dataClasses.Contractor;
import kokkodis.textprocessing.TextPreProcessing;
import kokkodis.utils.Counter;
import kokkodis.utils.PrintToFile;

public class OdeskDBQueries {

	private static int label;

	public OdeskDBQueries() {
		// TODO Auto-generated constructor stub
	}

	private String driver = "org.postgresql.Driver";
	String url = "jdbc:postgresql://localhost:2001/odw";
	private Connection conn;

	public void connect() {
		try {
			Class.forName(driver);
			Properties props = new Properties();
			props.setProperty("user", "odw");// "pkm239");
			props.setProperty("password", "odw");// "$SamEEra7");
			conn = DriverManager.getConnection(url, props);
		} catch (SQLException e) {
			System.err.println("SQLException: " + e.getMessage());
		} catch (java.lang.ClassNotFoundException e) {
			System.err.print("ClassNotFoundException: ");
			System.err.println(e.getMessage());
		} catch (Exception e) {

			e.printStackTrace();
		}
		System.out.println("Connected");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OdeskDBQueries q = new OdeskDBQueries();
		q.connect();
		q.analyzeCoverLetters();
		// q.computePrcSkillsMacthing();
		System.out.println("End");
	}

	public HashMap<Integer, HashMap<Integer, ArrayList<String>>> getCoversByContractorByApplication() {
		HashMap<Integer, HashMap<Integer, ArrayList<String>>> hm = new HashMap<Integer, HashMap<Integer, ArrayList<String>>>();
		//PrintToFile pf = new PrintToFile();
		//pf.openFile("/Users/mkokkodi/git/nudge/nudge_java/data/cover.csv");
		try {
			String selectString = "select contractor, application, cover from panagiotis.marios_covers_train " +
					"where contractor in ( select t.contractor from panagiotis.marios_contractors_train t left outer join panagiotis.marios_application_cover_scores s " +
					"on t.contractor = s.contractor and s.unigram_score is null limit 1000 )";

			PreparedStatement stmt = conn.prepareStatement(selectString);
			// System.out.println("Executing...");
			stmt.execute();
			System.out.println("Query executed...");
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				int contractor = rs.getInt("contractor");
				int application = rs.getInt("application");
				String cover = rs.getString("cover");
				HashMap<Integer, ArrayList<String>> innerHm = hm
						.get(contractor);
				if (innerHm == null)
					innerHm = new HashMap<Integer, ArrayList<String>>();
				ArrayList<String> l = new ArrayList<String>();
				l.add(cover);
				innerHm.put(application, l);
				hm.put(contractor, innerHm);
				//String str = contractor + "," + application + ",\"" + cover
					//	+ "\"";
				//pf.writeToFile(str);
			}
			//pf.closeFile();

			return hm;

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return null;
	}

	private void analyzeCoverLetters() {
		TextPreProcessing tp = new TextPreProcessing();
		PrintToFile pf = new PrintToFile();
		pf.openFile("/Users/mkokkodi/Documents/workspace/nudge_java/lexicon/lexicon.csv");
		pf.writeToFile("word,document_frequency");
		try {
			String selectString = "select application, cover"
					+ " from panagiotis.marios_application_covers "
					+ "limit 1000000";
			PreparedStatement stmt = conn.prepareStatement(selectString);
			// System.out.println("Executing...");
			stmt.execute();
			System.out.println("Query executed...");
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				tp.process(rs.getString("cover"), rs.getInt("application"));
			}

			System.out.println("Total terms:"
					+ TextPreProcessing.docFrequencies.size());
			Set<Entry<String, Double>> set = TextPreProcessing.docFrequencies
					.getEntrySet();
			Iterator<Entry<String, Double>> setIt = set.iterator();
			while (setIt.hasNext()) {
				Entry<String, Double> me = setIt.next();
				pf.writeToFile(me.getKey() + "," + me.getValue());

			}
			pf.closeFile();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	private void insertSkills() {
		try {
			BufferedReader input = new BufferedReader(
					new FileReader(
							new File(
									"/Users/mkokkodi/Documents/workspace/nudge_java/data/seedskills.csv")));
			String line;

			while ((line = input.readLine()) != null) {
				try {
					String selectString = "insert into panagiotis.marios_seedskills values ('"
							+ line.trim() + "')";
					PreparedStatement stmt = conn
							.prepareStatement(selectString);
					// System.out.println("Executing...");
					stmt.executeUpdate();
					System.out.println("Query executed...");

				} catch (SQLException sqle) {
					sqle.printStackTrace();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void computePrcSkillsMacthing() {
		HashMap<Integer, HashSet<String>> contractorSkills = new HashMap<Integer, HashSet<String>>();
		try {
			String selectString = "select contractor,application, required_skills, skill_requested, parsedskills from panagiotis.marios_openings op inner join"
					+ " panagiotis.marios_application_info ap on op.opening = ap.opening "
					+ "where parsedskills is not null "
					+ "and prc_skills_matching is null";
			PreparedStatement stmt = conn.prepareStatement(selectString);
			// System.out.println("Executing...");
			stmt.execute();
			System.out.println("Query executed...");
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				int contractor = rs.getInt("contractor");

				HashSet<String> curContractorSkills = contractorSkills
						.get(contractor);
				int app = rs.getInt("application");
				if (curContractorSkills == null) {

					System.out.println("New contractor:" + contractor
							+ " Appl:" + app);
					curContractorSkills = getContractorSkills(contractor);
					contractorSkills.put(contractor, curContractorSkills);
				}
				/*
				 * for(String str : curContractorSkills)
				 * System.out.print(str+" "); System.out.println();
				 */
				String parsedSkills = rs.getString("parsedSkills");
				HashSet<String> jobSkills;
				if (parsedSkills != null) {
					jobSkills = parseParsedSkills(parsedSkills);
					double size = jobSkills.size();
					jobSkills.retainAll(curContractorSkills);
					double prc = jobSkills.size() / size;
					updateApplication(app, prc);

					/*
					 * System.out.println("Common Skills:"); for(String str :
					 * jobSkills) System.out.print(str+" ");
					 * System.out.println();
					 */
				}

			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	private void updateApplication(int application, double prc) {
		try {
			System.out.println("Updating..." + application);
			String selectString = "update panagiotis.marios_application_info set prc_skills_matching ="
					+ prc + " where application=" + application;
			PreparedStatement stmt = conn.prepareStatement(selectString);

			stmt.executeUpdate();
			System.out.println("Update completed.");
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	private HashSet<String> parseParsedSkills(String parsedSkills) {
		HashSet<String> jobSkills = new HashSet<String>();
		String[] tmp = parsedSkills.split(" ");
		for (String str : tmp) {
			jobSkills.add(str.trim());
		}
		return jobSkills;

	}

	public HashSet<String> getContractorSkills(int contractor) {
		HashSet<String> curContractorSkills = new HashSet<String>();
		try {
			System.out.println("Querying contractor_skills");
			String selectString = "select skill from panagiotis.marios_contractor_skills where contractor=\'"
					+ contractor + "\'";
			PreparedStatement stmt = conn.prepareStatement(selectString);
			// System.out.println("Executing...");
			stmt.execute();
			System.out.println("Skills collected.");
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				curContractorSkills.add(rs.getString("skill"));
			}

			return curContractorSkills;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return null;
	}

	public void createTrainingFile() {
		HashMap<String, Integer> locationToNominal = new HashMap<String, Integer>();

		int locationIndex = 0;
		Counter<Integer> orderMap = new Counter<Integer>();
		PrintToFile trainFile = new PrintToFile();
		trainFile
				.openFile("/Users/mkokkodi/Documents/workspace/nudge_java/data/train.txt");
		PrintToFile testFile = new PrintToFile();
		testFile.openFile("/Users/mkokkodi/Documents/workspace/nudge_java/data/test.txt");
		HashMap<Integer, Contractor> contractors = new HashMap<Integer, Contractor>();
		try {

			String selectString = "drop table if exists panagiotis.marios_V_parsedApps; "
					+ "select * "
					+ "into panagiotis.marios_V_parsedApps "
					+ "from panagiotis.marios_application_info  c   "
					+ "where c.prc_skills_matching is not null";
			PreparedStatement stmt = conn.prepareStatement(selectString);
			stmt.execute();
			selectString = "select job_type, reason, ap.opening,  ap.contractor, interview_client, interview_contractor, english, hourly_rate, "
					+ "availability_hrs, ap.date_created, c.city, c.country, hourly_agency_rate,  total_tests, yrs_exp, no_qualifications, "
					+ "total_hours, adjusted_score,  "
					+ "adjusted_score_recent, total_last_90_days, start_date, job_description, prc_skills_matching, "
					+ "job_category, pref_english_score, pref_feedback_score,prc_interviewed,"
					+
					// " required_skills, skill_requested, " +
					"pref_hourly_rate_max,  pref_hourly_rate_min, "
					// + "pref_location,
					+ "pref_test,  pref_odesk_hours "
					+ "from panagiotis.marios_V_parsedApps ap  "
					+ "inner join panagiotis.marios_contractor_profiles  c  "
					+ "on ap.contractor = c.contractor "
					+ "inner join panagiotis.marios_openings op "
					+ "on ap.opening = op.opening "
					+ ""
					+ "order by date_created " + "limit 2000000";
			stmt = conn.prepareStatement(selectString);
			// System.out.println("Executing...");
			stmt.execute();
			System.out.println("Query executed...");
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				int opening = rs.getInt("opening");
				orderMap.incrementCount(opening, 1);

				Integer curLocation = null;
				String curCountry = rs.getString("country");
				if (curCountry != null) {
					curLocation = locationToNominal.get(curCountry);
					if (curLocation == null) {
						locationIndex++;
						locationToNominal.put(curCountry, locationIndex);
					}
				}
				String instance = createInstance(rs,
						(int) orderMap.getCount(opening), curLocation);
				if (label == 0 && Math.random() < 0.15 || label == 1) {
					if (Math.random() < 0.9)
						trainFile.writeToFile(instance);
					else
						testFile.writeToFile(instance);

				}
			}
			System.out.println("File created.");
			testFile.closeFile();
			trainFile.closeFile();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	private String createInstance(ResultSet rs, int order, Integer curLocation) {
		int jobType;

		try {

			int englishScore = -1;
			double hourlyAgencyRate = -1;
			double hourlyRate = -1;
			jobType = (rs.getString("job_type").equals("Hourly")) ? 0 : 1;

			try {
				label = (rs.getString("interview_client").equals("accept") && rs
						.getString("interview_contractor").equals("accept")) ? 1
						: 0;
			} catch (NullPointerException e) {
				label = 0;
			}
			String instance = "" + label + " 1:" + jobType;

			// if (rs.getObject("english") != null) {
			// englishScore = rs.getInt("english");
			// instance += " 2:" + ((double) englishScore) / 5.0;
			// }
			// if (rs.getObject("hourly_rate") != null) {
			// hourlyRate = rs.getDouble("hourly_rate");
			// if (hourlyRate < 500)
			// instance += " 3:" + (hourlyRate) / 500;
			// }
			// if (rs.getObject("availability_hrs") != null) {
			// instance += " 4:" + (rs.getDouble("availability_hrs") / 100);
			// }
			// if (rs.getObject("hourly_agency_rate") != null) {
			// hourlyAgencyRate = rs.getDouble("hourly_agency_rate");
			// if (hourlyAgencyRate < 500)
			// instance += " 5:" + hourlyAgencyRate / 500;
			// }
			// if (rs.getObject("total_tests") != null) {
			// double totalTests = rs.getInt("total_tests");
			// if (totalTests < 50)
			// instance += " 6:" + totalTests / 30.0;
			// }
			// if (rs.getObject("yrs_exp") != null) {
			// instance += " 7:" + (rs.getDouble("yrs_exp") / 30);
			// }
			//
			// if (rs.getObject("total_hours") != null) {
			// instance += " 8:" + rs.getDouble("total_hours") / 40000;
			// }
			if (rs.getObject("adjusted_score") != null) {
				double adjustedScore = rs.getDouble("adjusted_score");
				if (adjustedScore > 0)
					instance += " 9:" + rs.getDouble("adjusted_score") / 5.0;
			}
			if (rs.getObject("adjusted_score_recent") != null) {
				double adjustedScore = rs.getDouble("adjusted_score_recent");
				if (adjustedScore > 0)
					instance += " 10:" + rs.getInt("adjusted_score_recent")
							/ 5.0;
			}
			if (rs.getObject("total_last_90_days") != null) {
				instance += " 11:" + rs.getDouble("total_last_90_days") / 5000;
			}
			if (rs.getObject("pref_english_score") != null
					&& englishScore != -1) {
				instance += " 12:"
						+ ((rs.getDouble("pref_english_score") - englishScore))
						/ 5;
			}
			if (rs.getObject("pref_hourly_rate_max") != null
					&& (hourlyAgencyRate != -1 || hourlyRate != -1)) {
				if (rs.getDouble("pref_hourly_rate_max") > 0)
					instance += " 13:"
							+ (rs.getDouble("pref_hourly_rate_max") - ((hourlyRate != -1) ? hourlyRate
									: hourlyAgencyRate)
									/ rs.getDouble("pref_hourly_rate_max"));
			}
			instance += " 14:" + order;
			instance += " 15:" + rs.getDouble("prc_skills_matching");
			if (rs.getObject("prc_interviewed") != null) {
				instance += " 16:" + rs.getDouble("prc_interviewed");
			}
			// if (curLocation != null)
			// instance += " 15:" + curLocation;
			return instance;
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}

	public void insertLMScore(String str) {
		try {
			String selectString = "insert into panagiotis.marios_application_cover_scores values" + str;
			PreparedStatement stmt = conn
					.prepareStatement(selectString);
			System.out.println(selectString);
			// System.out.println("Executing...");
			stmt.executeUpdate();
			System.out.println("Query executed...");

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		
	}

}
