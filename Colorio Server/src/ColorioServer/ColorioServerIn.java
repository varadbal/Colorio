package ColorioServer;

import ColorioCommon.Handshake;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;

public class ColorioServerIn implements Runnable {
    private Thread t;
    private String threadName;
    //private BlockingQueue<byte[]> toSend;
    DatagramSocket serverSocket;
    int port = 49155;

    ConcurrentMap<Integer, Client> clients;

    ColorioServerIn(String name, ConcurrentMap<Integer, Client> clients){
        threadName = name;
        this.clients = clients;
        System.out.println("Creating thread " + threadName);
    }

    private void initServerIn(){
        try {
            serverSocket = new DatagramSocket(port);
        }catch(SocketException e){
            e.printStackTrace();
        }
    }

    public void run() {
        System.out.println("Running " + threadName);
        /*GET A SERIALIZED OBJECT VIA UDP*/
        try {
            while(true){
                DatagramPacket receivePacket = new DatagramPacket(new byte[2048], 2048);
                serverSocket.receive(receivePacket);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(receivePacket.getData()));
                Object o = ois.readObject();
                if(o instanceof Handshake){
                    //BEST CASE SCENARIO
                    Handshake h = (Handshake)o;
                    if(h.getName() != null && h.getId() == 0){  //First Shake
                        //Save ColorioServer.Client
                        int nextId = Collections.max(clients.keySet()) + 1;
                        clients.put(nextId, new Client(h.getName(), receivePacket.getAddress()));
                        //Send Response
                        DatagramPacket dp = new Handshake(h.getName(), h.getId()).toDatagramPacket(receivePacket.getAddress(), port);
                        serverSocket.send(dp);
                    }else if(h.getName() != null && h.getId() != 0){    //Second Shake
                        //Check ColorioServer.Client
                        if(h.getName().equals(clients.get(h.getId()).getName())) {
                            //Set Ready to play
                            clients.get(h.getId()).setReadyToPlay(true);
                        }
                    }
                }else if(false/*NOT HANDSHAKE*/){

                }

            }
        }catch(SocketException e){

        }catch(IOException e){

        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }
        System.out.println("Exiting thread " + threadName);
    }

    public void start(){
        System.out.println("Starting thread " + threadName);
        initServerIn();
        if(t == null){
            t = new Thread(this, threadName);
            t.start();
        }
    }
}
