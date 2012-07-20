package kokkodis.temp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import kokkodis.db.NudgeDBQueries;

public class ReadGz {

	public static boolean server = false;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String f;

		int ignorelines = 0;
		InputStream in;
		InputStreamReader r = null;

		if (args.length > 0) {
			f = args[0];
			server = true;
			try {
				in = new GZIPInputStream(new FileInputStream(f));
				r = new InputStreamReader(in);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ignorelines = Integer.parseInt(args[1]);

		} else
			f = "/Users/mkokkodi/git/nudge/nudge_java/tmp/test.csv";
		try {

			NudgeDBQueries q = new NudgeDBQueries();
			q.connect();
			// BufferedReader input = new BufferedReader(r);//new
			// FileReader(r.));
			// BufferedReader input = new BufferedReader(new FileReader(f));

			// String line;
			// line = input.readLine();
			CSVReader reader;
			if (r != null)
				reader = new CSVReader(r);
			else
				reader = new CSVReader(new FileReader(f));
			String[] nextLine;
			int index = 0;
			String insertString = "";
			int iteration = 0;
			for (int i = 0; i < ignorelines; i++)
				reader.readNext();
			System.out.println("Starting parsing after line " + ignorelines);
			while ((nextLine = reader.readNext()) != null) {

				insertString += "('" + nextLine[0] + "'";

				if (nextLine.length == 14) {
					for (int i = 3; i < 11; i++) {
						if (nextLine[i].length() > 0)
							insertString += ",'"
									+ nextLine[i].replaceAll("'", " ") + "'";
						else
							insertString += ",null";
					}
					for (int i = 11; i < 14; i++) {
						nextLine[i] = nextLine[i].replaceAll("\\(", "");
						String scores = nextLine[i].replaceAll("\\)", "");
						if (i > 11) {
							String[] tmpAr = scores.split(",");
							if (tmpAr.length == 6) {
								for (String s : tmpAr) {
									if (s.length() > 0)
										insertString += "," + s;
									else
										insertString += ",null";

								}
							} else {
								for (int j = 0; j < 6; j++)
									insertString += ",null";
							}
						} else {
							if (scores.length() > 0)
								insertString += ",'"
										+ scores.replaceAll("'", " ") + "'";
							else
								insertString += ",null";
						}

					}
					insertString += "),";
					if (index == 1000) {
						q.insertTuple(insertString.substring(0,
								insertString.length() - 1));
						index = 0;
						insertString = "";
						System.out.println("Iteration " + iteration
								+ " completed.");
						iteration++;

					} else {
						index++;
					}

				} else {
					System.out.println("Weird tuple.");
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// }

	}

	private static String getValue(String s) {
		String[] tmpAr2 = s.split(":");
		String res = tmpAr2[1].replaceAll("\'", "");
		return res;
	}

}
