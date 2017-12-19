package TradeScreen;

import java.io.IOException;

import OrderManager.Order;

public interface TradeScreen {
	enum api{newOrder, price, fill, cross};

	void newOrder(int id,Order order) throws IOException, InterruptedException;
	void acceptOrder(int id) throws IOException;

	//TODO::changed this from int, int to int, long to match var spec in Order.java which stated that size should be a long instead of int
	void sliceOrder(int id,long sliceSize) throws IOException;
	void price(int id,Order o) throws InterruptedException, IOException;
}
