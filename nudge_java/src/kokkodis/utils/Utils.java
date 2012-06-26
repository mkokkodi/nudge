package kokkodis.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Utils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		createCSV();
	}
	
	private static void createCSV(){
		try {
			PrintToFile pf = new PrintToFile();
			pf.openFile("/Users/mkokkodi/Documents/workspace/nudge_java/data/seedskills.csv");
			File f = new File("/Users/mkokkodi/Documents/workspace/nudge_java/data/seedskills.json");
			BufferedReader input = new BufferedReader(new FileReader(f));
			String line;
			//line = input.readLine();

			while ((line = input.readLine()) != null) {
			//	if(line.contains("11g-troubleshooting"))
				//		line = line.split("11g-troubleshooting")[1];
				String [] tmpAr = line.split(",");
				for(String str: tmpAr)
					pf.writeToFile(str.replaceAll("\"",""));
				
			}
			pf.closeFile();
			
		} catch (IOException e) {
		}
	}

}
