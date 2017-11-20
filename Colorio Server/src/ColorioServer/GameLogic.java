package ColorioServer;

import ColorioCommon.*;
import com.sun.istack.internal.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

import static ColorioCommon.Constants.startingWeight;

/**
 * ColorIo Server Game Logic class
 * @Author Balazs Varady
 */

public class GameLogic{
    //region Variables
    /**
     * Logger
     */
    private static final Logger LOGGER = Logger.getLogger(GameLogic.class.getName());
    /**
     * Instance variables
     */
    private ConcurrentMap<Integer, Client> clients = null;
    private MovePlayers moveP = null;
    private HandleInput handleI = null;
    private SendGameStatus sendS = null;
    private boolean isRunning = false;
    private int noInputTimeOut = 500;
    private int commTimeOut = Constants.responseTimeOut;
    //endregion

    /**
     * Constructor, initializing 'clients' and the Threads
     * @param clients A map of the clients (id, Client)
     * @param toSend A queue for communication with ServerOut
     * @param toHandle A queue for the incoming KeyInputs
     */
    public GameLogic(@NotNull ConcurrentMap<Integer, Client> clients,
                     @NotNull BlockingQueue<OutPacket> toSend, @NotNull BlockingQueue<KeyInput> toHandle){
        this.clients = clients;
        this.handleI = new HandleInput("HandleInput-1", toHandle);
        this.moveP = new MovePlayers("MovePlayers-1");
        this.sendS = new SendGameStatus("SendGameStatus-1", toSend);
    }

    /**
     * Starting the Move-, Send and Handle-threads
     * TODO complete
     */
    public void start(){
        LOGGER.info("Starting GameLogic");

        isRunning = true;
        if(moveP != null){
            moveP.start();
        }else{
            /**
             * TODO throw exception
             */
        }
        if(handleI != null){
            handleI.start();
        }else{
            /**
             * TODO throw exception
             */
        }
        if(sendS != null){
            sendS.start();
        }else{
            /**
             * TODO throw exception
             */
        }
    }

    /**
     * Stopping the threads
     */
    public void stop(){
        isRunning = false;
    }

    /**
     * Thread for moving and checking the players in regular time intervals
     * Should not be manipulated from the outside
     */
    private class MovePlayers implements Runnable{
        //region Variables
        /**
         * Thread variables
         */
        Thread t;
        String threadName;
        //endregion

        //region Methods
        /**
         * Constructor, initializing variables
         * @param threadName The name of the Thread
         */
        private MovePlayers(String threadName){
            this.threadName = threadName;
        }

        /**
         * Thread start method
         */
        private void start(){
            LOGGER.info("Starting thread " + threadName);
            if(t == null){
                t = new Thread(this, threadName);
                t.start();
            }
        }

        /**
         * Thread run method
         *     Checks map for inactive players
         *     Moves a (playing) player (or waits until it is possible)
         *     Checks map (with that player in the 'center of attention')
         * TODO implement
         */
        @Override
        public void run() {
            LOGGER.info("Running thread " + threadName);

            while(isRunning){
                Set<Map.Entry<Integer, Client>> es = clients.entrySet();
                ArrayList<Integer> toRemove = new ArrayList<>();
                /*Iterate through the clients*/
                for(Map.Entry<Integer, Client> i : es){
                    /*Get the inactive-client keys and skip them*/
                    long atm = Instant.now().toEpochMilli();//So that it is calculated only once
                    if(atm - i.getValue().getLastCheck() > commTimeOut){
                        toRemove.add(i.getKey());
                        continue;
                    }

                    /*Move the (active) players (clients)*/
                    if(i.getValue().isPlaying()) {
                        i.getValue().moveCentroid();
                        checkMap(i.getKey());
                    }
                }

                /*Remove the inactive clients*/
                for(Integer i : toRemove){
                    clients.remove(i);
                }
            }
            LOGGER.info("Stopping thread " + threadName);
        }

        private void checkMap(int lastMovedId){

        }


        //endregion
    }

    /**
     * Thread for handling the incoming KeyInputs
     * Should not be manipulated from the outside
     */
    private class HandleInput implements Runnable{
        //region Variables
        /**
         * Thread variables
         */
        Thread t;
        String threadName;
        /**
         * Instance variables
         */
        private BlockingQueue<KeyInput> toHandle = null;
        //endregion

        //region Methods
        /**
         * Constructor, initializing variables
         * @param threadName The name of the Thread
         * @param toHandle The queue of the incoming KeyInputs to Handle
         */
        private HandleInput(String threadName, @NotNull BlockingQueue<KeyInput> toHandle){
            this.threadName = threadName;
            this.toHandle = toHandle;
        }

        /**
         * Thread start method
         */
        private void start(){
            LOGGER.info("Starting thread " + threadName);
            if(t == null){
                t = new Thread(this, threadName);
                t.start();
            }
        }

        /**
         * Thread run method
         *     Handles and incoming KeyInput (or waits until it becomes possible)
         */
        @Override
        public void run() {
            LOGGER.info("Running thread " + threadName);

            while(isRunning){

                KeyInput i = null;
                try {
                    i = toHandle.poll(noInputTimeOut, TimeUnit.MILLISECONDS);
                }catch (InterruptedException e){
                    continue;
                }

                if(i instanceof KeyEvent){
                    KeyEvent e = (KeyEvent)i;
                    updateStatus(e);
                }else if(i instanceof KeyStatus){
                    KeyStatus s = (KeyStatus)i;
                    checkStatus(s);
                }

            }
            LOGGER.info("Stopping thread " + threadName);
        }

        /**
         * Updating the 'keys' (key status) of the Client according to the incoming KeyEvent
         * @param k The incoming KeyEvent
         */
        private void updateStatus(KeyEvent k){
            LOGGER.info("Handling KeyEvent");
            Client said = clients.get(k.getPlayerId());
            if (said != null && said.isPlaying()) {                 //If client exists and playing (handshake done)
                if (said.getLastKeyUpdate() < k.getTimeStamp()) {   //If timestamp is still valid
                    //region Everything fine, handling KeyEvent (KeyStatus-change)
                    if (k.getId() == java.awt.event.KeyEvent.KEY_PRESSED) {   //If pressed
                        switch (k.getKeyChar()) {                            //Turn the appropriate key on
                            case 'w':
                                said.setKeys(said.new ClientKeys(true, said.getKeys().isA(), said.getKeys().isS(), said.getKeys().isD()));
                                break;
                            case 'a':
                                said.setKeys(said.new ClientKeys(said.getKeys().isW(), true, said.getKeys().isS(), said.getKeys().isD()));
                                break;
                            case 's':
                                said.setKeys(said.new ClientKeys(said.getKeys().isW(), said.getKeys().isA(), true, said.getKeys().isD()));
                                break;
                            case 'd':
                                said.setKeys(said.new ClientKeys(said.getKeys().isW(), said.getKeys().isA(), said.getKeys().isS(), true));
                                break;
                        }
                    } else if (k.getId() == java.awt.event.KeyEvent.KEY_RELEASED) { //If released
                        switch (k.getKeyChar()) {                                  //Turn the appropriate key off
                            case 'w':
                                said.setKeys(said.new ClientKeys(false, said.getKeys().isA(), said.getKeys().isS(), said.getKeys().isD()));
                                break;
                            case 'a':
                                said.setKeys(said.new ClientKeys(said.getKeys().isW(), false, said.getKeys().isS(), said.getKeys().isD()));
                                break;
                            case 's':
                                said.setKeys(said.new ClientKeys(said.getKeys().isW(), said.getKeys().isA(), false, said.getKeys().isD()));
                                break;
                            case 'd':
                                said.setKeys(said.new ClientKeys(said.getKeys().isW(), said.getKeys().isA(), said.getKeys().isS(), false));
                                break;
                        }
                    }
                    //endregion
                }
            }
        }

        /**
         * Either initializing the Client (completing handshake) or comparing the key status with the incoming one
         * @param k The incoming KeyStatus-Object
         */
        private void checkStatus(KeyStatus k){
            LOGGER.info("Handling KeyStatus");
            Client said = clients.get(k.getPlayerId());
            if(said != null){                                       //If client exists
                if(said.isPlaying() == false){                      //If uninitialized, complete handshake (initialize)
                    LOGGER.info("-It is a handshake");
                    said.setKeys(k);
                    Random rand = new Random();
                    said.setCent(new Centroid(50.0, 500.0, startingWeight, new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat())));
                    said.setPlaying(true);
                }else{                                              //If initialized (handshake is done)
                    LOGGER.info("-It is a check");
                    if(!said.keyCheck(k)){                          //If check is not alright, update to correct (for now)
                        said.setKeys(k);
                    }else{                                          //If check is alright
                        //TODO: Yay!
                    }
                }
            }
        }



        //endregion
    }

    /**
     * Thread for sending the GameStatus in regular time intervals
     * Should not be manipulated from the outside
     */
    private class SendGameStatus implements Runnable{
        //region Variables
        /**
         * Thread variables
         */
        Thread t;
        String threadName;
        /**
         * Instance variables
         */
        private BlockingQueue<OutPacket> toSend = null;
        //endregion

        //region Methods
        /**
         * Constructor, initializing variables
         * @param threadName The name of the Thread
         * @param toSend A queue for communication with the ServerOut
         */
        private SendGameStatus(String threadName, @NotNull BlockingQueue<OutPacket> toSend){
            this.threadName = threadName;
            this.toSend = toSend;
        }

        /**
         * Thread start method
         */
        private void start(){
            LOGGER.info("Starting thread " + threadName);
            if(t == null){
                t = new Thread(this, threadName);
                t.start();
            }
        }

        /**
         * Thread run method, calling send regularly
         * TODO more elegant (e.g. with Timer & TimerTask ?)
         */
        @Override
        public void run() {
            LOGGER.info("Running thread " + threadName);

            while(isRunning){
                send();

                try {
                    Thread.sleep(10);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            LOGGER.info("Stopping thread " + threadName);
        }

        /**
         * Prepare and 'send' a GameStatus object for each (playing) client
         */
        private void send(){
            //LOGGER.info("Sending");
            GameStatus currentStatus;
            Set<Map.Entry<Integer, Client>> es = clients.entrySet();

            for(Map.Entry<Integer, Client> i : es){
                if(i.getValue().isPlaying()) {
                    currentStatus = new GameStatus();
                    for (Map.Entry<Integer, Client> j : es) {
                        if (j.getKey() == i.getKey()) {
                            currentStatus.addCentroid(0, j.getValue().getCent());
                        } else {
                            currentStatus.addCentroid(j.getValue().getCent());
                        }
                    }
                    try {
                        toSend.put(new OutPacket(i.getKey(), currentStatus));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //endregion
    }


}