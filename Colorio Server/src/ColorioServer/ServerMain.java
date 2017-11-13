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
        BlockingQueue<KeyInput> toHandle = new LinkedBlockingQueue<>();

        Server serv = new Server(toSend, clients, toHandle);
        GameLogic gl = new GameLogic(clients, toSend, toHandle);

        serv.start();
        gl.start();


    }
}
