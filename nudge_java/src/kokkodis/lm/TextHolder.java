package kokkodis.lm;

import java.util.ArrayList;

public class TextHolder {

	private int contractor;
	private int application;
	private ArrayList<String> text;
	public TextHolder() {
		text = new ArrayList<String>();
	}
	public int getApplication() {
		return application;
	}
	public void setApplication(int contractor) {
		this.application = contractor;
	}
	public ArrayList<String> getText() {
		return text;
	}
	public void setText(ArrayList<String> cover) {
		this.text = cover;
	}
	public int getContractor() {
		return contractor;
	}
	public void setContractor(int contractor) {
		this.contractor = contractor;
	}
	

}
