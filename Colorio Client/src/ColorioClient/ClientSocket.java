package ColorioClient;

import ColorioCommon.*;

import javax.swing.*;
import java.io.IOException;
import java.net.*;

public class ClientSocket extends DatagramSocket {
    public static boolean isLoggerOn=true;
    public static int defaultPort=12345;
    private String name;
    private InetAddress address;
    private int port;
    private ColorioFrame frame;
    JLabel messageLabel;

    /**
     *
     * @param address
     * @param name
     * @param messageLabel
     * @throws IOException
     */
    public ClientSocket(InetAddress address, String name, JLabel messageLabel) throws IOException {
        super(defaultPort);
        this.messageLabel = messageLabel;
        this.port = defaultPort;
        this.name=name;
        this.address=address;
        frame = new ColorioFrame(this);
        setSoTimeout(Constants.responseTimeOut);
    }

    /**
     *
     */
    public void start(){
        ColorioClientHandshake handshake = new ColorioClientHandshake();
        messageLabel.setText("Connecting...");
        handshake.start();
    }

    /**
     *
     * @param thread
     * @param message
     */
    private void log(String thread, String message){
        if(isLoggerOn) System.out.println("ClientSocket("+thread+"): " + message);
    }

    /**
     *
     */
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
                        log("handshake","Handshake sent. Waiting for response...");
                        receive(responsePacket);
                    } catch (SocketTimeoutException e) {
                        log("handshake","Response waiting timout(" + Constants.responseTimeOut + "ms). Sending handshake packet again...");
                        send(handshake.toDatagramPacket(address,port));
                        continue;
                    }
                    if(handshakeResponse.getFromDatagramPacket(responsePacket)) {
                        log("handshake","Handshake response packet received, and successfully deserialized: " + handshakeResponse.toString());
                        break;
                    }
                    else {
                        log("handshake","Wrong response packet received. Sending handshake packet again...");
                        send(handshake.toDatagramPacket(address,port));
                    }
                }
                GameStatus initialStatus = new GameStatus();
                log("handshake","Sending the initial KeyStatus...");
                send(frame.getKeyStatus().toDatagramPacket(address,port));
                while (true){
                    try {
                        log("handshake","Initial KeyStatus sent. Waiting for the initial game status...");
                        receive(responsePacket);
                    } catch (SocketTimeoutException e) {
                        log("handshake","Response waiting timeout( " + Constants.responseTimeOut + "ms). Sending the initial KeyStatus again...");
                        send(handshakeResponse.toDatagramPacket(address,port));
                        continue;
                    }
                    if(initialStatus.getFromDatagramPacket(responsePacket)){
                        log("handshake","Initial game status received, and successfully deserialized.");
                        break;
                    }
                    else {
                        log("handshake","Wrong response packet received. Sending handshake packet again...");
                        send(handshakeResponse.toDatagramPacket(address,port));
                    }
                }
                log("handshake","Successful handshake happened.");
                messageLabel.setText("Connected");
                frame.setVisible(true);
                log("handshake","Starting receive and send threads...");
                ReceiveThread receiveThread = new ReceiveThread();
                receiveThread.start();
                EventSendThread eventSendThread = new EventSendThread();
                eventSendThread.start();
                StatusSendThread statusSendThread = new StatusSendThread();
                statusSendThread.start();

            } catch (IOException e) {
                e.printStackTrace();
                close();
                log("handshake","Error while sending or receiving packets. Communication stopped.");
            }
        }
    }
    private class EventSendThread extends Thread{
        @Override
        public void run(){
            log("eventSend","EventSendthread started.");
            while (true){
                try {
                    log("eventSend","Waiting for a key event...");
                    KeyEvent keyEvent = frame.keyInput();
                    log("eventSend","Key event happened: "+keyEvent.toString());
                    log("eventSend","Sending key event...");
                    send(keyEvent.toDatagramPacket(address,port));
                    log("eventSend", "Key event sent.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class StatusSendThread extends Thread{
        @Override
        public void run() {
            log("statusSend","StatusSendThread started.");
            while (true){
                try {
                    log("statusSend","Sending KeyStatus...");
                    send(frame.getKeyStatus().toDatagramPacket(address,port));
                    log("statusSend","KeyStatus sent");
                    sleep(Constants.clientSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ReceiveThread extends Thread{
        @Override
        public void run(){
            log("ReceiveThread","ReceiveThread started.");
            DatagramPacket receivePacket = new DatagramPacket(new byte[1024],1024);
            while (true) {
                try {
                    log("ReceiveThread","Waiting for packet...");
                    receive(receivePacket);
                    log("ReceiveThread","Packet received.");
                    UDPSerializable receivedClass = UDPSerializable.getClassFromDatagramPacket(receivePacket);
                    if(receivedClass instanceof GameStatus){
                        frame.refreshGameStatus((GameStatus)receivedClass);
                    }
                    else {
                        if (receivedClass instanceof Handshake){
                            //Server wants to stop
                        }
                        else {
                            //error
                        }
                    }
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
