package kokkodis.lm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import kokkodis.db.OdeskDBQueries;

public class JobsMain {

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

			if ((hm = q.getJobsByContractorByApplication()).size() > 0) {
				String insertString = "";
				index++;

				for (Entry<Integer, ArrayList<TextHolder>> contractoreEntry : hm
						.entrySet()) {
					int contractor = contractoreEntry.getKey();
					UserUnigram uniLM = new UserUnigram();
					// System.out.println(contractor);
					for (TextHolder textHolder : contractoreEntry.getValue()) {

						String coverLetter = textHolder.getText().get(0);
						// System.out.println(coverLetter);
						ArrayList<String> words = uniLM.preProcess(coverLetter,
								null);
						if (UserUnigram.wordCounter.size() > 1) {

							double score = uniLM.computeLogProb(words);
							// System.out.println(innerMap.getKey()+" "+e.getKey()+" "+score);
							insertString += "('" + textHolder.getApplication()
									+ "','" + Math.round(score) + "','"
									+ contractor + "'),";
							// System.out.println(coverHolder.getApplication() +
							// "','"
							// + Math.round(score) + "','" + contractor);
						} else {
							insertString += "('" + textHolder.getApplication()
									+ "','" + -1 + "','" + contractor + "'),";
						}
						uniLM.generateUnigramLM(words);

					}

				}
			
				if (insertString.length() > 0)
					q.insertLMScore(insertString.substring(0,
							insertString.length() - 1),
							"marios_application_job_score");
				
				System.out.println("Iteration " + index + " completed.");

			} else
				break;
		}
	}

}
