package kokkodis.db;

import ipeirotis.readability.Readability;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

import kokkodis.lm.TextHolder;
import kokkodis.textprocessing.TextPreProcessing;
import kokkodis.utils.PrintToFile;

public class OdeskDBQueries {

	private static int label;

	public OdeskDBQueries() {
		// TODO Auto-generated constructor stub
	}

	private String driver = "org.postgresql.Driver";
	String url = "jdbc:postgresql://localhost:2001/odw";
	private Connection conn;

	public Connection getConn() {
		return conn;
	}

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

		System.out.println("End");
	}

	public HashMap<Integer, ArrayList<TextHolder>> getCoversByContractorByApplication() {
		HashMap<Integer, ArrayList<TextHolder>> hm = new HashMap<Integer, ArrayList<TextHolder>>();
		// PrintToFile pf = new PrintToFile();
		// pf.openFile("/Users/mkokkodi/git/nudge/nudge_java/data/cover.csv");
		System.out.println("Selecting cover letters...");
		try {
			String selectString = "select c.application, c.cover, c.contractor "
					+ "from "
					+ "(  "
					+ "select distinct(con.contractor) "
					+ "from panagiotis.temp_contractors con "
					+ "left outer join  panagiotis.marios_application_cover_scores  s "
					+ "on con.contractor = s.contractor where unigram_score is null"
					+ " limit 100) r "
					+ "inner join panagiotis.marios_application_cover_contractor c on r.contractor = c.contractor "
					+ "order by date_created";

			PreparedStatement stmt = conn.prepareStatement(selectString);
			// System.out.println("Executing...");
			stmt.execute();
			System.out.println("Cover letters ready to parse.");
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				int contractor = rs.getInt("contractor");
				// System.out.println("in:"+contractor);
				int application = rs.getInt("application");
				String cover = rs.getString("cover");
				ArrayList<TextHolder> holderList = hm.get(contractor);
				if (holderList == null)
					holderList = new ArrayList<TextHolder>();
				TextHolder ch = new TextHolder();
				ch.getText().add(cover);
				ch.setApplication(application);
				holderList.add(ch);
				hm.put(contractor, holderList);
				// String str = contractor + "," + application + ",\"" + cover
				// + "\"";
				// pf.writeToFile(str);
			}
			// pf.closeFile();

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

	public void insertScores(String str, String table) {
		try {
			System.out.println("Inserting...");
			String selectString = "insert into panagiotis." + table
					+ " values " + str;

			// System.out.println(selectString);
			PreparedStatement stmt = conn.prepareStatement(selectString);
			// System.out.println(selectString);
			// System.out.println("Executing...");
			stmt.executeUpdate();
			// System.out.println("Query executed...");

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}

	}

	public HashMap<Integer, ArrayList<TextHolder>> getJobsByContractorByApplication() {
		HashMap<Integer, ArrayList<TextHolder>> hm = new HashMap<Integer, ArrayList<TextHolder>>();
		System.out.println("Selecting Job descriptions...");
		try {
			String selectString = "select c.application, c.job_description, c.contractor "
					+ "from (  select distinct(con.contractor) "
					+ "from panagiotis.temp_contractors_for_jobs con  "
					+ "left outer join  panagiotis.marios_application_job_score  s "
					+ "on con.contractor = s.contractor "
					+ "where unigram_score is null  "
					+ "limit 100) r "
					+ "inner join panagiotis.temp_application_job c "
					+ "on r.contractor = c.contractor "
					+ "order by date_created;";

			PreparedStatement stmt = conn.prepareStatement(selectString);
			// System.out.println("Executing...");
			stmt.execute();
			System.out.println("Cover letters ready to parse.");
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				int contractor = rs.getInt("contractor");
				// System.out.println("in:"+contractor);
				int application = rs.getInt("application");
				String text = rs.getString("job_description");
				ArrayList<TextHolder> holderList = hm.get(contractor);
				if (holderList == null)
					holderList = new ArrayList<TextHolder>();
				TextHolder ch = new TextHolder();
				ch.getText().add(text);
				ch.setApplication(application);
				holderList.add(ch);
				hm.put(contractor, holderList);
				// String str = contractor + "," + application + ",\"" + cover
				// + "\"";
				// pf.writeToFile(str);
			}
			// pf.closeFile();

			return hm;

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return null;
	}

	public boolean estimateBlurbScores() {
		System.out.println("Selecting blurbs ...");
		try {
			String selectString = "select c.contractor, c.blurb "
					+ "from panagiotis.temp_blurbs c "
					+ "left outer join panagiotis.marios_contractor_blurb_scores p "
					+ "on c.contractor = p.contractor  "
					+ "where p.smog is null " + "limit 5000";

			PreparedStatement stmt = conn.prepareStatement(selectString);
			// System.out.println("Executing...");
			stmt.execute();
			System.out.println("Blurbs are here - let's parse them.");
			ResultSet rs = stmt.getResultSet();
			String insertString = "";
			if (rs.next())
				insertString += parseReadabilityText(rs);
			else
				return false;
			while (rs.next()) {
				insertString += "," + parseReadabilityText(rs);
			}
			insertScores(insertString, "marios_contractor_blurb_scores");
			return true;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			return false;
		}
	}

	private String parseReadabilityText(ResultSet rs) {

		try {
			int contractor = rs.getInt("contractor");

			String text = rs.getString("blurb");
			// System.out.println("in:"+contractor);
			Readability r = new Readability(text);
			// System.out.println(text);
			if(text.contains("[a-z]+")){
			Double ari = null;
			try {
				ari = r.getARI();
			} catch (NumberFormatException e) {
				System.out.println(text);
				System.out.println("Smog:" + r.getSMOG());
			}

			String insertString = "('" + contractor + "','" + r.getSMOG()
					+ "','" + ari + "','" + r.getColemanLiau() + "','"
					+ r.getFleschKincaidGradeLevel() + "','"
					+ r.getGunningFog() + "','" + r.getSMOGIndex() + "','"
					+ r.getCharacters() + "','" + r.getWords() + "','"
					+ r.getSentences() + "','" + r.getSyllables() + "','"
					+ r.getComplex() + "')";
			return insertString;
			}
			else return "('" + contractor + "','" + -1
					+ "','" + 0 + "','" + 0 + "','"
					+ 0 + "','"
					+ 0 + "','" + 0 + "','"
				+ 0 + "','" + 0 + "','"
					+ 0 + "','" + 0 + "','"
					+ 0 + "')";
		} catch (SQLException e) {

			e.printStackTrace();
			return "";
		}

	}

	public ArrayList<String> selectJobText(int i) {
		System.out.println("collecting text ...");
		try {
			String selectString = "select c.contractor, c.blurb "
					+ "from panagiotis.temp_blurbs c "
					+ "left outer join panagiotis.marios_contractor_blurb_scores p "
					+ "on c.contractor = p.contractor  "
					+ "where p.smog is null " + "limit 5000";

			PreparedStatement stmt = conn.prepareStatement(selectString);
			// System.out.println("Executing...");
			stmt.execute();
			System.out.println("Blurbs are here - let's parse them.");
			ResultSet rs = stmt.getResultSet();
			String insertString = "";
		
			while (rs.next()) {
			}
	
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
		return null;
	}

}
