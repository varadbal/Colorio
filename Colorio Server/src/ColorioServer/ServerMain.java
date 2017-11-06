package ColorioServer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerMain {
    public static void main(String[] args) {

        BlockingQueue<byte[]> toSend = new LinkedBlockingQueue<byte[]>();
        ConcurrentMap<Integer, Client> clients = new ConcurrentHashMap<>();

        ColorioServerIn s1 = new ColorioServerIn( "ServerIn", clients);
        s1.start();
        //ColorioServer.ColorioServerOut s2 = new ColorioServer.ColorioServerOut( "Thread-2", toSend);
        //s2.start();


        /*START TESTER*/
        try {
            Thread.sleep(1000);
        }catch(Exception e){
            e.printStackTrace();
        }
        //ColorioServer.ColorioServerTester t1 = new ColorioServer.ColorioServerTester( "ServerTester");
        //t1.start();

    }
}
