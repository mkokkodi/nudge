/****************************************************
 * @author Marios Kokkodis                          *
 * comments/questions : mkokkodi@odesk.com     		*
 *													*					  
 *  Class Description  - queries for creating		* 
 *  test/train sets                     			*	
 *													*  
 * 	*************************************************									
 */

package kokkodis.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import kokkodis.logistic.Classify;
import kokkodis.utils.PrintToFile;

public class CreateTrainTest {

	/**
	 * @param args
	 */
	private static OdeskDBQueries q;
	private static String testSet = "\'2012-03-01\'";

	public static void main(String[] args) {

		if(args.length> 0)
			testSet = "'"+args[0]+"'";
		q = new OdeskDBQueries();
		q.connect();

		for (int i = 20; i < 30; i += 10) {
			System.out.println("Fetcing training set " + i);
			createSet(i, false);

			System.out.println("Fetching testing set " + i);
			createSet(i, true);
		}

		/*
		 * for(int i=10; i<20; i+=10){ dropTemp(i); }
		 */
	}

	/**
	 * 
	 * @param cat
	 * @param test
	 *            : true for creating test set (> than test date);
	 */
	private static void createSet(int cat, boolean test) {
		try {

			String fieldString = "job_type,  opening,  contractor, "
					+ "interview_client, interview_contractor, english, hourly_rate, "
					+ "availability_hrs,  contr_country,  "
					+ "hourly_agency_rate,  total_tests, yrs_exp, no_qualifications,  "
					+ "total_hours, adjusted_score,  "
					+ "adjusted_score_recent, total_last_90_days,  "
					+ " pref_english_score,  "
					+ "pref_feedback_score, interview_prc,"
					+ "pref_test,  pref_has_portfolio, "
					+ "number_prev_openings, contr_timezone, client_timezone,  "
					+ "cover_unigram_score, order_of_application, client_country, "
					+ "job_unigram_score, number_prev_applications, client";

			String selectString = "select  " + fieldString
					+ " from panagiotis.marios_train_test_cat" + cat
					+ " where  to_timestamp(date_created  / 1000)  "
					+ (test ? ">" : "<") + testSet + "order by date_created"
					+ " ";

			PreparedStatement stmt = q.getConn().prepareStatement(selectString);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			String[] fields = fieldString.split(",");
			System.out.println("Writting file...");
			PrintToFile pf = new PrintToFile();
			pf.openFile("/Users/mkokkodi/git/nudge/data/rawData/"
					+ (test ? "test" : "train") + cat + ".csv");
			fields[0] = fields[0].trim();
			String str = fields[0];

			for (int i = 1; i < fields.length; i++) {
				fields[i] = fields[i].trim();
				str += "," + fields[i];

			}
			pf.writeToFile(str);

			while (rs.next()) {
				String values = "\"" +rs.getString(fields[0])+"\"";
				for (int i = 1; i < fields.length; i++) {
					String curValue = rs.getString(fields[i]);
					values += ",\"";
					if (curValue != null)
						values +=   curValue ;
					values += "\"";
				}
				pf.writeToFile(values);
			}
			System.out.println("File written.");

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

}
