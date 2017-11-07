package ColorioServer;

import ColorioCommon.Handshake;
import ColorioCommon.KeyEvent;
import com.sun.istack.internal.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * ColorIo server-end communication class
 * @Author Balazs Varady
 */

public class Server {
    /**
     * Instance variables
     */
    private BlockingQueue<OutPacket> toSend = null;
    ConcurrentMap<Integer, Client> clients = null;
    private ServerIn sIn = null;
    private ServerOut sOut = null;
    private boolean isRunning = false;

    private int port = 49155;
    private DatagramSocket socket = null;

    /**
     * Constructor, creating the threads, setting up the communication between threads
     * @param toSend A queue for communication with the ServerOut
     * @param clients A map of the currently active clients
     * @param toHandle A queue for communication with the Game Logic
     */
    public Server(@NotNull BlockingQueue<OutPacket> toSend,
                  @NotNull ConcurrentMap<Integer, Client> clients, @NotNull ConcurrentLinkedQueue<KeyEvent> toHandle) {
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
     * Should not be instantiated/manipulated from outside
     */
    private class ServerIn implements Runnable{
        /**
         * Thread variables
         */
        private Thread t;
        private String threadName;
        /**
         * Instance variables
         */
        private ConcurrentLinkedQueue<KeyEvent> toHandle;

        /**
         * Constructor
         * @param threadName The name of the thread
         * @param toHandle The queue of the KeyEvents to handle
         */
        private ServerIn(String threadName, @NotNull ConcurrentLinkedQueue<KeyEvent> toHandle) {
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
         *     Decide its type and act accordingly (do handshake or add key event to the handle queue)
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

                Object o = null;
                try {
                    o = new ObjectInputStream(new ByteArrayInputStream(receivePacket.getData())).readObject();
                }catch (IOException e){
                    e.printStackTrace();
                }catch (ClassNotFoundException e){
                    e.printStackTrace();
                }

                if(o instanceof Handshake){
                    Handshake h = (Handshake) o;
                    doHandshake(h, receivePacket.getAddress());

                } else if(o instanceof KeyEvent){
                    KeyEvent k = (KeyEvent) o;
                    clients.get(k.getId()).setAddr(receivePacket.getAddress()); //Update IP - might have changed
                    toHandle.add(k);
                }
            }
        }

        /**
         * Method for handling an incoming Handshake-Object, always doing an appropriate step in a handshake
         *     If first shake, save client and give it an Id
         *     If second shake, check data and set playing, (update IP - might have changed)
         * @param h The incoming Handshake-Object
         * @param ip The source-IP of the Handshake-Object
         */
        private void doHandshake(@NotNull Handshake h, @NotNull InetAddress ip){
            if(h.getName() != null && h.getId() == 0){ //First shake
                int nextId = Collections.max(Server.this.clients.keySet()) + 1;             //TODO more elegant nextId
                Server.this.clients.put(nextId, new Client(h.getName(), ip));
                OutPacket op = new OutPacket(nextId, new Handshake(h.getName(), nextId));
                //DatagramPacket dp = new Handshake(h.getName(), h.getId()).toDatagramPacket(ip, port);
                try {
                    toSend.put(op);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

            }else if(h.getName() != null &&h.getId() != 0){ //Second shake
                if(h.getName().equals(clients.get(h.getId()).getName())){ //Data Correct
                    Server.this.clients.get(h.getId()).setAddr(ip);
                    Server.this.clients.get(h.getId()).setPlaying(true);
                }
            }
        }
    }

    /**
     * Thread for sending the Datagram Packets from toSend
     * Should not be instantiated/manipulated from outside
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
