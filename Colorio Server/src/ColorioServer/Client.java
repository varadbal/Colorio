package ColorioServer;

import ColorioCommon.Centroid;
import com.sun.istack.internal.NotNull;

import java.net.InetAddress;
import java.time.Instant;

public class Client {
    private String name;            //Communciation modifies
    private InetAddress addr;       //Communication modifies
    private boolean isPlaying = false;  //Communication modifies, GameLogic uses
    private Centroid cent = null;   //GameLogic modifies (!)
    private long lastModified = 0L; //Last Game-Modify


    Client(@NotNull String name, @NotNull InetAddress address){
        this.name = name;
        addr = address;
    }

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

    public long getLastModified() {
        return lastModified;
    }

    public void setAddr(@NotNull InetAddress addr) {
        this.addr = addr;
    }

    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
    }

    public void setCent(Centroid cent) {
        this.cent = cent;
        lastModified = Instant.now().toEpochMilli();
    }


}
