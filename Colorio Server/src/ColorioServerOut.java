import java.util.concurrent.BlockingQueue;

public class ColorioServerOut implements Runnable{
    private Thread t;
    private String threadName;
    private BlockingQueue<byte[]> toSend;
    ColorioServerOut(String name, BlockingQueue<byte[]> send){
        threadName = name;
        toSend = send;
        System.out.println("Creating thread " + threadName);
    }

    public void initServerOut(){

    }

    public void run() {

    }

    public void start(){
        System.out.println("Starting thread " + threadName);
        initServerOut();
        if(t == null){
            t = new Thread(this, threadName);
            t.start();
        }
    }


}
