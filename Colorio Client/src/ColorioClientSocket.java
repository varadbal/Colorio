import java.io.IOException;
import java.net.*;

public class ColorioClientSocket extends DatagramSocket {
    public static boolean isLoggerOn=true;
    private final int timeOut=5000;
    private String name;
    private InetAddress address;
    private int port;
    public ColorioClientSocket(InetAddress address, int port, String name) throws IOException {
        super(port);
        this.port = port;
        this.name=name;
        this.address=address;
        setSoTimeout(timeOut);
        ColorioClientHandshake handshake = new ColorioClientHandshake();
        handshake.start();
    }
    
    private void log(String string){
        if(isLoggerOn) System.out.println("ColorioClientSocket: " + string);
    }
    
    private class ColorioClientHandshake extends Thread{
        @Override
        public void run(){
            try {
                Handshake handshake = new Handshake(name,0);
                Handshake handshakeResponse = new Handshake();
                DatagramPacket responsePacket = new DatagramPacket(new byte[1024],1024);
                log("Sending handshake packet...");
                send(handshake.toDatagramPacket(address,port));
                while (true){
                    try {
                        log("Handshake sent. Waiting for response...");
                        receive(responsePacket);
                    } catch (SocketTimeoutException e) {
                        log("Response waiting timout(" + timeOut + "ms). Sending handshake packet again...");
                        send(handshake.toDatagramPacket(address,port));
                        continue;
                    }
                    if(handshakeResponse.getFromDatagramPacket(responsePacket)) {
                        log("Handshake response packet received, and successfully deserialized: " + handshakeResponse.toString());
                        break;
                    }
                    else {
                        log("Unknown response packet received. Sending handshake packet again...");
                        send(handshake.toDatagramPacket(address,port));
                    }
                }
                GameStatus initialStatus = new GameStatus();
                log("Sending back the received handshake...");
                send(handshakeResponse.toDatagramPacket(address,port));
                while (true){
                    try {
                        log("Handshake sent. Waiting for the initial game status...");
                        receive(responsePacket);
                    } catch (SocketTimeoutException e) {
                        log("Response waiting timout( " + timeOut + "ms). Sending th received handshake again...");
                        send(handshakeResponse.toDatagramPacket(address,port));
                        continue;
                    }
                    if(initialStatus.getFromDatagramPacket(responsePacket)){
                        log("Initial game status received, and successfully deserialized.");
                        break;
                    }
                    else {
                        log("Unknown response packet received. Sending handshake packet again...");
                        send(handshakeResponse.toDatagramPacket(address,port));
                    }
                }
                log("Successful handshake happened.");
                
            } catch (IOException e) {
                e.printStackTrace();
                close();
                log("Error while sending or receiving packets. Communication stopped");
            }
        }
    }
    private class ColorioClientSend extends Thread{
        @Override
        public void run(){
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private class ColorioClientreceive extends Thread{
        @Override
        public void run(){
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
