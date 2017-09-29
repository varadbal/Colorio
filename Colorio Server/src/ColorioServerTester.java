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
            Socket s = new Socket("localhost", 60010);
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(s.getOutputStream()));

            while (true) {
                out.write("Hello World!");
                out.newLine();
                out.flush();

                Thread.sleep(200);
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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
