package kokkodis.logitModel;

public class ProbHolder {

	private Double prob;
	private Integer label;
	/**
	 * @param args
	 */
	
	
	public ProbHolder(double prob, int label) {
		super();
		this.prob = prob;
		this.label = label;
	}
	
	public Double getProb() {
		return prob;
	}

	public void setProb(double prob) {
		this.prob = prob;
	}
	public Integer getLabel() {
		return label;
	}
	public void setLabel(int label) {
		this.label = label;
	}

	public boolean isPositive() {
	
		return (label == 1)?true:false;
	}
	

}
