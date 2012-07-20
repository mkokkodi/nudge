package kokkodis.holders;

import java.util.HashSet;

import kokkodis.utils.Counter;

public class ClientHolder {

	HashSet<String> workedWithCountries;
	Counter<String> workedWithContractors;
	public ClientHolder() {
		workedWithCountries = new HashSet<String>();
		workedWithContractors = new Counter<String>();
	}
	public HashSet<String> getWorkedWithCountries() {
		return workedWithCountries;
	}
	public void setWorkedWithCountries(HashSet<String> workedWithCities) {
		this.workedWithCountries = workedWithCities;
	}
	public Counter<String> getWorkedWithContractors() {
		return workedWithContractors;
	}
	public void setWorkedWithContractors(Counter<String> workedWithContractors) {
		this.workedWithContractors = workedWithContractors;
	}
	
	

}
