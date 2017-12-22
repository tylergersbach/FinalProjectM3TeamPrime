package Ref;

import java.io.Serializable;
import java.util.Date;

public class Instrument implements Serializable{
	//TODO::make member variables private [DONE: JORGE]

	private long id;
	private String name;
	private Ric ric;
	private String isin;
	private String sedol;
	private String bbid;

	public Instrument(Ric ric) {
		this.ric = ric;
	}

	public String toString() {
		return ric.getRic();
	}
}

class EqInstrument extends Instrument{
	private Date exDividend;

	public EqInstrument(Ric ric) {
		super(ric);
	}
}

class FutInstrument extends Instrument{
	private Date expiry;
	private Instrument underlier;

	public FutInstrument(Ric ric) {
		super(ric);
	}
}

//TODO::I have no idea what this is asking us to do
/*TODO
Index
bond
methods
*/