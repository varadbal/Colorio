package ColorioClient;

import ColorioCommon.KeyEvent;
import ColorioCommon.GameStatus;
import ColorioCommon.Handshake;

import javax.swing.*;
import java.io.IOException;
import java.net.*;

public class ClientSocket extends DatagramSocket {
    public static boolean isLoggerOn=true;
    public static int defaultPort=12345;
    private final int timeOut=5000;
    private String name;
    private InetAddress address;
    private int port;
    private ColorioFrame frame;
    JLabel messageLabel;
    public ClientSocket(InetAddress address, String name, JLabel messageLabel) throws IOException {
        super(defaultPort);
        this.messageLabel = messageLabel;
        this.port = defaultPort;
        this.name=name;
        this.address=address;
        frame = new ColorioFrame(this);
        setSoTimeout(timeOut);
    }

    public void start(){
        ColorioClientHandshake handshake = new ColorioClientHandshake();
        messageLabel.setText("Connecting...");
        handshake.start();
    }
    
    private void log(String thread, String message){
        if(isLoggerOn) System.out.println("ClientSocket("+thread+"): " + message);
    }
    
    private class ColorioClientHandshake extends Thread{
        @Override
        public void run(){
            try {
                Handshake handshake = new Handshake(name,0);
                Handshake handshakeResponse = new Handshake();
                DatagramPacket responsePacket = new DatagramPacket(new byte[1024],1024);
                log("handshake","Sending handshake packet...");
                send(handshake.toDatagramPacket(address,port));
                while (true){
                    try {
                        log("handshake","ColorioCommon.Handshake sent. Waiting for response...");
                        receive(responsePacket);
                    } catch (SocketTimeoutException e) {
                        log("handshake","Response waiting timout(" + timeOut + "ms). Sending handshake packet again...");
                        send(handshake.toDatagramPacket(address,port));
                        continue;
                    }
                    if(handshakeResponse.getFromDatagramPacket(responsePacket)) {
                        log("handshake","ColorioCommon.Handshake response packet received, and successfully deserialized: " + handshakeResponse.toString());
                        break;
                    }
                    else {
                        log("handshake","Unknown response packet received. Sending handshake packet again...");
                        send(handshake.toDatagramPacket(address,port));
                    }
                }
                GameStatus initialStatus = new GameStatus();
                log("handshake","Sending back the received handshake...");
                send(handshakeResponse.toDatagramPacket(address,port));
                while (true){
                    try {
                        log("handshake","ColorioCommon.Handshake sent. Waiting for the initial game status...");
                        receive(responsePacket);
                    } catch (SocketTimeoutException e) {
                        log("handshake","Response waiting timout( " + timeOut + "ms). Sending th received handshake again...");
                        send(handshakeResponse.toDatagramPacket(address,port));
                        continue;
                    }
                    if(initialStatus.getFromDatagramPacket(responsePacket)){
                        log("handshake","Initial game status received, and successfully deserialized.");
                        break;
                    }
                    else {
                        log("handshake","Unknown response packet received. Sending handshake packet again...");
                        send(handshakeResponse.toDatagramPacket(address,port));
                    }
                }
                log("handshake","Successful handshake happened.");
                messageLabel.setText("Connected");
                frame.setVisible(true);
                log("handshake","Starting receive and send threads...");
                ColorioClientReceive receiveThread = new ColorioClientReceive();
                receiveThread.start();
                ColorioClientSend sendThread = new ColorioClientSend();
                sendThread.start();

            } catch (IOException e) {
                e.printStackTrace();
                close();
                log("handshake","Error while sending or receiving packets. Communication stopped.");
            }
        }
    }
    private class ColorioClientSend extends Thread{
        @Override
        public void run(){
            log("send","Send-thread started.");
            while (true){
                try {
                    log("send","Waiting for a key event...");
                    KeyEvent keyEvent = frame.keyInput();
                    log("send","Key event happened: "+keyEvent.toString());
                    log("send","Sending key event...");
                    send(keyEvent.toDatagramPacket(address,port));
                    log("send", "Key event sent.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private class ColorioClientReceive extends Thread{
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
