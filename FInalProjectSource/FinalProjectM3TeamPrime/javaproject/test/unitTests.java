import OrderClient.NewOrderSingle;
import OrderManager.Order;
import Ref.Instrument;
import Ref.Ric;
import TradeScreen.TradeScreen;
import org.junit.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;


import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;

public class unitTests {

    @Test
    public void testPortNumber() {
        Trader trader =  new Trader("Trader James",2020);
        trader.start();

        int actual = trader.getPort();

        assertEquals(2020 ,actual );
    }

    @Test
    public void testNewOrder() throws IOException, InterruptedException {
        Trader trader1 =  new Trader("Trader James",2020);
        trader1.start();

        InetSocketAddress connection = new InetSocketAddress("localhost",2020);

        Socket trader = connect(connection);

        Ric ric = new Ric("VOD.L");
        Instrument instrument = new Instrument(ric);
        NewOrderSingle nos = new NewOrderSingle(333,222, instrument);

        Order order =  new Order(0, 0, nos.getInstrument(), nos.getSize());

        ObjectOutputStream ost = new ObjectOutputStream(trader.getOutputStream());
        ost.writeObject(TradeScreen.api.newOrder);
        ost.writeInt(0);
        ost.writeObject(order);
        ost.flush();
        sleep(4000);
        HashMap<Integer,Order> actual = trader1.getOrders();
        HashMap<Integer,Order> expected = new HashMap<>();
        expected.put(0,order);


        assertEquals( expected.get(0).getInstrument().toString() , actual.get(0).getInstrument().toString() );
        assertEquals( expected.get(0).getID() , actual.get(0).getID() );
        assertEquals( expected.get(0).getClientID() , actual.get(0).getClientID() );
        assertEquals( expected.get(0).getSize() , actual.get(0).getSize() );


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
                sleep(1000);
                tryCounter++;
            }
        }

        System.out.println("Failed to connect to " + location.toString());
        return null;
    }

}
