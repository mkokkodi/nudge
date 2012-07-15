package kokodis.holders;

public class ContractorHolder {

	private double currentFeedbackScore;
	private double prcSkills;
	private double englishScore;
	private double coverLM;
	private double jobsLM;
	
	
	public ContractorHolder() {
		coverLM = -1;
		jobsLM = -1;
	}


	public double getCurrentFeedbackScore() {
		return currentFeedbackScore;
	}


	public void setCurrentFeedbackScore(double currentFeedbackScore) {
		this.currentFeedbackScore = currentFeedbackScore;
	}


	public double getPrcSkills() {
		return prcSkills;
	}


	public void setPrcSkills(double prcSkills) {
		this.prcSkills = prcSkills;
	}


	public double getEnglishScore() {
		return englishScore;
	}


	public void setEnglishScore(double englishScore) {
		this.englishScore = englishScore;
	}


	public double getCoverLM() {
		return coverLM;
	}


	public void setCoverLM(double coverLM) {
		this.coverLM = coverLM;
	}


	public double getJobsLM() {
		return jobsLM;
	}


	public void setJobsLM(double jobsLM) {
		this.jobsLM = jobsLM;
	}

}
