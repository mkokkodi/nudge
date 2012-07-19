package kokkodis.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import kokkodis.temp.ReadGz;

public class NudgeDBQueries {

	private String driver = "org.postgresql.Driver";
	private String url = "jdbc:postgresql://localhost:2003/nudgedb";
	private Connection conn;

	public NudgeDBQueries() {
		if(ReadGz.server){
			 url = "jdbc:postgresql://localhost:5432/nudgedb";
		}else{
				 url = "jdbc:postgresql://localhost:2003/nudgedb";
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	

	}

	public Connection getConn() {
		return conn;
	}

	public void connect() {
		try {
			Class.forName(driver);
			Properties props = new Properties();
			props.setProperty("user", "nudge");// "pkm239");
			props.setProperty("password", "nudge");// "$SamEEra7");
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

	public void insertTuple(String str) {
		String selectString="";
		try {
			System.out.println("Inserting...");
			selectString = "insert into nudge.snapshots" + " values " + str;

			// System.out.println(selectString);
			PreparedStatement stmt = conn.prepareStatement(selectString);
			// System.out.println(selectString);
			// System.out.println("Executing...");
			stmt.executeUpdate();
			// System.out.println("Query executed...");

		} catch (SQLException sqle) {
			System.out.println(selectString);
			sqle.printStackTrace();
		}

	}

}
