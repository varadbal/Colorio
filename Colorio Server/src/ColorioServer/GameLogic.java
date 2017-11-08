package ColorioServer;

import ColorioCommon.Centroid;
import ColorioCommon.GameStatus;
import ColorioCommon.KeyEvent;
import com.sun.istack.internal.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 * ColorIo Server Game Logic class
 * @Author Balazs Varady
 */
public class GameLogic implements Runnable {
    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(GameLogic.class.getName());
    /**
     * Thread variables
     */
    private Thread t;
    private String threadName;
    /**
     * Instance variables regarding the Class
     */
    private ConcurrentMap<Integer, Client> clients = null;
    private BlockingQueue<OutPacket> toSend = null;
    private ConcurrentLinkedQueue<KeyEvent> toHandle = null;
    private boolean isRunning = false;
    /**
     * Game constants
     */
    private final double defaultWeight = 10.0;
    private final int speed = 10;

    /**
     * Constructor, initializing the variables for thread and inter-thread communications
     * @param threadName Name of the thread
     * @param clients A map of the currently active clients
     * @param toSend A queue for communication with ServerOut
     * @param toHandle A queue for communication with ServerIn
     */
    public GameLogic(String threadName, @NotNull ConcurrentMap<Integer, Client> clients,
                     @NotNull BlockingQueue<OutPacket> toSend, @NotNull ConcurrentLinkedQueue<KeyEvent> toHandle) {
        this.threadName = threadName;
        this.clients = clients;
        this.toSend = toSend;
        this.toHandle = toHandle;
    }

    /**
     * Creates a thread and starts it, (if it does not exist yet)
     */
    public void start(){
        System.out.println("Starting thread " + threadName);
        isRunning = true;

        //The part after this should come last in this method
        if(t == null){
            t = new Thread(this, threadName);
            t.start();
        }
    }

    /**
     * Thread run method
     *     Call checkPlayers
     *     Call sendStatus in ~regular time intervals
     *     Call handleInput
     */
    @Override
    public void run() {
        System.out.println("Running thread " + threadName);

        long lastSent = 0L;
        long now;
        KeyEvent k = null;

        while(isRunning){
            checkPlayers();

            if((now = Instant.now().toEpochMilli()) - lastSent > 10){
                lastSent = now;
                sendStatus();
            }

            if((k = toHandle.poll()) != null){
                handleEvent(k);
                k = null;
            }
        }
    }

    /**
     * Setting the yet unset variables, checking if everything is valid (on the game-logic layer), calculating in-game 'events'
     *     Color, Weight, X, Y must be set to be able to play
     *     TODO complete('in-game events', improbable cases) and improve
     */
    private void checkPlayers(){
        Collection<Client> clis = clients.values();
        for(Client c : clis){
            if(c.isPlaying()){  //If it should have everything defined
                if(c.getCent() == null){ //Player's object not yet defined

                    Random rand = new Random();         //TODO more elegant
                    float r = rand.nextFloat();
                    float g = rand.nextFloat();
                    float b = rand.nextFloat();
                    c.setCent(new Centroid(0.0, 0.0, defaultWeight, new Color(r, g, b))); //TODO more elegant

                }else if(false){
                    //TODO implement other bad cases (that are ATM theoretically possible but highly improbable)
                }

            }
        }

    }

    /**
     * Preparing and sending a GameStatus to every client
     *     Gets the entry set of 'clients'
     *     For each entry (client)
     *        Iterate through the set and make a GameStatus with first (0.) element as the client's own
     *        Send this GameStatus to the client
     * @implSpec O(n square), where n is the number of clients
     */
    private void sendStatus(){
        //LOGGER.info("Sending");
        GameStatus currentStatus;
        Set<Map.Entry<Integer, Client>> es = clients.entrySet();

        for(Map.Entry<Integer, Client> i : es){
            currentStatus = new GameStatus();
            for(Map.Entry<Integer, Client> j : es){
                if(j.getKey() == i.getKey()){
                    currentStatus.addCentroid(0, j.getValue().getCent());
                }else{
                    currentStatus.addCentroid(j.getValue().getCent());
                }
            }
            try{
                toSend.put(new OutPacket(i.getKey(), currentStatus));
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Handling a single (incoming) KeyEvent
     * @param k The KeyEvent to handle
     */
    private void handleEvent(@NotNull KeyEvent k){
        Client said = clients.get(k.getId());
        if(said != null){                                     //If client exists
            if(said.getLastModified() < k.getTimeStamp()){    //If timestamp is still valid
                switch(k.getKeyChar()){
                    case 'w': said.setCent(centroidMoveUp(said.getCent(), said.getLastModified(), k.getTimeStamp()));
                        break;
                    case 'a': said.setCent(centroidMoveLeft(said.getCent(), said.getLastModified(), k.getTimeStamp()));
                        break;
                    case 's': said.setCent(centroidMoveDown(said.getCent(), said.getLastModified(), k.getTimeStamp()));
                        break;
                    case 'd': said.setCent(centroidMoveRight(said.getCent(), said.getLastModified(), k.getTimeStamp()));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * A trivial centroid moving function that uses the modification-time-difference to calculate linear movement upwards
     * @param original The Centroid to move
     * @param lastModified The last modification time (from epoch in millis)
     * @param modifyTime The time of the movement (from epoch in millis)
     * @return The moved centroid
     */
    @NotNull
    private Centroid centroidMoveUp(@NotNull Centroid original, long lastModified, long modifyTime){
        return new Centroid(original.getX(), original.getY() + speed * (modifyTime - lastModified), original.getWeight(), original.getColor() );
    }

    /**
     * A trivial centroid moving function that uses the modification-time-difference to calculate linear movement downwards
     * @param original The Centroid to move
     * @param lastModified The last modification time (from epoch in millis)
     * @param modifyTime The time of the movement (from epoch in millis)
     * @return The moved centroid
     */
    @NotNull
    private Centroid centroidMoveDown(@NotNull Centroid original, long lastModified, long modifyTime){
        return new Centroid(original.getX(), original.getY() - speed * (modifyTime - lastModified), original.getWeight(), original.getColor() );
    }

    /**
     * A trivial centroid moving function that uses the modification-time-difference to calculate linear movement to the right
     * @param original The Centroid to move
     * @param lastModified The last modification time (from epoch in millis)
     * @param modifyTime The time of the movement (from epoch in millis)
     * @return The moved centroid
     */
    @NotNull
    private Centroid centroidMoveRight(@NotNull Centroid original, long lastModified, long modifyTime){
        return new Centroid(original.getX() + speed * (modifyTime - lastModified), original.getY(), original.getWeight(), original.getColor() );
    }

    /**
     * A trivial centroid moving function that uses the modification-time-difference to calculate linear movement to the left
     * @param original The Centroid to move
     * @param lastModified The last modification time (from epoch in millis)
     * @param modifyTime The time of the movement (from epoch in millis)
     * @return The moved centroid
     */
    @NotNull
    private Centroid centroidMoveLeft(@NotNull Centroid original, long lastModified, long modifyTime){
        return new Centroid(original.getX() + speed * (modifyTime - lastModified), original.getY(), original.getWeight(), original.getColor() );
    }
}
