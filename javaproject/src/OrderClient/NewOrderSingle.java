package OrderClient;

import java.io.Serializable;

import Ref.Instrument;

public class NewOrderSingle implements Serializable{

	//TODO::make these member variables private
	public int size;
	public float price;
	public Instrument instrument;

	public NewOrderSingle(int size,float price,Instrument instrument){
		this.size=size;
		this.price=price;
		this.instrument=instrument;
	}
}