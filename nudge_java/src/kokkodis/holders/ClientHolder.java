package kokkodis.holders;

import java.util.HashSet;

public class ClientHolder {

	HashSet<String> workedWithCountries;
	HashSet<String> workedWithContractors;
	public ClientHolder() {
		workedWithCountries = new HashSet<String>();
		workedWithContractors = new HashSet<String>();
	}
	public HashSet<String> getWorkedWithCountries() {
		return workedWithCountries;
	}
	public void setWorkedWithCountries(HashSet<String> workedWithCities) {
		this.workedWithCountries = workedWithCities;
	}
	public HashSet<String> getWorkedWithContractors() {
		return workedWithContractors;
	}
	public void setWorkedWithContractors(HashSet<String> workedWithContractors) {
		this.workedWithContractors = workedWithContractors;
	}
	
	

}
