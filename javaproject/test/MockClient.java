import java.io.IOException;

class MockClient extends Thread{

    private int port;

    MockClient(String name,int port){
        this.port=port;
        this.setName(name);
        //todo:!!!!!print port and name passed to mock client
        System.out.printf("            MockClient.java -> MockClient(...)....CLIENT CREATED WITH MockClient:--PORT:%s--NAME:%s--______\n",port,name);
    }

    public void run(){
        try {
            //todo:!!!!!print before create a sample client.
            System.out.printf("            MockClient.java -> run()....MockClient Runs and Creates A new SampleClient sending:--PORT:%s--______\n",port);

            SampleClient client=new SampleClient(port);

            //todo:!!!!!this is purely to select what thread/client will run inside the if statement.
            //todo:!!!!! I think it is unnecessary in out case. for testing.
            //if(port==2000){}
            //TODO why does this take an arg? outside of our testing environment, par0 would be an object described in
            //todo:!!!!! SampleClient. It would be of type nos.
            //todo:!!!!! we have an int returned. Don't see the reason for that. since the omX (om1 or om2) is a function of Order Manager, not Client
            //todo:!!!!! and they are 2 separate and independent threads.
            //int id = client.sendOrder(null);
            //todo:!!!!! client sends a new order

            System.out.printf("            MockClient.java -> run()....MockClient -client.sendOrder- is sending an order with null as the object______\n");
            client.sendOrder(null);

            //todo:!!!!! this is where the client is listening for the order manager to update him with the latest news. it has an infinite loop, listening
            System.out.printf("            MockClient.java -> run()....MockClient -client.messageHandler- is starting to listen for messages: ______\n");
            client.messageHandler();



            //TODO client.sendCancel(id); -- we will do this in another test. with a different client
        } catch (IOException e) {
            // TODO Auto-generated catch block - this looks fine like this
            e.printStackTrace();
        }
    }
}
