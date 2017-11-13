package ColorioServer;

import ColorioCommon.Centroid;
import ColorioCommon.KeyStatus;
import com.sun.istack.internal.NotNull;

import java.net.InetAddress;
import java.security.Key;
import java.time.Instant;

public class Client {
    //region Communication Variables
    private String name;            //Communciation modifies
    private InetAddress addr;       //Communication modifies
    //endregion

    //region GameLogic Variables
    private boolean isPlaying = false;  //If true, all variables should be set
    private Centroid cent = null;       //Centroid of the Client
    private long lastKeyCheck = 0L;     //Last Check of Keys ClientKeys.equals() updates
    private long lastKeyUpdate = 0L;    //Last update of 'keys' (setKeys() updates)
    private ClientKeys keys = null;     //Keys pressed on the client
    //endregion


    /**
     * Constructor initializing an empty Client
     * @param name
     * @param address
     */
    Client(@NotNull String name, @NotNull InetAddress address){
        this.name = name;
        addr = address;
    }


    /**
     * Comparing the keys with a control-object
     * @param control The ClientKeys-Object to compare with the object's actual keys
     * @return True if the two objects are similar
     */
    public boolean keyCheck(ClientKeys control){
        lastKeyCheck = Instant.now().toEpochMilli();
        if(keys.equals(control)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Getters
     */
    public String getName() {
        return name;
    }

    public InetAddress getAddr() {
        return addr;
    }

    public Centroid getCent(){
        return this.cent;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public ClientKeys getKeys() {
        return keys;
    }

    public long getLastKeyCheck() {
        return lastKeyCheck;
    }

    public long getLastKeyUpdate() {
        return lastKeyUpdate;
    }

    /**
     * Setters
     */
    public void setAddr(@NotNull InetAddress addr) {
        this.addr = addr;
    }

    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
    }

    public void setCent(Centroid cent) {
        this.cent = cent;
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
     * @param k
     * @return
     */
    public boolean keyCheck(KeyStatus k){
        lastKeyCheck = Instant.now().toEpochMilli();
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
     * Moves the Centroid of the Client, according to the (currently) pressed keys
     * FIXME for now only doing something
     */
    public void moveCentroid(){

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
        }else if(keys.isD()){
            vertical = 1;
        }

        cent.setLocation(cent.getX() + horizontal * 1, cent.getY() + vertical * 1);
    }

    /**
     * Class representing the key-status of the Client on the server-side
     * TODO comment
     */
    public class ClientKeys {
        /**
         * Key-Variables (final so that no setters thus lastModified is correctly and simply updated)
         */
        private final boolean w;
        private final boolean a;
        private final boolean s;
        private final boolean d;

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

    }

}
