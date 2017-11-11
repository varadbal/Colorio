package ColorioServer;

import ColorioCommon.Centroid;
import ColorioCommon.KeyStatus;
import com.sun.istack.internal.NotNull;

import java.net.InetAddress;
import java.time.Instant;

public class Client {
    private String name;            //Communciation modifies
    private InetAddress addr;       //Communication modifies
    private boolean isPlaying = false;  //Communication modifies, GameLogic uses
    private Centroid cent = null;   //GameLogic modifies (!)
    private long lastMoved = 0L;    //Last Game-Movement
    private long lastKeyCheck = 0L;  //Last Check of Keys
    private ClientKeys keys = null; //Keys pressed on the client


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

    public long getLastMoved() {
        return lastMoved;
    }

    public ClientKeys getKeys() {
        return keys;
    }

    public long getLastKeyCheck() {
        return lastKeyCheck;
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
        lastMoved = Instant.now().toEpochMilli();
    }

    public void setKeys(ClientKeys Keys) {
        this.keys = keys;
    }
}
