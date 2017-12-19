package OrderManager;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.Map;

import Database.Database;
import LiveMarketData.LiveMarketData;
import OrderClient.NewOrderSingle;
import OrderRouter.Router;
import OrderRouter.Router.api;
import TradeScreen.TradeScreen;

public class OrderManager {

	private LiveMarketData liveMarketData; //Changed from static to non static
	private Map<Integer,Order> orders; //debugger will do this line as it gives state to the object
	//currently recording the number of new order messages we get. TODO why? use it for more?
	private int id; //debugger will do this line as it gives state to the object
	private Socket[] orderRouters; //debugger will skip these lines as they dissapear at compile time into 'the object'/stack
	private Socket[] clients;
	private Socket trader;
	private ObjectInputStream isc;
	private ObjectInputStream isr;


	//TODO::Break this method up into smaller methods
	//@param args the command line arguments
	public OrderManager(InetSocketAddress[] orderRouters, InetSocketAddress[] clients,InetSocketAddress trader,LiveMarketData liveMarketData)throws IOException, ClassNotFoundException, InterruptedException{

		this.liveMarketData = liveMarketData;
		orders = new HashMap<>();
		this.trader = connect(trader);

		//for the router connections, copy the input array into our object field.
		//but rather than taking the address we create a socket+ephemeral port and connect it to the address
		this.orderRouters = new Socket[orderRouters.length];
		int i = 0; //need a counter for the the output array
		for (InetSocketAddress location : orderRouters) {
			this.orderRouters[i] = connect(location);
			i++;
		}

		//repeat for the client connections
		this.clients = new Socket[clients.length];
		i = 0;
		for (InetSocketAddress location : clients){
			this.clients[i]=connect(location);
			i++;
		}

		while(true){
			checkClients();
			checkRouters();
			checkTrader();
		}
	}

	private void checkClients() throws IOException, ClassNotFoundException{
		int clientId;
		Socket client;
		//TODO this is pretty cpu intensive, use a more modern polling/interrupt/select approach
		//we want to use the arrayindex as the clientId, so use traditional for loop instead of foreach
		for (clientId = 0; clientId < this.clients.length; clientId++){ //check if we have data on any of the sockets
			client = this.clients[clientId];
			if (0 < client.getInputStream().available()) { //if we have part of a message ready to read, assuming this doesn't fragment messages
				//TODO::Change this so that it is only created once
                isc = new ObjectInputStream(client.getInputStream());
				String method = (String)isc.readObject();
				System.out.println(Thread.currentThread().getName()+" calling "+method);
				switch(method){ //determine the type of message and process it
					//call the newOrder message with the clientId and the message (clientMessageId,NewOrderSingle)
					case "newOrderSingle":
						newOrder(clientId, isc.readInt(), (NewOrderSingle)isc.readObject());
						break;
					//TODO create a default case which errors with "Unknown message type"+...
					default: throw new IOException("Unknown message type");
				}
			}
		}
	}

	private void checkRouters() throws IOException, ClassNotFoundException{
		int routerId;
		Socket router;
		for (routerId = 0; routerId < this.orderRouters.length; routerId++){ //check if we have data on any of the sockets
			router = this.orderRouters[routerId];
			if (0 < router.getInputStream().available()){ //if we have part of a message ready to read, assuming this doesn't fragment messages
				//TODO::Change this so that it is only created once
                isr = new ObjectInputStream(router.getInputStream());
				String method = (String)isr.readObject();
				System.out.println(Thread.currentThread().getName()+" calling "+method);
				switch(method){ //determine the type of message and process it
					case "bestPrice":
						int OrderId = isr.readInt();
						int sliceID = isr.readInt();

						Order slice = orders.get(OrderId).getSlices().get(sliceID);
						//TODO::I commented this out. Should I create a method in Order called setBestPrice(int routerID, double price)
						//TODO::Maybe even make best prices a hashMap
						//slice.bestPrices[routerId] = is.readDouble();
						slice.setBestPriceCount(slice.getBestPriceCount() + 1);

						if(slice.getBestPriceCount() == slice.getBestPriceLength())
							reallyRouteOrder(sliceID, slice);
						break;
					case "newFill":
						newFill(isr.readInt(), isr.readInt(), isr.readInt(), isr.readDouble());
						break;
				}
			}
		}
	}

	private void checkTrader() throws IOException, ClassNotFoundException{
		if (0 < this.trader.getInputStream().available()){
			ObjectInputStream is = new ObjectInputStream(this.trader.getInputStream());
			String method = (String)is.readObject();
			System.out.println(Thread.currentThread().getName()+" calling "+method);
			switch(method){
				case "acceptOrder":
					acceptOrder(is.readInt());
					break;
				case "sliceOrder":
					sliceOrder(is.readInt(), is.readInt());
					break;
			}
		}
	}

	private Socket connect(InetSocketAddress location) throws InterruptedException{
		boolean connected = false;
		int tryCounter = 0;

		while(!connected && tryCounter < 600){
			try{
				Socket s = new Socket(location.getHostName(), location.getPort());
				s.setKeepAlive(true);
				return s;
			}catch (IOException e) {
				Thread.sleep(1000);
				tryCounter++;
			}
		}

		System.out.println("Failed to connect to " + location.toString());
		return null;
	}

	private void newOrder(int clientId, int clientOrderId, NewOrderSingle nos) throws IOException{
		orders.put(id, new Order(clientId, clientOrderId, nos.getInstrument(), nos.getSize()));			//TODO::Replaced nos.instrument with getters b/c now private
		//send a message to the client with 39=A; //OrdStatus is Fix 39, 'A' is 'Pending New'
		ObjectOutputStream os = new ObjectOutputStream(clients[clientId].getOutputStream());
		//newOrderSingle acknowledgement
		//ClOrdId is 11=
		os.writeObject("11="+clientOrderId+";35=A;39=A;");
		os.flush();

		//send the new order to the trading screen
		//don't do anything else with the order, as we are simulating high touch orders and so need to wait for the trader to accept the order
		sendOrderToTrader(id, orders.get(id), TradeScreen.api.newOrder);

		id++;
	}

	private void sendOrderToTrader(int id, Order o, Object method) throws IOException{
		ObjectOutputStream ost = new ObjectOutputStream(trader.getOutputStream());
		ost.writeObject(method);
		ost.writeInt(id);
		ost.writeObject(o);
		ost.flush();
	}

	private void acceptOrder(int id) throws IOException{
		Order o = orders.get(id);
		if(o.getOrderStatus() != 'A'){ //Pending New
			System.out.println("error accepting order that has already been accepted");
			return;
		}

		o.setOrderStatus('0'); //New
		ObjectOutputStream os = new ObjectOutputStream(clients[o.getClientID()].getOutputStream());
		//newOrderSingle acknowledgement
		//ClOrdId is 11=
		os.writeObject("11="+o.getClientOrderID()+";35=A;39=0");
		os.flush();

		price(id,o);
	}

	private void sliceOrder(int id,int sliceSize) throws IOException{
		Order o = orders.get(id);
		//slice the order. We have to check this is a valid size.
		//Order has a list of slices, and a list of fills, each slice is a childorder and each fill is associated with either a child order or the original order
		if (sliceSize > o.sizeRemaining() - o.sliceSizes()){
			System.out.println("error sliceSize is bigger than remaining size to be filled on the order");
			return;
		}

		int sliceId = o.newSlice(sliceSize);
		Order slice = o.getSlices().get(sliceId);
		internalCross(id, slice);
		//TODO::Changed this from an int to a long to meet variable specs
		long sizeRemaining = o.getSlices().get(sliceId).sizeRemaining();
		if(sizeRemaining > 0){
			routeOrder(id, sliceId, sizeRemaining, slice);
		}
	}
	private void internalCross(int id, Order o) throws IOException{
		for (Map.Entry<Integer, Order> entry : orders.entrySet()){
			if (entry.getKey() == id)		//TODO::Got rid of unboxing to int since it's already an int
				continue;

			Order matchingOrder = entry.getValue();
			if(!(matchingOrder.getInstrument().equals(o.getInstrument()) && matchingOrder.getInitialMarketPrice() == o.getInitialMarketPrice()))
				continue;

			//TODO add support here and in Order for limit orders
			long sizeBefore = o.sizeRemaining();				//TODO::Changed type from int to long to match var spec
			o.cross(matchingOrder);
			if(sizeBefore != o.sizeRemaining()){
				sendOrderToTrader(id, o, TradeScreen.api.cross);
			}
		}
	}

	private void cancelOrder(){
		
	}

	private void newFill(int id, int sliceId, int size, double price) throws IOException{
		Order o = orders.get(id);
		o.getSlices().get(sliceId).createFill(size, price);
		if(o.sizeRemaining() == 0){
			Database.write(o);
		}
		sendOrderToTrader(id, o, TradeScreen.api.fill);
	}

	//TODO::this might mean we are selling the order
	//TODO::Changed size from int to long
	private void routeOrder(int id, int sliceId, long size, Order order) throws IOException{
		for (Socket r : orderRouters){
			ObjectOutputStream os = new ObjectOutputStream(r.getOutputStream());
			os.writeObject(Router.api.priceAtSize);
			os.writeInt(id);
			os.writeInt(sliceId);
			os.writeObject(order.getInstrument());		//TODO::Changed from o.instrument to o.getInstrument
			os.writeLong(order.sizeRemaining());		//TODO::Changed from os.writeInt to os.writeLong
			os.flush();
		}
		//need to wait for these prices to come back before routing
		order.setBestPrices(new double[orderRouters.length]);			//TODO::Changed from order.bestPrices = new double[orderRouters.length to method call
		order.setBestPriceCount(0);										//TODO::Changed from assignment to method call
	}

	//TODO::this is looking for the best price, maybe rename this to lookForBestPrice()
	private void reallyRouteOrder(int sliceId,Order o) throws IOException{
		//TODO this assumes we are buying rather than selling
		int minIndex = 0;
		double min = o.getBestPrice(0);		//TODO::added a way to get elements via method that takes in an index
		for (int i = 1; i < o.getBestPriceLength(); i++){
			if (min > o.getBestPrice(i)) {
				minIndex = i;
				min = o.getBestPrice(i);
			}
		}

		ObjectOutputStream os = new ObjectOutputStream(orderRouters[minIndex].getOutputStream());
		os.writeObject(Router.api.routeOrder);
		os.writeInt(o.getID());					//TODO::Changed to getter method instead of o.id
		os.writeInt(sliceId);
		os.writeLong(o.sizeRemaining());		//TODO::changed from os.writeInt to o.writeLong to match var spec
		os.writeObject(o.getInstrument());		//TODO::changed from o.instrument to o.getInstrument
		os.flush();
	}

	private void sendCancel(Order order, Router orderRouter){
		//orderRouter.sendCancel(order);
		//order.orderRouter.writeObject(order);
	}

	private void price(int id,Order o) throws IOException{
		liveMarketData.setPrice(o);
		sendOrderToTrader(id, o, TradeScreen.api.price);
	}
}