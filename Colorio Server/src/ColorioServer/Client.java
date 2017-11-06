package ColorioServer;

import java.net.InetAddress;

public class Client {
    private String name;
    private InetAddress addr;
    private boolean readyToPlay = false;


    Client(String name, InetAddress address){
        this.name = name;
        addr = address;
    }

    public String getName() {
        return name;
    }

    public InetAddress getAddr() {
        return addr;
    }

    public boolean isReadyToPlay() {
        return readyToPlay;
    }

    public void setAddr(InetAddress addr) {
        this.addr = addr;
    }

    public void setReadyToPlay(boolean readyToPlay) {
        this.readyToPlay = readyToPlay;
    }
}
