import java.io.IOException;
import java.net.InetSocketAddress;

import LiveMarketData.LiveMarketData;

public class Main{
	public static void main(String[] args) throws IOException{
		System.out.println("TEST: this program tests ordermanager");

		//start sample clients
<<<<<<< HEAD
		//we have have only oneclient
		System.out.printf("            Main.java.... ONE CLIENT WILL BE CREATED \n");
		(new MockClient("Client 1",2000)).start();

		(new MockClient("Manager",2002)).start();

=======

		//todo:!!!!have only oneclient
		System.out.printf("            Main.java.... ONE CLIENT WILL BE CREATED \n");
		(new MockClient("Client 1",2000)).start();

		//(new MockClient("Client 2",2001)).start();
>>>>>>> origin/master
		
		//start sample routers
		(new SampleRouter("Router LSE",2010)).start();
		(new SampleRouter("Router BATE",2011)).start();
	
		(new Trader("Trader James",2020)).start();
		//start order manager
		//todo:!!!!!pass a list of only one client
<<<<<<< HEAD
		InetSocketAddress[] clients = {new InetSocketAddress("localhost",2000), new InetSocketAddress("localhost",2002)};
=======
		InetSocketAddress[] clients = {new InetSocketAddress("localhost",2000)};//, new InetSocketAddress("localhost",2001)};
>>>>>>> origin/master
		InetSocketAddress[] routers = {new InetSocketAddress("localhost",2010), new InetSocketAddress("localhost",2011)};
		InetSocketAddress trader = new InetSocketAddress("localhost",2020);
		LiveMarketData liveMarketData = new SampleLiveMarketData();
		(new MockOM("Order Manager",routers,clients,trader,liveMarketData)).start();
	}
}