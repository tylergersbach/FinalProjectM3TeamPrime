package OrderClient;

import java.io.Serializable;

import Ref.Instrument;

public class NewOrderSingle implements Serializable{

	//TODO::make these member variables private [DONE: JORGE]
	private int size;
	private float price;
	private Instrument instrument;

	public NewOrderSingle(int size,float price,Instrument instrument){
		this.size=size;
		this.price=price;
		this.instrument=instrument;
	}

	//TODO::Generated getters to use outside class
	public int getSize() {
		return this.size;
	}

	public float getPrice() {
		return this.price;
	}

	public Instrument getInstrument() {
		return this.instrument;
	}

}