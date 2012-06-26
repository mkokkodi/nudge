package kokkodis.probInterview.dataClasses;

public class Contractor {
private int id;
private double quality; //feedbackscore
private String city;
private String country;
private int english;
private double hourlyRate;
private String primaryCategory;
private int timeZone;
private int totalTests;
private int yrsExperience;
private int numberOfQualifications;
private boolean exposedFullName;



public Contractor() {
	super();
}
public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}
public double getQuality() {
	return quality;
}
public void setQuality(double quality) {
	this.quality = quality;
}
public String getCity() {
	return city;
}
public void setCity(String city) {
	this.city = city;
}
public String getCountry() {
	return country;
}
public void setCountry(String country) {
	this.country = country;
}
public int getEnglish() {
	return english;
}
public void setEnglish(int english) {
	this.english = english;
}
public double getHourlyRate() {
	return hourlyRate;
}
public void setHourlyRate(double hourlyRate) {
	this.hourlyRate = hourlyRate;
}
public String getPrimaryCategory() {
	return primaryCategory;
}
public void setPrimaryCategory(String primaryCategory) {
	this.primaryCategory = primaryCategory;
}
public int getTimeZone() {
	return timeZone;
}
public void setTimeZone(int timeZone) {
	this.timeZone = timeZone;
}
public int getTotalTests() {
	return totalTests;
}
public void setTotalTests(int totalTests) {
	this.totalTests = totalTests;
}
public int getYrsExperience() {
	return yrsExperience;
}
public void setYrsExperience(int yrsExperience) {
	this.yrsExperience = yrsExperience;
}
public int getNumberOfQualifications() {
	return numberOfQualifications;
}
public void setNumberOfQualifications(int numberOfQualifications) {
	this.numberOfQualifications = numberOfQualifications;
}
public boolean isExposedFullName() {
	return exposedFullName;
}
public void setExposedFullName(boolean exposedFullName) {
	this.exposedFullName = exposedFullName;
}

}
