/****************************************************
 * @author Marios Kokkodis                          *
 * comments/questions : mkokkodi@odesk.com     		*
 *													*					  
 *  Class Description - Building model utils.  		*	
 *													*  
 * 	*************************************************									
 */

package kokkodis.utils;

import java.io.File;
import java.io.IOException;

import kokkodis.logistic.Classify;

import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;

public class BuildUtils extends Utils {

	public BuildUtils() {
	}

	public void buildModel() {

		Problem problem = loadProblem("trainData/train" + Classify.trainTestName + ".txt");

		Parameter p = new Parameter(getSolverType(), Classify.C, Classify.eps);
		Model ml = Linear.train(problem, p);
		Linear.enableDebugOutput();

		try {
			Linear.saveModel(new File(Classify.dataPath + "model/"
					+ Classify.fileName), ml);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
