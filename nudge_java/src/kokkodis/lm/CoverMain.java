/****************************************************
 * @author Marios Kokkodis                          *
 * comments/questions : mkokkodi@odesk.com     		*
 *													*					  
 *  Class Description  - Calculates unigram 
 *  cover score and stores it i DB.            		*	
 *													*  
 * 	*************************************************									
 */

package kokkodis.lm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import kokkodis.db.OdeskDBQueries;
import kokkodis.holders.TextHolder;

public class CoverMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OdeskDBQueries q = new OdeskDBQueries();
		q.connect();

		HashMap<Integer, ArrayList<TextHolder>> hm;

		int index = 0;
		System.out.println("Starting...");
		while (true) {
			if ((hm = q.getCoversByContractorByApplication()).size() > 0) {
				String insertString = "";
				index++;

				for (Entry<Integer, ArrayList<TextHolder>> contractoreEntry : hm
						.entrySet()) {
					int contractor = contractoreEntry.getKey();
					UserUnigram uniLM = new UserUnigram();
					for (TextHolder coverHolder : contractoreEntry.getValue()) {

						String coverLetter = coverHolder.getText().get(0);
						ArrayList<String> words = uniLM.preProcess(coverLetter,
								null);
						if (UserUnigram.wordCounter.size() > 1) {

							double score = uniLM.computePerplexity(words);
							insertString += "('" + coverHolder.getApplication()
									+ "','" + Math.round(score) + "','"
									+ contractor + "'),";
						} else {
							insertString += "('" + coverHolder.getApplication()
									+ "','" + -1 + "','" + contractor + "'),";
						}
						uniLM.generateUnigramLM(words);

					}

				}
				if (insertString.length() > 0)
					q.insertScores(insertString.substring(0,
							insertString.length() - 1),
							"marios_application_cover_scores");
				System.out.println("Iteration " + index + " completed.");

			} else
				break;
		}
	}
}
