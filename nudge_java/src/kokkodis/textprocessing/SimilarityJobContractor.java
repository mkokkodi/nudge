/****************************************************
 * @author Marios Kokkodis                          *
 * comments/questions : mkokkodi@odesk.com     		*
 *													*					  
 *  Class Description  : Estimates the cosine
 *  similarity of contractor profile and 
 *  job description		                     		*	
 *													*  
 * 	*************************************************									
 */

package kokkodis.textprocessing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;

import kokkodis.db.OdeskDBQueries;
import kokkodis.utils.Counter;
import kokkodis.utils.PrintToFile;

public class SimilarityJobContractor {

	/**
	 * @param args
	 */
	public static HashSet<String> featureVector;

	public static void main(String[] args) {

		System.out.println("Loading Vector of bigrams...");
		featureVector = getFeatureVector();
		System.out.println("Queryng db...");
		// createLexicon();
		OdeskDBQueries q = new OdeskDBQueries();
		q.connect();

		int i = 0;
		while (q.estimateCosineSimilarity()) {
			System.out.println("Iteration " + i + " completed. ");
			i++;
		}
	}

	private static HashSet<String> getFeatureVector() {
		HashSet<String> hm = new HashSet<String>();
		try {
			BufferedReader input = new BufferedReader(new FileReader(
					"/Users/mkokkodi/git/nudge"
							+ "/nudge_java/lexicon/jobsFreq.csv"));
			String line;
			line = input.readLine();
			while ((line = input.readLine()) != null) {
				String[] tmpAr = line.split(",");
				double docFreq = Double.parseDouble(tmpAr[2].trim());
				//double wordFreq = Double.parseDouble(tmpAr[1]);
				if (docFreq > 3) {
					hm.add(tmpAr[1]);
				}
			}
			System.out.println("Size:" + hm.size());
		} catch (IOException e) {
		}
		return null;
	}

	private static void createLexicon() {
		try {
			System.out.println("Start reading..");
			BufferedReader input = new BufferedReader(
					new FileReader(
							"/Users/mkokkodi/git/nudge/data/lexicon/jobsDescription.csv"));
			String line;
			line = input.readLine();
			Counter<String> globalCounter = new Counter<String>();
			Counter<String> docCounter = new Counter<String>();
			while ((line = input.readLine()) != null) {
				TextPreProcessing tp = new TextPreProcessing();
				tp.process(globalCounter, docCounter, line);
			}
			System.out.println("Reading completed...Start printing.");
			PrintToFile pf = new PrintToFile();
			pf.openFile("/Users/mkokkodi/git/nudge/nudge_java/lexicon/jobsFreq.csv");
			pf.writeToFile("word,word_freq, doc_freq");
			for (Entry<String, Double> e : globalCounter.getEntrySet()) {
				pf.writeToFile(e.getKey() + "," + e.getValue() + ","
						+ docCounter.getCount(e.getKey()));
			}

		} catch (IOException e) {
		}
	}

}
