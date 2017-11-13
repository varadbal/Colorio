package ColorioServer;

import ColorioCommon.*;
import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import static ColorioCommon.Constants.portNumber;

/**
 * ColorIo server-end communication class
 * @Author Balazs Varady
 */

public class Server {
    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    /**
     * Instance variables
     */
    private BlockingQueue<OutPacket> toSend = null;
    private ConcurrentMap<Integer, Client> clients = null;
    private ServerIn sIn = null;
    private ServerOut sOut = null;
    private boolean isRunning = false;

    private int port = portNumber;
    private DatagramSocket socket = null;

    /**
     * Constructor, creating the threads, setting up the communication between threads
     * @param toSend A queue for communication with the ServerOut
     * @param clients A map of the clients (id, Client)
     * @param toHandle A queue for communication with the Game Logic
     */
    public Server(@NotNull BlockingQueue<OutPacket> toSend,
                  @NotNull ConcurrentMap<Integer, Client> clients, @NotNull BlockingQueue<KeyInput> toHandle) {
        this.toSend = toSend;
        this.clients = clients;
        sIn = new ServerIn("ServerIn-1", toHandle);
        sOut = new ServerOut("ServerOut-1");
    }

    /**
     * Setting up the socket
     */
    private void initializeServer(){
        try {
            socket = new DatagramSocket(port);
        }catch (SocketException e){
            e.printStackTrace();
        }
    }

    /**
     * Starting the in- and out-threads, (if they exist)
     * TODO complete
     */
    public void start(){
        initializeServer();
        isRunning = true;

        //The part after this should come last in this method
        if(sIn != null){
            sIn.start();
        }else{
            /**
             * TODO throw exception
             */
        }
        if(sOut != null){
            sOut.start();
        }else{
            /**
             * TODO throw exception
             */
        }
    }


    /**
     * Thread for handling the incoming messages
     * Should not be manipulated from outside
     */
    private class ServerIn implements Runnable{
        //region Variables
        /**
         * Thread variables
         */
        private Thread t;
        private String threadName;
        /**
         * Instance variables
         */
        private BlockingQueue<KeyInput> toHandle;
        //endregion

        /**
         * Constructor
         * @param threadName The name of the thread
         * @param toHandle The queue of the KeyEvents to handle
         */
        private ServerIn(String threadName, @NotNull BlockingQueue<KeyInput> toHandle) {
            this.threadName = threadName;
            this.toHandle = toHandle;
        }

        /**
         * Creates a thread and starts it, (if it does not exist yet)
         */
        private void start(){
            System.out.println("Starting thread " + threadName);
            if(t == null){
                t = new Thread(this, threadName);
                t.start();
            }
        }

        /**
         * Thread run method:
         *     Create a DatagramPacket: receivePacket
         *     Receive data from the socket
         *     Deserialize the data
         *     Decide its type and act accordingly (do handshake or add KeyInput to the handle queue)
         */
        @Override
        public void run() {
            System.out.println("Running thread " + threadName);
            while(Server.this.isRunning){
                DatagramPacket receivePacket = new DatagramPacket(new byte[2048], 2048);

                try {
                    socket.receive(receivePacket);
                }catch (IOException e){
                    e.printStackTrace();
                }

                UDPSerializable o = null;
                o.getFromDatagramPacket(receivePacket);

                if(o instanceof Handshake){         //Either connecting first shake or disconnecting
                    Handshake h = (Handshake) o;
                    gotHandshake(h, receivePacket.getAddress());

                } else if(o instanceof KeyInput){   //At this point it does not matter what kind, just pass to GameLogic
                    KeyEvent k = (KeyEvent) o;
                    clients.get(k.getPlayerId()).setAddr(receivePacket.getAddress()); //Update IP - might have changed
                    toHandle.add(k);
                }

            }
        }

        /**
         * Method for handling an incoming Handshake-Object, which, according to the communication protocol,
         * means either connecting or disconnecting client.
         * @param h The incoming Handshake-Object
         * @param ip The source-IP of the Handshake-Object
         */
        private void gotHandshake(@NotNull Handshake h, @NotNull InetAddress ip){
            if(h.getName() != null && h.getId() == 0){              //Connecting client
                int nextId = Collections.max(Server.this.clients.keySet()) + 1;             //TODO more elegant nextId

                Server.this.clients.put(nextId, new Client(h.getName(), ip));
                OutPacket op = new OutPacket(nextId, new Handshake(h.getName(), nextId));
                try {
                    toSend.put(op);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

            }else if(h.getName() == null && h.getId() != 0) {         //Disconnecting Client
                OutPacket op = new OutPacket(h.getId(), new Handshake(null, 0));
                Server.this.clients.get(h.getId()).setPlaying(false);
                try {
                    toSend.put(op);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Thread for sending the OutPackets (from toSend) as DatagramPackets
     * Should not be manipulated from outside
     */
    private class ServerOut implements Runnable{
        /**
         * Thread variables
         */
        private Thread t;
        private String threadName;

        /**
         * Instance variables
         */

        /**
         * Constructor
         * @param threadName The name of the thread
         */
        public ServerOut(String threadName) {
            this.threadName = threadName;
        }

        /**
         * Creates a thread and starts it, (if it does not exist yet)
         */
        private void start(){
            System.out.println("Starting thread " + threadName);
            if(t == null){
                t = new Thread(this, threadName);
                t.start();
            }
        }

        /**
         * Thread run method - sends the elements of toSend
         *     Takes an element from toSend, converts its packet to DatagramPacket whose IP id the IP-Address of its client
         */
        @Override
        public void run() {
            System.out.println("Running thread " + threadName);
            while(Server.this.isRunning){
                try {
                    OutPacket ts = toSend.take();
                    socket.send(ts.getPacket().toDatagramPacket(clients.get(ts.getTargetId()).getAddr(), port));
                }catch (InterruptedException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

}
