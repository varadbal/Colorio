package ColorioServer;

import ColorioCommon.KeyStatus;
import ColorioCommon.Player;
import com.sun.istack.internal.NotNull;

import java.net.InetAddress;
import java.time.Instant;


/**
 * Class representing a client on the server-side
 * @author Balazs Varady
 */
public class Client {
    //region Communication Variables

    private String name;            //Communciation modifies
    private InetAddress addr;       //Communication modifies
    //endregion

    //region GameLogic Variables
    private boolean isPlaying = false;  //If true, all variables should be set
    private Player player = null;       //Centroid of the Client
    private long lastCheck = 0L;     //Last Check of Keys ClientKeys.equals() updates (and set once in c'tor)
    private long lastKeyUpdate = 0L;    //Last update of 'keys' (setKeys() updates)
    private long lastMoved = 0L;
    private ClientKeys keys = null;     //Keys pressed on the client
    //endregion


    /**
     * Constructor, initializing an empty Client
     * @param name The name of the client
     * @param address The IP-address of the client
     */
    Client(@NotNull String name, @NotNull InetAddress address){
        this.name = name;
        addr = address;
        lastCheck = Instant.now().toEpochMilli();
    }


    /**
     * Comparing the keys with a control-object
     * @param control The ClientKeys-Object to compare with the object's actual keys
     * @return True if the two objects are similar
     */
    public boolean keyCheck(ClientKeys control){
        lastCheck = Instant.now().toEpochMilli();
        if(keys.equals(control)){
            return true;
        }else{
            return false;
        }
    }

    /*
     * Getters
     */
    public String getName() {
        return name;
    }

    public InetAddress getAddr() {
        return addr;
    }

    public Player getPlayer(){
        return this.player;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public ClientKeys getKeys() {
        return keys;
    }

    public long getLastCheck() {
        return lastCheck;
    }

    public long getLastKeyUpdate() {
        return lastKeyUpdate;
    }

    /*
     * Setters
     */
    public void setAddr(@NotNull InetAddress addr) {
        this.addr = addr;
    }

    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setKeys(KeyStatus Keys) {
        lastKeyUpdate = Instant.now().toEpochMilli();
        this.keys = toClientKeys(Keys);
    }

    public void setKeys(ClientKeys Keys){
        lastKeyUpdate = Instant.now().toEpochMilli();
        this.keys = Keys;
    }

    /**
     * Checks if 'keys' is the same as the provided KeyStatus object (converted to ClientKeys)
     * @param k The KeyStatus-Object to compare with
     * @return If the two objects represent the same keys
     */
    public boolean keyCheck(KeyStatus k){
        lastCheck = Instant.now().toEpochMilli();
        if(keys.equals(toClientKeys(k))){
            return true;
        }
        return false;
    }

    /**
     * Returns a new ClientKeys object according to a provided KeyStatus object
     * @param k The KeyStatus object to convert
     * @return The resulting ClientKeys object
     */
    public ClientKeys toClientKeys(KeyStatus k){
        return new ClientKeys(k.isWPressed(), k.isAPressed(), k.isSPressed(), k.isDPressed());
    }

    /**
     * Moves the Player of the Client, according to the (currently) pressed keys
     *  More like 'update'
     */
    public void movePlayer(){

        if(lastMoved == 0L){ //Check if it was ever moved (no -> initialize)
            lastMoved = Instant.now().toEpochMilli();
        }

        int horizontal = 0;
        int vertical = 0;

        if(keys.isD() && keys.isA()){
            horizontal = 0;
        }else if(keys.isD()){
            horizontal = 1;
        }else if(keys.isA()){
            horizontal = -1;
        }
        if(keys.isW() && keys.isS()){
            vertical = 0;
        }else if(keys.isW()){
            vertical = -1;
        }else if(keys.isS()){
            vertical = 1;
        }

        long now = Instant.now().toEpochMilli();
        player.movePlayer(horizontal, vertical, now - lastMoved);
        player.manageDistances(now - lastMoved);
        lastMoved = now;
    }

    /**
     * Class representing the key-status of the Client on the server-side
     */
    public class ClientKeys {
        /**
         * Key-Variables (final so that no setters thus lastModified is correctly and simply updated)
         */
        private final boolean w;
        private final boolean a;
        private final boolean s;
        private final boolean d;

        /**
         * Constructor with every key
         * @param w Whether the w-key is pressed
         * @param a Whether the a-key is pressed
         * @param s Whether the s-key is pressed
         * @param d Whether the d-key is pressed
         */
        public ClientKeys(boolean w, boolean a, boolean s, boolean d) {
            this.w = w;
            this.a = a;
            this.s = s;
            this.d = d;
        }

        public boolean isW() {
            return w;
        }
        public boolean isA() {
            return a;
        }
        public boolean isS() {
            return s;
        }
        public boolean isD() {
            return d;
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof ClientKeys){
                if(     this.w == ((ClientKeys) o).w
                        && this.a == ((ClientKeys) o).a
                        && this.s == ((ClientKeys) o).s
                        && this.d == ((ClientKeys) o).d){
                    return true;
                }
            }
            return false;
        }

    }

}
