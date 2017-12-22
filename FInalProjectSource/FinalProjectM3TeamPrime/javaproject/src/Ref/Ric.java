package Ref;

import java.io.Serializable;

public class Ric implements Serializable{

	//TODO::make this private [DONE: JORGE]
	private String ric;

	public Ric(String ric) {
		this.ric = ric;
	}
	public String getEx() {
		return ric.split(".")[1];
	}

	public String getCompany() {
		return ric.split(".")[0];
	}

	//TODO::Added this to be able to use it in the Instrument toString method
	public String getRic() {
		return this.ric;
	}
}