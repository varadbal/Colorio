package ColorioServer;


import ColorioCommon.KeyInput;

import java.util.concurrent.*;

/**
 * @author Balazs Varady
 *
 */
public class ServerMain {
    public static void main(String[] args) {
        ConcurrentMap<Integer, Client> clients = new ConcurrentHashMap<>();
        BlockingQueue<OutPacket> toSend = new LinkedBlockingQueue<>();
        ConcurrentLinkedQueue<KeyInput> toHandle = new ConcurrentLinkedQueue<>();

        Server serv = new Server(toSend, clients, toHandle);
        GameLogic gl = new GameLogic("GL-1", clients, toSend, toHandle);

        serv.start();
        gl.start();


    }
}
