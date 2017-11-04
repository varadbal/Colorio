package ColorioServer;

import java.util.HashMap;


public class ColorioGameMap implements Runnable {
    private Thread t;
    private String threadName;

    HashMap<Integer, Player> players;




    ColorioGameMap(String name){
        threadName = name;
        System.out.println("Creating thread " + threadName);
    }

    public void run(){




    }



    public void start(){
        System.out.println("Starting thread " + threadName);
        if(t == null){
            t = new Thread(this, threadName);
            t.start();
        }
    }
}
