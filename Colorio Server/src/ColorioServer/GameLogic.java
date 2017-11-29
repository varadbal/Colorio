package ColorioServer;

import ColorioCommon.*;
import com.sun.istack.internal.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

import static ColorioCommon.Constants.*;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

/**
 * ColorIo Server Game Logic class
 * @author Balazs Varady
 */

public class GameLogic{
    //region Variables
    private static final Logger LOGGER = Logger.getLogger(GameLogic.class.getName());
    //Policy: INFO - once/server, FINE - once/client, FINER - other frequent events, FINEST - inner statuses
    /*
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
    private Timer timer = null;
    //endregion

    /**
     * Constructor, initializing clients, foods and the Threads/Tasks
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
        this.timer = new Timer();

        //Initialize foods
        this.foods = new ConcurrentHashMap<>();
        Random rand = new Random();
        for(int i = foods.size(); i < foodsAtOnce; ++i){
            foods.put(++foodId, new Centroid(rand.nextFloat() * mapMaxX, rand.nextFloat() * mapMaxY, foodWeight, foodColor));
        }
    }

    /**
     * Starting the Move-, Send and Handle-threads/tasks
     */
    public void start(){
        LOGGER.info("Starting GameLogic");

        isRunning = true;
        if(moveP != null){
            //moveP.start();
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
           // sendS.start();
            timer.scheduleAtFixedRate(sendS, 0, Constants.serverSleep);
        }else{
            throw new IllegalStateException();
        }
    }

    /**
     * Stopping the threads and tasks
     */
    public void stop(){
        isRunning = false;
        timer.cancel();
    }

    /**
     * TimerTask (to schedule) for moving and checking the players in regular time intervals
     */
    private class MovePlayers extends TimerTask{
        //region Variables
        /**The name of this Thread for logging*/
        private String threadName;
        //endregion

        //region Methods
        /**
         * Constructor, initializing variables
         * @param threadName The name of the Thread/Task for logging
         */
        private MovePlayers(String threadName){
            this.threadName = threadName;
        }

        /**
         * TimerTask run method
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
         * Checks the game-map for 'game-events'
         *  Checks if players have eaten any foods
         *  Checks if players have eaten each other
         *  Adds new foods to the map, if necessary
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
                        checkPoP(i.getValue().getPlayer(), j.getValue().getPlayer());
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

                if(distance < ((c.getWeight() < 500.0) ? c.getWeight() / 10.0 : (c.getWeight() < 1000 ? c.getWeight()/15.0 : c.getWeight()/20.0))) {
                    toReturn +=foods.get(f).getWeight();
                    foodsToRemove.add(f);
                    System.out.println(c.getWeight());
                }
            }
            for (Integer k : foodsToRemove) {
                foods.remove(k);
            }

            return toReturn;
        }

        /**
         * Finds overlapping Players and handles them eating each other if necessary
         * @param i One Player
         * @param j Another Player
         */
        private void checkPoP(Player i, Player j){
            /*ArrayList<Centroid> p1 = new ArrayList<>();
            p1.add(i.getTop()); p1.add(i.getBottom()); p1.add(i.getLeft()); p1.add(i.getRight());*/
            ArrayList<Centroid> p2 = new ArrayList<>();
            p2.add(j.getTop()); p2.add(j.getBottom()); p2.add(j.getLeft()); p2.add(j.getRight());

            Player eaten = null;    //if a whole player was eaten
            for(Centroid c2 : p2){
                double area1 = abs(((c2.getX())*(i.getTop().getY()-i.getRight().getY()) + (i.getTop().getX())*(i.getRight().getY()-c2.getY()) + (i.getRight().getX())*(c2.getY()-i.getTop().getY()))/2);
                double area2 = abs(((c2.getX())*(i.getTop().getY()-i.getLeft().getY()) + (i.getTop().getX())*(i.getLeft().getY()-c2.getY()) + (i.getLeft().getX())*(c2.getY()-i.getTop().getY()))/2);
                double area3 = abs(((c2.getX())*(i.getBottom().getY()-i.getRight().getY()) + (i.getBottom().getX())*(i.getRight().getY()-c2.getY()) + (i.getRight().getX())*(c2.getY()-i.getBottom().getY()))/2);
                double area4 = abs(((c2.getX())*(i.getBottom().getY()-i.getLeft().getY()) + (i.getBottom().getX())*(i.getLeft().getY()-c2.getY()) + (i.getLeft().getX())*(c2.getY()-i.getBottom().getY()))/2);

                double rectA = abs(((i.getTop().getX()*i.getRight().getY()-i.getTop().getY()*i.getRight().getX()) + (i.getRight().getX()*i.getBottom().getY()-i.getRight().getY()*i.getBottom().getX())
                                + (i.getBottom().getX()*i.getLeft().getY()-i.getBottom().getY()*i.getLeft().getX() + (i.getLeft().getX()*i.getTop().getY()-i.getLeft().getY()*i.getTop().getX())))/2);


                if(area1+area2+area3+area4 > rectA + 0.01){ //If true, c2 is outside the rectangle
                    //Do nothing for now
                }else if(abs(area1+area2+area3+area4 - rectA) < 0.01){  //If true, c2 is either on the rectangle (one area is 0) or inside (else)
                    Centroid c1 = null;
                    if(c2 == j.getTop()){
                        c1 = i.getBottom();
                    }else if(c2 == j.getBottom()){
                        c1 = i.getTop();
                    }else if(c2 == j.getLeft()){
                        c1 = i.getRight();
                    }else if(c2 == j.getRight()){
                        c1 = i.getLeft();
                    }
                    if((c1 != null) && (c1.getWeight() < c2.getWeight())){  //p2(=j) eats c1(=Centroid of i)
                        j.growPlayer(c1.weight);
                        c1.setLocation(i.calculateMiddleX(),i.calculateMiddleY());
                        i.shrinkPlayer(c1.weight);

                        if(i.getTop().getWeight() < foodWeight){
                            eaten = i;
                        }
                    }
                }
            }

            if(eaten != null){//If eaten, set playing off. For now, this will generate a new Player for him (or dc)
                for(Map.Entry<Integer, Client> c : clients.entrySet()){
                    if(c.getValue().getPlayer() == eaten){
                        c.getValue().setPlaying(false);
                        c.getValue().setPlayer(null);
                    }
                }
            }
        }


        //endregion
    }

    /**
     * Thread for handling the incoming KeyInputs
     */
    private class HandleInput implements Runnable{
        //region Variables
        /**A thread to run this class*/
        private Thread t;
        /**The name of this Thread for logging*/
        private String threadName;
        /**A queue of the incoming Events to handle*/
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
         *  Handles the incoming KeyInputs (or waits until it becomes possible)
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
         * Handling KeyEvent
         *  Updating the 'keys' (key status) of the Client according to the incoming KeyEvent
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
         * Handling KeyStatus
         *  Either initializing the Client (completing handshake) or comparing the key status with the incoming one
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
         * Creates a new Player-Object
         * @return The new Player-Object
         */
        private Player createNewPlayer(){
            Random rand = new Random();
            float min = 0.7f;
            float max = 1.0f;
            Color col = new Color(rand.nextFloat() * (max-min)+min, rand.nextFloat() * (max-min)+min, rand.nextFloat() * (max-min)+min);
            double initX = 200;
            double initY = 300;
            double radius = radius(startingWeight);//2 * startingWeight / 10;
            return new Player(
                    new Centroid(initX, initY - radius, startingWeight, /*new Color(255, 0, 0)),*/col),
                    new Centroid(initX, initY + radius, startingWeight, /*new Color(0,255,255)),*/col),
                    new Centroid(initX - radius, initY, startingWeight, /*new Color(0, 0, 255)),*/col),
                    new Centroid(initX + radius, initY, startingWeight, /*new Color(200, 200, 200))*/col));
        }

        //endregion
    }

    /**
     * TimerTask for sending the GameStatus in regular time intervals
     */
    private class SendGameStatus extends TimerTask{
        //region Variables
        /**The name of this Thread for logging*/
        String threadName;
        /**A queue of the OutPackets to send*/
        private BlockingQueue<OutPacket> toSend = null;
        //endregion

        //region Methods
        /**
         * Constructor, initializing variables
         * @param threadName The name of the Thread(/Task)
         * @param toSend A queue for communication with the ServerOut
         */
        private SendGameStatus(String threadName, @NotNull BlockingQueue<OutPacket> toSend){
            this.threadName = threadName;
            this.toSend = toSend;
        }

        /**
         * TimerTask run method
         *  Calling send regularly
         */
        @Override
        public void run() {
            LOGGER.finest("Running task " + threadName);
                send();
        }

        /**
         * Prepares and 'sends' a GameStatus object for each (playing) client
         */
        private void send(){
            GameStatus currentStatus = new GameStatus();
            Set<Map.Entry<Integer, Client>> es = clients.entrySet();

            //Put all Player-Objects in currentStatus
            for(Map.Entry<Integer, Client> i : es){
                if(i.getValue().isPlaying()) {
                    currentStatus.addPlayerEntry(new PlayerEntry(i.getKey(), i.getValue().getPlayer(), i.getValue().getName()));
                }
            }
            //Put all Foods in currentStatus
            for(Centroid i : foods.values()){
                currentStatus.addFood(i);
            }
            //Schedule currentStatus to send to every Client
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