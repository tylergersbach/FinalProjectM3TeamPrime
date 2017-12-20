import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import OrderClient.Client;
import OrderClient.NewOrderSingle;
import OrderManager.Order;
import Ref.Instrument;
import Ref.Ric;

import static java.lang.Thread.sleep;

public class SampleClient implements Client{
	private static final Random RANDOM_NUM_GENERATOR = new Random();
	private static final Instrument[] INSTRUMENTS = {new Instrument(new Ric("VOD.L")), new Instrument(new Ric("BP.L")), new Instrument(new Ric("BT.L"))};
	private static final Map OUT_QUEUE = new HashMap(); //queue for outgoing orders		//TODO::Changed from HashMap OUT_QUE to Map OUT_QUE
	private int id = 0; //message id number
	private Socket orderManagerConnection; //connection to order manager
			
	public SampleClient(int port) throws IOException{
		//OM will connect to us
		//todo:!!!!!Sample client created and updates order manager with passed port
		System.out.printf("            SampleClient.java -> SampleClient......SAMPLE CLIENT CREATED AND CREATE AN orderManagerConnection with passed port number:--PORT:%s______\n",port);
		orderManagerConnection = new ServerSocket(port).accept();
	}

	@Override
	public int sendOrder(Object par0)throws IOException{
		String fix_message = "35=D";
		int size = RANDOM_NUM_GENERATOR.nextInt(5000);
		int instrumentID = RANDOM_NUM_GENERATOR.nextInt(3);

		//todo: Now this implementation of instrument is wrong.
		//todo: You have and instrument id  and then you chose from instruments, a random instrument rather than choosing based on instrument id.
		//Instrument instrument = INSTRUMENTS[RANDOM_NUM_GENERATOR.nextInt(INSTRUMENTS.length)];


		//todo: I think this is the right way to do this:
		Instrument instrument = INSTRUMENTS[instrumentID];

		//todo:!!!!!!nos is NewOrderSingle and accepts (size, price, instrument) -- it was given as (size,instrumentID, instrument)
		NewOrderSingle nos = new NewOrderSingle(size, instrumentID, instrument);
		show("sendOrder: id=" + id + " size=" + size + " instrument=" + INSTRUMENTS[instrumentID].toString());
		OUT_QUEUE.put(id, nos);
		//todo:!!!!!Here is where nos gets: size, instrumentID and instrument
		System.out.printf("            SampleClient.java -> sendOrder......SAMPLE CLIENT creates nos with:--size:%s--instrumentID:%s--instrument:%s--_______\n",size,instrumentID,instrument.toString());



		if(orderManagerConnection.isConnected()){

			ObjectOutputStream os = new ObjectOutputStream(orderManagerConnection.getOutputStream());

			//todo:!!!!!Here is where the client sends over the socket the data to the OrderManager
			System.out.printf("            SampleClient.java -> sendOrder......SAMPLE CLIENT sends to the socket:\n");
			System.out.printf("					--FIX_MESSAGE:%s--id:%s--nos(size,instrID,instr):(%s,%s,%s)--_______\n",fix_message,id,nos.getSize(),nos.getInstrument().toString(),nos.getInstrument().toString());

			//TODO::I think this is par0????? --- Yes, par0 would be the whole packet of:{35=D,id,nos}
			//os.writeObject("newOrderSingle");
			os.writeObject(fix_message); //fix message is 35=D meaning "new order single" in FIX code
			os.writeInt(id);
			os.writeObject(nos);
			os.flush();
		}

		return id++;
	}

	@Override
	public void sendCancel(int orderId){
		show("sendCancel: id=" + orderId);
		if(orderManagerConnection.isConnected()){
			//OMconnection.sendMessage("cancel",idToCancel);
		}
	}

	@Override
	public void partialFill(Order order ){
		show(""+order);
	}

	@Override
	public void fullyFilled(Order order) {
		show("Order fully filled: " + order.getClientOrderID());
		OUT_QUEUE.remove(order.getClientOrderID());
	}

	@Override
	public void cancelled(Order order){show("" + order);
		OUT_QUEUE.remove(order.getClientOrderID());
	}

	//We added pendng new for 39-A case

	public void pendingNew(Order order){
		show("--Order is pending...");
	}

	public void newOrderCreated(Order order){
		show("--New order accepted");
	}

	enum methods{newOrderSingleAcknowledgement,dontKnow};
	@Override
	public void messageHandler(){
		
		ObjectInputStream is;

		try {

			while(true){
				sleep(1);
				//Thread.sleep(1); //this throws an exception!!
				while(0< orderManagerConnection.getInputStream().available()){

					is = new ObjectInputStream(orderManagerConnection.getInputStream());

					String fix=(String)is.readObject();
					Order order = (Order) is.readObject();

					System.out.println(Thread.currentThread().getName()+" received fix message: "+fix);
					String[] fixTags=fix.split(";");
					int OrderId=-1;
					char MsgType='x';
					methods whatToDo=methods.dontKnow;
					//String[][] fixTagsValues=new String[fixTags.length][2];

					for(int i=0;i<fixTags.length;i++){
						String[] tag_value=fixTags[i].split("=");
						switch(tag_value[0]){
							case"11":OrderId=Integer.parseInt(tag_value[1]);break;
							case"39":MsgType=tag_value[1].charAt(0);break;
						}
					}

					switch(MsgType){
						case '4':cancelled(order);break;
						case '1':partialFill(order);break;
						case '2':fullyFilled(order);break;
						case 'A':pendingNew(order);break;
						case '0':newOrderCreated(order);break;
						default:
					}
				}
			}
		} catch (IOException|ClassNotFoundException|InterruptedException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	private static void show(String out){
		System.err.println(Thread.currentThread().getName()+":"+out);
	}

/*listen for connections
once order manager has connected, then send and cancel orders randomly
listen for messages from order manager and print them to stdout.*/

}