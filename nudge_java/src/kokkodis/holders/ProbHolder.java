/****************************************************
 * @author Marios Kokkodis                          *
 * comments/questions : mkokkodi@odesk.com     		*
 *													*					  
 *  Class Description   - Probabiltiy holder class. *	
 *													*  
 * 	*************************************************									
 */

package kokkodis.holders;

public class ProbHolder {

	private String opening;
	private String conractor;
	private Double prob;
	private Integer actualLabel;

	/**
	 * @param args
	 */

	public ProbHolder(double prob, int label) {
		super();
		this.prob = prob;
		this.actualLabel = label;
	}

	public Double getProb() {
		return prob;
	}

	public void setProb(double prob) {
		this.prob = prob;
	}

	public Integer getActualabel() {
		return actualLabel;
	}

	public void setActuaLabel(int label) {
		this.actualLabel = label;
	}

	public boolean isPositive() {

		return (actualLabel == 1) ? true : false;
	}

	public String getOpening() {
		return opening;
	}

	public void setOpening(String opening) {
		this.opening = opening;
	}

	public String getConractor() {
		return conractor;
	}

	public void setConractor(String conractor) {
		this.conractor = conractor;
	}

}
