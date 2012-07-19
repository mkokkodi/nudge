package kokkodis.textprocessing;

import java.util.ArrayList;

import kokkodis.db.OdeskDBQueries;

public class SimilarityJobContractor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OdeskDBQueries q = new OdeskDBQueries();
		q.connect();
		ArrayList<String> textList  = q.selectJobText(20);
	}

}
