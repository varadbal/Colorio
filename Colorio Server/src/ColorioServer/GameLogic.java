package ColorioServer;

import ColorioCommon.*;
import com.sun.istack.internal.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

import static ColorioCommon.Constants.*;
import static java.lang.Math.sqrt;

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
    //Policy: INFO - once/server, FINE - once/client, FINER - other frequent events, FINEST - inner statuses
    /**
     * Instance variables
     */
    private ConcurrentMap<Integer, Client> clients = null;
    private MovePlayers moveP = null;
    private HandleInput handleI = null;
    private SendGameStatus sendS = null;
    private ConcurrentMap<Integer, Centroid> foods = null;      //Concurrent Set part matters
    private int foodId = 0;                                     //Just to have some unique keys
    private Color foodColor = new Color(255, 0, 0);
    private int foodsAtOnce = 20;
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

        //Initialize foods
        this.foods = new ConcurrentHashMap<>();
        Random rand = new Random();
        for(int i = foods.size(); i < foodsAtOnce; ++i){
            foods.put(++foodId, new Centroid(rand.nextFloat() * mapMaxX, rand.nextFloat() * mapMaxY, foodWeight, foodColor));
        }
    }

    /**
     * Starting the Move-, Send and Handle-threads
     * TODO complete
     */
    public void start(){
        LOGGER.info("Starting GameLogic");

        isRunning = true;
        if(moveP != null){
            //moveP.start();
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(moveP, 0, Constants.serverSleep / 2);
        }else{
            throw new IllegalStateException();
        }
        if(handleI != null){
            handleI.start();
        }else{
            throw new IllegalStateException();
        }
        if(sendS != null){
            sendS.start();
        }else{
            throw new IllegalStateException();
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
    private class MovePlayers extends TimerTask{
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
        /*private void start(){
            LOGGER.info("Starting thread " + threadName);
            if(t == null){
                t = new Thread(this, threadName);
                t.start();
            }
        }*/

        /**
         * Thread run method
         *     Checks map for inactive players
         *     Moves a (playing) player (or waits until it is possible)
         *     Checks map (with that player in the 'center of attention')
         */
        @Override
        public void run() {
            LOGGER.finest("Running thread " + threadName);

            //long lastOne = 0L;
            //while(isRunning){
                /*Troll Timing*/
                //if(Instant.now().toEpochMilli() - lastOne > Constants.serverSleep / 2) {
                    //lastOne = Instant.now().toEpochMilli();
                    /*Troll timing ends*/
                    LOGGER.finest("Moving player");

                    Set<Map.Entry<Integer, Client>> es = clients.entrySet();
                    ArrayList<Integer> toRemove = new ArrayList<>();

                    /*Get the inactive-clients*/
                    for (Map.Entry<Integer, Client> i : es) {
                        long atm = Instant.now().toEpochMilli();//So that it is calculated only once
                        if (atm - i.getValue().getLastCheck() > commTimeOut) {
                            toRemove.add(i.getKey());
                            continue;
                        }

                    /*Move the (active) players (clients)*/
                        if (i.getValue().isPlaying()) {
                            i.getValue().movePlayer();
                        }
                    }
                    checkMap();
                /*Remove the inactive clients*/
                    for (Integer i : toRemove) {
                        clients.remove(i);
                    }

                //}
            //}
            //LOGGER.info("Stopping thread " + threadName);
        }

        /**
         * Adds new foods to the map, if necessary
         */
        private void checkMap(){
            Set<Map.Entry<Integer, Client>> es = clients.entrySet();

            //Check Players-On-Foods
            for(Map.Entry<Integer, Client> i : es) {
                if (i.getValue().isPlaying()) {
                    i.getValue().getPlayer().growPlayer(eatFoodsWithCentroid(i.getValue().getPlayer().getTop()));
                    i.getValue().getPlayer().growPlayer(eatFoodsWithCentroid(i.getValue().getPlayer().getBottom()));
                    i.getValue().getPlayer().growPlayer(eatFoodsWithCentroid(i.getValue().getPlayer().getLeft()));
                    i.getValue().getPlayer().growPlayer(eatFoodsWithCentroid(i.getValue().getPlayer().getRight()));
                }
            }

            //Check Players-On-Players
            for(Map.Entry<Integer, Client> i : es){
                for(Map.Entry<Integer, Client> j : es){
                    if(!i.equals(j)){
                        //TODO implement
                    }
                }
            }

            //Add foods if necessary
            Random rand = new Random();
            for(int i = 0; i < foodsAtOnce-foods.size(); ++i){
                foods.put(++foodId, new Centroid(rand.nextDouble()*mapMaxX, rand.nextDouble()*mapMaxY, foodWeight, foodColor));
            }

        }

        /**
         *Checks (also eats if possible) the foods with the given Centroid
         * @param c The Centroid to eat with
         * @return The weight of the eaten foods
         */
        private double eatFoodsWithCentroid(Centroid c){
            double toReturn = 0.0;

            ArrayList<Integer> foodsToRemove = new ArrayList<>();
            for (Integer f : foods.keySet()) {
                double distanceX = c.getX() - foods.get(f).getX();
                double distanceY = c.getY() - foods.get(f).getY();
                double distance = sqrt(distanceX * distanceX + distanceY * distanceY);

                if(distance < c.weight / 10) {
                    toReturn +=foods.get(f).getWeight();
                    foodsToRemove.add(f);
                }
            }
            for (Integer k : foodsToRemove) {
                foods.remove(k);
            }

            return toReturn;
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
            LOGGER.finest("Handling KeyStatus");
            Client said = clients.get(k.getPlayerId());
            if(said != null){                                       //If client exists
                if(said.isPlaying() == false){                      //If uninitialized, complete handshake (initialize)
                    LOGGER.finest("-It is a handshake");
                    said.setKeys(k);
                    said.setPlayer(createNewPlayer());
                    said.setPlaying(true);
                }else{                                              //If initialized (handshake is done)
                    LOGGER.finest("-It is a check");
                    if(!said.keyCheck(k)){                          //If check is not alright, update to correct (for now)
                        said.setKeys(k);
                        //LOGGER.info("Keys out of sync: Client-" + k.getPlayerId());
                    }else{                                          //If check is alright
                        //TODO: Yay!
                    }
                }
            }
        }

        /**
         * Returns a new Player-Object
         * @return the created Player
         */
        private Player createNewPlayer(){
            Random rand = new Random();
            float min = 0.7f;
            float max = 1.0f;
            Color col = new Color(rand.nextFloat() * (max-min)+min, rand.nextFloat() * (max-min)+min, rand.nextFloat() * (max-min)+min);
            double initX = 200;
            double initY = 300;
            double radius = 2 * startingWeight / 10;
            return new Player(
                    new Centroid(initX, initY - radius, startingWeight, /*new Color(255, 0, 0)),*/col),
                    new Centroid(initX, initY + radius, startingWeight, /*new Color(0,255,255)),*/col),
                    new Centroid(initX - radius, initY, startingWeight, /*new Color(0, 0, 255)),*/col),
                    new Centroid(initX + radius, initY, startingWeight, /*new Color(200, 200, 200))*/col));
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
                    Thread.sleep(Constants.serverSleep);
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
            GameStatus currentStatus = new GameStatus();
            Set<Map.Entry<Integer, Client>> es = clients.entrySet();

            for(Map.Entry<Integer, Client> i : es){
                if(i.getValue().isPlaying()) {
                    currentStatus.addPlayerEntry(new PlayerEntry(i.getKey(), i.getValue().getPlayer(), i.getValue().getName()));
                }
            }
            for(Centroid i : foods.values()){
                currentStatus.addFood(i);
            }

            for(Map.Entry<Integer, Client> i : es){
                try {
                    if(i.getValue().isPlaying()) {
                        toSend.put(new OutPacket(i.getKey(), currentStatus));
                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
        //endregion
    }


}