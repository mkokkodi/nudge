package kokkodis.temp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map.Entry;

import kokkodis.utils.Counter;
import kokkodis.utils.PrintToFile;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			BufferedReader input = new BufferedReader(new FileReader(new File(
					"/Users/mkokkodi/Desktop/freqs.csv")));
			String line;
			line = input.readLine();

			Counter<Integer> c = new Counter<Integer>();
			while ((line = input.readLine()) != null) {
				line = line.replaceAll("\"", "");
				c.incrementCount(Integer.parseInt(line.trim()), 1);
			}
			c.normalize();
			PrintToFile pf = new PrintToFile();
			pf.openFile("/Users/mkokkodi/Desktop/freqs_cdf.csv");
			for(Entry<Integer, Double> e: c.getEntrySet()){
				pf.writeToFile(e.getKey()+","+e.getValue());
			}
			pf.closeFile();
		} catch (IOException e) {
		}
	}

}
