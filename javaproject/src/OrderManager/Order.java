package OrderManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Ref.Instrument;

public class Order implements Serializable {

	//TODO::make these private [DONE: JORGE]
	//TODO these should all be longs [DONE: JORGE]
	private int id; 				//TODO::This is being used to index into an array so I changed it back to an int, should this also be a long??
	private long orderRouter;
	private long clientOrderID; 	//TODO refactor to lowercase C [DONE: JORGE]
	private long size;
	private double[] bestPrices;	//JORGE: This stays a double because we are dealing with prices. Maybe set as a hashmap instead
	private long bestPriceCount;

	//TODO::Moved these up from below some methods and made them private [DONE: JORGE]
	private int clientID;
	private Instrument instrument;
	private double initialMarketPrice;
	private ArrayList<Order> slices;
	private ArrayList<Fill> fills;
	private char orderStatus = 'A'; //OrdStatus is Fix 39, 'A' is 'Pending New'

	public Order(int clientId, long clientOrderID, Instrument instrument, long size){
		this.clientID = clientId;
		this.clientOrderID = clientOrderID;
		this.instrument = instrument;
		this.size = size;

		fills = new ArrayList<Fill>();
		slices = new ArrayList<Order>();
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//TODO::Generated getters and setters for our private variables. Used in OrderManager
	public int getID() {
		return this.id;
	}

	public void setInitialMarketPrice(double price) {
		this.initialMarketPrice = price;
	}

	public double getInitialMarketPrice() {
		return this.initialMarketPrice;
	}

	public ArrayList<Order> getSlices() {
		return this.slices;
	}

	public void setBestPrices(double[] prices) {
		this.bestPrices = prices;
	}

	public long getBestPriceLength() {
		return this.bestPrices.length;
	}

	public double getBestPrice(int index) {
		return this.bestPrices[index];
	}

	public void setBestPriceCount(long count) {
		this.bestPriceCount = count;
	}

	public long getBestPriceCount() {
		return this.bestPriceCount;
	}

	public Instrument getInstrument() {
		return this.instrument;
	}

	public int getClientID() {
		return this.clientID;
	}

	public long getClientOrderID() {
		return this.clientOrderID;
	}

	public char getOrderStatus() {
		return this.orderStatus;
	}

	public void setOrderStatus(char status) {
		this.orderStatus = status;
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////

	//TODO::Changed totalSizeOfSlices and return type to long to match variable specifications
	public long sliceSizes(){
		long totalSizeOfSlices = 0;
		for (Order c : slices)
			totalSizeOfSlices += c.size;

		return totalSizeOfSlices;
	}

	//TODO::Changed slice type from type int to type long to match requirements
	public int newSlice(long sliceSize){
		slices.add(new Order(id, clientOrderID, instrument, sliceSize));
		return slices.size() - 1;
	}

	//TODO::Made private because it is only a helper method
	private int sizeFilled(){
		int filledSoFar=0;
		for(Fill f : fills)
			filledSoFar += f.getSize();		//TODO::Changed this from f.size to f.getSize() [DONE: JORGE]

		for(Order c : slices)
			filledSoFar += c.sizeFilled();

		return filledSoFar;
	}

	//TODO::Changed the return ro a long to match variable specifications
	public long sizeRemaining() {
		return size - sizeFilled();
	}

	//Status state;
	float price(){
		//TODO this is buggy as it doesn't take account of slices. Let them fix it
		float sum = 0;
		for(Fill fill : fills){
			sum += fill.getPrice();		//TODO::replaced fill.price with fill.getPrice() [DONE: JORGE]
		}
		return sum / fills.size();
	}

	//TODO::Changed param size to type long to match var specs
	void createFill(long size, double price){
		fills.add(new Fill(size, price));
		if (sizeRemaining() == 0){
			orderStatus = '2';
		} else {
			orderStatus = '1';
		}
	}

	//TODO::changed all int variables to long to match variable specs
	void cross(Order matchingOrder){
		//pair slices first and then parent
		for(Order slice : slices) {
			if(slice.sizeRemaining() == 0)
				continue;

			//TODO could optimise this to not start at the beginning every time
			for(Order matchingSlice : matchingOrder.slices){
				long msze = matchingSlice.sizeRemaining();
				if( msze == 0)
					continue;

				long sze = slice.sizeRemaining();
				if( sze <= msze) {
					 slice.createFill(sze,initialMarketPrice);
					 matchingSlice.createFill(sze, initialMarketPrice);
					 break;
				}
				//sze>msze
				slice.createFill(msze,initialMarketPrice);
				matchingSlice.createFill(msze, initialMarketPrice);
			}
			long sze = slice.sizeRemaining();
			long mParent=matchingOrder.sizeRemaining() - matchingOrder.sliceSizes();

			if (sze > 0 && mParent > 0) {
				if (sze >= mParent) {
					slice.createFill(sze, initialMarketPrice);
					matchingOrder.createFill(sze, initialMarketPrice);
				} else {
					slice.createFill(mParent, initialMarketPrice);
					matchingOrder.createFill(mParent, initialMarketPrice);					
				}
			}
			//no point continuing if we didn't fill this slice, as we must already have fully filled the matchingOrder
			if(slice.sizeRemaining()>0)break;
		}

		if( sizeRemaining() > 0) {
			for(Order matchingSlice : matchingOrder.slices) {
				long msze = matchingSlice.sizeRemaining();
				if( msze == 0)
					continue;

				long sze=sizeRemaining();
				if(sze<=msze){
					 createFill(sze,initialMarketPrice);
					 matchingSlice.createFill(sze, initialMarketPrice);
					 break;
				}
				//sze>msze
				createFill(msze,initialMarketPrice);
				matchingSlice.createFill(msze, initialMarketPrice);
			}

			long sze = sizeRemaining();
			long mParent = matchingOrder.sizeRemaining() - matchingOrder.sliceSizes();
			if (sze > 0 && mParent > 0){
				if (sze >= mParent){
					createFill(sze, initialMarketPrice);
					matchingOrder.createFill(sze, initialMarketPrice);
				} else {
					createFill(mParent,initialMarketPrice);
					matchingOrder.createFill(mParent, initialMarketPrice);					
				}
			}
		}
	}

	void cancel(){
		//state=cancelled
	}

}

class Basket{
	Order[] orders;
}

class Fill implements Serializable{

	//TODO::Make these private [DONE: JORGE]
	//long id;
	private long size;				//TODO::Changed type from int to long to match variable specs
	private double price;

	//TODO::Changed size from int to long to match variable specs
	Fill(long size, double price){
		this.size=size;
		this.price=price;
	}

	//TODO::Added getters for size and price
	public long getSize() {
		return this.size;
	}

	public double getPrice() {
		return this.price;
	}
}
