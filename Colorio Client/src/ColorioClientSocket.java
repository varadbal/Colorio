import java.io.IOException;
import java.net.*;

public class ColorioClientSocket extends DatagramSocket {
    private final int timeOut=5000;
    private String name;
    private InetAddress address;
    public ColorioClientSocket(InetAddress address, int port, String name) throws IOException {
        super(port);
        this.name=name;
        this.address=address;
        setSoTimeout(timeOut);
        ColorioClientHandshake handshake = new ColorioClientHandshake();
        handshake.start();
    }
    private class ColorioClientHandshake extends Thread{
        @Override
        public void run(){
            try {
                Handshake handshake = new Handshake("Test",0);
                Handshake handshakeResponse = new Handshake();
                DatagramPacket responsePacket = new DatagramPacket(new byte[1024],1024);
                send(handshake.toDatagramPacket(address,getPort()));
                while (true){
                    try {
                        receive(responsePacket);
                    } catch (SocketTimeoutException e) {
                        send(handshake.toDatagramPacket(address,getPort()));
                        continue;
                    }
                    if(handshakeResponse.getFromDatagramPacket(responsePacket)) break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private class ColorioClientSend extends Thread{
        @Override
        public void run(){

        }
    }
    private class ColorioClientRecieve extends Thread{
        @Override
        public void run(){

        }
    }
}
