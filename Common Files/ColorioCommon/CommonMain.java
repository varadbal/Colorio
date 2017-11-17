package ColorioCommon;

import ColorioClient.StartDialog;
import ColorioServer.Client;
import ColorioServer.GameLogic;
import ColorioServer.OutPacket;
import ColorioServer.Server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Main class which executes both server and client
 */
public class CommonMain {
    public static void main(String[] args) {
        /**
         * Start server
         */
        ConcurrentMap<Integer, Client> clients = new ConcurrentHashMap<>();
        BlockingQueue<OutPacket> toSend = new LinkedBlockingQueue<>();
        BlockingQueue<KeyInput> toHandle = new LinkedBlockingQueue<>();

        Server serv = new Server(toSend, clients, toHandle);
        GameLogic gl = new GameLogic(clients, toSend, toHandle);

        serv.start();
        gl.start();

        /**
         * Start client
         */
        StartDialog startDialog = new StartDialog();
        startDialog.setVisible(true);
    }
}
