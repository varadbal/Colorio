package ColorioServer;

import ColorioCommon.*;
import com.sun.istack.internal.NotNull;
import sun.rmi.runtime.Log;

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
    private ConcurrentLinkedQueue<KeyInput> toHandle = null;
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
                     @NotNull BlockingQueue<OutPacket> toSend, @NotNull ConcurrentLinkedQueue<KeyInput> toHandle) {
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
        KeyInput k = null;

        while(isRunning){
            LOGGER.info("Polling");
            checkPlayers();

            if((now = Instant.now().toEpochMilli()) - lastSent > 10){
                lastSent = now;
                sendStatus();
            }

            if((k = toHandle.poll()) != null){
                handleKeyInput(k);
                k = null;
            }
        }
    }

    /**
     * Removing disconnected players, checking if everything is valid, calculating in-game 'events'
     *     TODO complete('in-game events', improbable cases) and improve
     */
    private void checkPlayers(){
        LOGGER.info("CheckPlayers");
        Collection<Client> clis = clients.values();
        for(Client c : clis){
            //TODO everything

            /*if(c.isPlaying()){  //If it should have everything defined
                if(c.getCent() == null){ //Player's object not yet defined

                    Random rand = new Random();
                    float r = rand.nextFloat();
                    float g = rand.nextFloat();
                    float b = rand.nextFloat();
                    c.setCent(new Centroid(0.0, 0.0, defaultWeight, new Color(r, g, b)));

                }else if(false){

                }

            }*/
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
        //TODO move centroids here
        LOGGER.info("Sending");
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
     * Handling a single KeyInput, which can be either a KeyStatus-change or a -check
     * @param i The KeyInput to handle
     */
    private void handleKeyInput(@NotNull KeyInput i){
        if(i instanceof KeyEvent) {
            //TODO move centroids here
            LOGGER.info("Handling KeyEvent");
            KeyEvent k = (KeyEvent)i;
            Client said = clients.get(k.getPlayerId());
            if (said != null && said.isPlaying()) {                 //If client exists and playing (handshake done)
                if (said.getLastMoved() < k.getTimeStamp()) {       //If timestamp is still valid
                        //region Everything fine, handling event
                        if (k.getId() == java.awt.event.KeyEvent.KEY_PRESSED) {   //If pressed
                            switch (k.getKeyChar()) {                            //Turn the appropriate key on
                                case 'w':
                                    said.getKeys().setW(true);
                                    break;
                                case 'a':
                                    said.getKeys().setA(true);
                                    break;
                                case 's':
                                    said.getKeys().setS(true);
                                    break;
                                case 'd':
                                    said.getKeys().setD(true);
                                    break;
                            }
                        } else if (k.getId() == java.awt.event.KeyEvent.KEY_RELEASED) { //If released
                            switch (k.getKeyChar()) {                                  //Turn the appropriate key off
                                case 'w':
                                    said.getKeys().setW(false);
                                    break;
                                case 'a':
                                    said.getKeys().setA(false);
                                    break;
                                case 's':
                                    said.getKeys().setS(false);
                                    break;
                                case 'd':
                                    said.getKeys().setD(false);
                                    break;
                            }
                        }
                        //endregion
                }
            }
        }else if(i instanceof KeyStatus){
            LOGGER.info("Handling KeyStatus");
            KeyStatus k = (KeyStatus) i;
            Client said = clients.get(k.getPlayerId());
            if(said != null){                                       //If client exists
                if(said.isPlaying() == false){                      //If handshake is not yet done (uninitialized)
                    LOGGER.info("-It is a handshake");
                    said.setKeys(new ClientKeys(k.isWPressed(), k.isAPressed(), k.isSPressed(), k.isDPressed()));
                    said.setPlaying(true);
                }else{                                              //If handshake is done
                    LOGGER.info("It is a regular");
                    if(!said.keyCheck(toClientKeys(k))){            //If check is not alright
                        //TODO: OH SH*T!
                    }
                    //TODO move centroids here
                }
            }

        }
    }

    /**
     * KeyStatus to ClientKeys method
     * @param ks The KeyStatus to convert
     * @return The resulting ClientKeys
     */
    private ClientKeys toClientKeys(KeyStatus ks){
        ClientKeys toReturn = new ClientKeys(ks.isWPressed(), ks.isAPressed(), ks.isSPressed(), ks.isDPressed());
        return toReturn;
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
