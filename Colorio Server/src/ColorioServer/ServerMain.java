package ColorioServer;


import ColorioCommon.KeyEvent;
import java.net.DatagramPacket;
import java.util.concurrent.*;

/**
 * TODO delete every class from ColorioServer, except: Client, Server, GameLogic, ServerMain
 *
 */
public class ServerMain {
    public static void main(String[] args) {
        ConcurrentMap<Integer, Client> clients = new ConcurrentHashMap<>();
        BlockingQueue<DatagramPacket> toSend = new LinkedBlockingQueue<>();
        ConcurrentLinkedQueue<KeyEvent> toHandle = new ConcurrentLinkedQueue<>();

        Server serv = new Server(toSend, clients, toHandle);
        GameLogic gl = new GameLogic("GL-1", clients, toSend, toHandle);

        serv.start();
        gl.start();


    }
}
