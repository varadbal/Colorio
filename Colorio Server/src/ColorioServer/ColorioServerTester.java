package ColorioServer;

import java.io.*;
import java.net.*;

public class ColorioServerTester implements Runnable{
    private Thread t;
    private String threadName;

    ColorioServerTester(String name){
        threadName = name;
        System.out.println("Creating thread " + threadName);
    }


    public void run(){
        System.out.println("Running " + threadName);

        try {
            DatagramSocket clientSocket = new DatagramSocket(49156);
            InetAddress IPAddress = InetAddress.getByName("localhost");
            int i = 0;
            while(true) {

                byte[] sendData = new byte[1024];
                String sentence = "Hello World!" + i++;
                sendData = sentence.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 49155);
                clientSocket.send(sendPacket);


            }
        }catch(IOException e){
            e.printStackTrace();
        }


        System.out.println("Exiting thread " + threadName);
    }

    public void start(){
        System.out.println("Starting thread " + threadName);
        if(t == null){
            t = new Thread(this, threadName);
            t.start();
        }
    }

}
