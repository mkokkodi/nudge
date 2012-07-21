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
		String s = " 0.859 , 0.058 , 0.013  , 0.022 , 0.030 , 0.019  \n"
				+ "0.291 , 0.617 , 0.020 , 0.038 , 0.020 , 0.015 Ê \n"
				+ "0.023 , 0.007  , 0.813 , 0.093 , 0.054 ,0.010  "
				+ "\n 0.047 , 0.016 , 0.111 , 0.694 , 0.117 , 0.014  \n "
				+ "0.081 , 0.008  , 0.084 , 0.145 , 0.671 ,0.010  Ê \n"
				+ "0.182 , 0.025 , 0.052 , 0.061 , 0.034 , 0.646";

		String[] tmpAr = s.split("\n");

		String[] labels = { "Web development", "Software development",
				"Writing", "Administration", "Sales and Marketing",
				"Design and Multimedia" };
		int j = 0;
		System.out.println("source,destination,probability");
		for (String str : tmpAr) {
			String[] tmpAr2 = str.split(",");
			for (int i = 0; i < 6; i++) {
				System.out.println(labels[j] + "," + labels[i] + ","
						+ tmpAr2[i]);
			}
			j++;

		}
	}

}
