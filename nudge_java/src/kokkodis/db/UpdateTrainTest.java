package kokkodis.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import kokkodis.lm.TextHolder;

public class UpdateTrainTest {

	/**
	 * @param args
	 */
	private static OdeskDBQueries q;

	public static void main(String[] args) {
		q = new OdeskDBQueries();
		q.connect();

		for (int i = 20; i < 30; i += 10) {
			System.out.println("creating table " + i);
			if (createTemp(i) > 0){
				swap(i);
				createIndices(i);
			}
			System.out.println("Table " + i + " created.");
		}

		/*
		 * for(int i=10; i<20; i+=10){ dropTemp(i); }
		 */
	}

	private static void createIndices(int cat) {
		try {
			String selectString = "create index appIndexCat"+cat+" on panagiotis.marios_train_test_cat"+cat+"  (application)";
				
			PreparedStatement stmt = q.getConn().prepareStatement(selectString);
			// System.out.println("Executing...");
			stmt.executeUpdate();
			
			selectString ="create index dateIndexCat"+cat+" on panagiotis.marios_train_test_cat"+cat+"  (date_created)";
			stmt = q.getConn().prepareStatement(selectString);
			stmt.executeUpdate();
			System.out.println("Indices created. ");

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	private static void swap(int cat) {
		try {
			String selectString = "drop table panagiotis.marios_train_test_cat"
					+ cat + "; "
					+ "select * into panagiotis.marios_train_test_cat" + cat
					+ " " + "from panagiotis.temp" + cat + "; "
					+ "drop table panagiotis.temp" + cat + ";";

			PreparedStatement stmt = q.getConn().prepareStatement(selectString);
			// System.out.println("Executing...");
			stmt.executeUpdate();
			System.out.println("Swap completed.  ");

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

	private static int createTemp(int cat) {

		try {
			String selectString = "drop table if exists panagiotis.temp" + cat
					+ "; " + "select t.*, ap.number_prev_applications "
					+ "into panagiotis.temp" + cat
					+ " from panagiotis.marios_train_test_cat" + cat
					+ " t inner join panagiotis.temp ap "
					+ "on t.application = ap.application";

			PreparedStatement stmt = q.getConn().prepareStatement(selectString);
			// System.out.println("Executing...");
			stmt.executeUpdate();
			System.out.println("Temp table created! ");

			return 1;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			return -1;
		}
	}

}
