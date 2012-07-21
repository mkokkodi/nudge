/****************************************************
 * @author Marios Kokkodis                          *
 * comments/questions : mkokkodi@odesk.com     		*
 *													*					  
 *  Class Description - Calculates readability 
 *  indices for users' blurbs.                 		*	
 *													*  
 * 	*************************************************									
 */


package kokkodis.readibility;


import kokkodis.db.OdeskDBQueries;

public class ClulateBlurbScores {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		OdeskDBQueries q = new OdeskDBQueries();
		q.connect();


		int index = 0;
		System.out.println("Starting...");
		while (q.estimateBlurbScores())
			
		{
			System.out.println("Iteration " + index + " completed. ");
			index++;
		}
	}

}
