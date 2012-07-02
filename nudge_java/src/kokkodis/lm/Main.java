package kokkodis.lm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import kokkodis.db.OdeskDBQueries;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OdeskDBQueries q = new OdeskDBQueries();
		q.connect();

		HashMap<Integer, HashMap<Integer, ArrayList<String>>> hm;

		while ((hm = q.getCoversByContractorByApplication()).size() > 0) {
			String insertString = "";
			int index = 0;
			for (Entry<Integer, HashMap<Integer, ArrayList<String>>> innerMap : hm
					.entrySet()) {
				index++;
				int contractor = innerMap.getKey();
				UserUnigram uniLM = new UserUnigram();
				ArrayList<String> globalList = new ArrayList<String>();
				for (Entry<Integer, ArrayList<String>> innerEntry : innerMap
						.getValue().entrySet()) {
					innerEntry.setValue(uniLM.preProcess(innerEntry.getValue()
							.get(0), globalList));

				}
				uniLM.generateUnigramLM(globalList);

				for (Entry<Integer, ArrayList<String>> e : innerMap.getValue()
						.entrySet()) {
					double score = uniLM.computeLogProb(e.getValue());
					// System.out.println(innerMap.getKey()+" "+e.getKey()+" "+score);
					insertString += "('" + e.getKey() + "','"
							+ Math.round(score) + "','" + contractor + "'),";

				}
				if (index % 1001 == 1000) {

					q.insertLMScore(insertString.substring(0,
							insertString.length() - 1));
					insertString = "";
				}

			}
			q.insertLMScore(insertString.substring(0, insertString.length() - 1));

		}
	}

}
