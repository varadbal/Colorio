import java.io.*;
import java.net.*;

public class ColorioServer implements Runnable {
    private Thread t;
    private String threadName;

    ColorioServer(String name){
        threadName = name;
        System.out.println("Creating thread " + threadName);
    }

    public void run(){
        System.out.println("Running " + threadName);

        ServerSocket ss;
        try {
            ss = new ServerSocket(60010);

            Socket s = ss.accept();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(s.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
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
