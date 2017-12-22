package OrderRouter;

import java.io.IOException;

import OrderManager.Order;
import Ref.Instrument;

public interface Router {
	enum api{routeOrder, sendCancel, priceAtSize,killProgram};

	void routeOrder(int id ,int sliceId, long size, Instrument i, double price) throws IOException, InterruptedException;
	void sendCancel(int id ,int sliceId, int size, Instrument i);
	void priceAtSize(int id, int sliceId, Instrument i, int size) throws IOException;
	
}
