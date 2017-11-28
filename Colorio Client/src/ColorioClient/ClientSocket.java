package ColorioClient;

import ColorioCommon.*;
import ColorioCommon.Exceptions.HandshakeFailedException;

import javax.swing.*;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.*;

import static ColorioCommon.Constants.minBufferSize;

/**
 * This class is responsible for the communication with th server
 */
public class ClientSocket extends DatagramSocket implements KeyListener {

    private static boolean isLoggerOn=true;
    public static int defaultPort=12345;
    private String name;
    private InetAddress address;
    private int sendPort;
    private ColorioFrame frame;
    JLabel messageLabel;
    private boolean wPressed;
    private boolean aPressed;
    private boolean sPressed;
    private boolean dPressed;
    private boolean serverDisconnected = false;

    /**
     * Constructor
     * @param address Address of the server
     * @param name Name of the client
     * @param messageLabel Label on the startdialog GUI
     * @throws IOException
     */
    public ClientSocket(InetAddress address, String name, JLabel messageLabel) throws IOException {
        super(Constants.clientPort);
        this.messageLabel = messageLabel;
        this.sendPort = Constants.serverPort;
        this.name=name;
        this.address=address;
        frame = new ColorioFrame(this);
        frame.addKeyListener(this);
        setSoTimeout(Constants.responseTimeOut);
    }

    /**
     *This function starts the communication
     */
    public void start(){
        ColorioClientHandshake handshake = new ColorioClientHandshake();
        messageLabel.setText("Connecting...");
        handshake.start();
    }

    /**
     * A simple logger function
     * @param thread name of the thread
     * @param message message to print
     */
    private void log(String thread, String message){
        if(isLoggerOn) System.out.println("ClientSocket("+thread+"): " + message);
    }

    @Override
    public void keyTyped(java.awt.event.KeyEvent e) {

    }

    /**
     * Key event handling method
     * @param e received event
     */
    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
        try {
            KeyEvent keyEvent = new KeyEvent(frame.getPlayerID(),e);
            log("eventSend","Key event happened: "+keyEvent.toString());
            log("eventSend","Sending key event...");
            send(keyEvent.toDatagramPacket(address, sendPort));
            log("eventSend", "Key event sent.");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if(e.getKeyChar()=='w'){
            wPressed=true;
        }
        if(e.getKeyChar()=='a'){
            aPressed=true;
        }
        if(e.getKeyChar()=='s'){
            sPressed=true;
        }
        if(e.getKeyChar()=='d'){
            dPressed=true;
        }

    }

    /**
     * Key event handling method
     * @param e received event
     */
    @Override
    public void keyReleased(java.awt.event.KeyEvent e) {
        try {
            KeyEvent keyEvent = new KeyEvent(frame.getPlayerID(),e);
            log("eventSend","Key event happened: "+keyEvent.toString());
            log("eventSend","Sending key event...");
            send(keyEvent.toDatagramPacket(address, sendPort));
            log("eventSend", "Key event sent.");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if(e.getKeyChar()=='w'){
            wPressed=false;
        }
        if(e.getKeyChar()=='a'){
            aPressed=false;
        }
        if(e.getKeyChar()=='s'){
            sPressed=false;
        }
        if(e.getKeyChar()=='d'){
            dPressed=false;
        }
    }

    /**
     * getter method
     * @return the actual keyboard status
     */
    public KeyStatus getKeyStatus(){
        return new KeyStatus(frame.getPlayerID(), wPressed,aPressed,sPressed,dPressed);
    }

    /**
     * Function to stop the server properly
     */
    public void stop(){
        frame.setVisible(false);
    }

    /**
     *The thread which does the handshake with the server
     */
    private class ColorioClientHandshake extends Thread{
        @Override
        public void run(){
            setName("ClientHandshakeThread");
            try {
                Handshake handshake = new Handshake(name,0);
                Handshake handshakeResponse = new Handshake();
                DatagramPacket responsePacket = new DatagramPacket(new byte[minBufferSize],minBufferSize);
                log("handshake","Sending handshake packet...");
                send(handshake.toDatagramPacket(address, sendPort));
                int failCounter=0;
                while (true){
                    try {
                        log("handshake","Handshake sent. Waiting for response...");
                        receive(responsePacket);
                    } catch (SocketTimeoutException e) {
                        log("handshake","Response waiting timout(" + Constants.responseTimeOut + "ms). Sending handshake packet again...");
                        send(handshake.toDatagramPacket(address, sendPort));
                        continue;
                    }
                    if(handshakeResponse.getFromDatagramPacket(responsePacket)) {
                        log("handshake","Handshake response packet received, and successfully deserialized: " + handshakeResponse.toString());
                        break;
                    }
                    else {
                        log("handshake","Wrong response packet received. Sending handshake packet again...");
                        send(handshake.toDatagramPacket(address, sendPort));
                    }
                    failCounter++;
                    if(failCounter>5){
                        throw new HandshakeFailedException();
                    }
                }
                GameStatus initialStatus = new GameStatus();
                frame.setPlayerID(handshakeResponse.getId());
                log("handshake","Sending the initial KeyStatus...");
                send(getKeyStatus().toDatagramPacket(address, sendPort));
                failCounter=0;
                while (true){
                    try {
                        log("handshake","Initial KeyStatus sent. Waiting for the initial game status...");
                        receive(responsePacket);
                    } catch (SocketTimeoutException e) {
                        log("handshake","Response waiting timeout( " + Constants.responseTimeOut + "ms). Sending the initial KeyStatus again...");
                        send(getKeyStatus().toDatagramPacket(address, sendPort));
                        continue;
                    }
                    if(initialStatus.getFromDatagramPacket(responsePacket)){
                        log("handshake","Initial game status received, and successfully deserialized.");
                        frame.refreshGameStatus(initialStatus);
                        break;
                    }
                    else {
                        log("handshake","Wrong response packet received. Sending initial KeyStatus again...");
                        send(getKeyStatus().toDatagramPacket(address, sendPort));
                    }
                    if (failCounter>5) {
                        throw new HandshakeFailedException();
                    }
                    failCounter++;
                }
                log("handshake","Successful handshake happened.");
                messageLabel.setText("Connected");
                frame.setVisible(true);
                log("handshake","Starting receive and send threads...");
                ReceiveThread receiveThread = new ReceiveThread();
                receiveThread.start();
                StatusSendThread statusSendThread = new StatusSendThread();
                statusSendThread.start();

            } catch (IOException e) {
                e.printStackTrace();
                close();
                log("handshake","Error while sending or receiving packets. Communication stopped.");
                messageLabel.setText("Unable to connect to server: communication failure");
            } catch (HandshakeFailedException e) {
                messageLabel.setText(("Unable to connect to server: server unreachable"));
            }
        }
    }

    /**
     * The thread which sends the status of the keyboard periodically
     */
    private class StatusSendThread extends Thread{
        @Override
        public void run() {
            setName("StatusSendThread");
            log("statusSend","StatusSendThread started.");
            while (frame.isVisible()){
                try {
                    //log("statusSend","Sending KeyStatus...");
                    send(getKeyStatus().toDatagramPacket(address, sendPort));
                    //log("statusSend","KeyStatus sent");
                    sleep(Constants.clientSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(!serverDisconnected){
                //user wants to disconnect
                int failCounter=0;
                while (true) {
                    try {
                        log("statusSend","Sending stopHandshake...");
                        send(new Handshake(null,frame.getPlayerID()).toDatagramPacket(address,sendPort));
                        log("statusSend","stopHandshake sent");
                        DatagramPacket receivePacket = new DatagramPacket(new byte[Constants.minBufferSize],Constants.minBufferSize);
                        log("statusSend","Waiting for stopHandshake response...");
                        receive(receivePacket);
                        Handshake stopHandshake = new Handshake();
                        if(stopHandshake.getFromDatagramPacket(receivePacket)){
                            if(stopHandshake.getId()==0&&stopHandshake.getName()==null){
                                log("statusSend","Connection successfully stopped");
                                break;
                            }
                            else {
                                log("statusSend","Invalid handshake packet received");
                                failCounter++;
                            }
                        }
                        else {
                            log("statusSend","Invalid packet received");
                            failCounter++;
                        }
                    } catch (SocketTimeoutException e){
                        log("statusSend","Waiting timeout");
                        failCounter++;
                        if(failCounter>Constants.connectionStopRepeatLimit){
                            log("statusSend","Limit reached, attempting stopped");
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            messageLabel.setText("Server disconnected");
            serverDisconnected = false;
            frame.dispose();
            close();
        }
    }


    /**
     * The thread, which is listening to the server
     */
    private class ReceiveThread extends Thread{
        @Override
        public void run(){
            setName("ReceiveThread");
            log("ReceiveThread","ReceiveThread started.");
            DatagramPacket receivePacket = new DatagramPacket(new byte[minBufferSize],minBufferSize);
            while (frame.isVisible()) {
                try {
                    //log("ReceiveThread","Waiting for packet...");
                    receive(receivePacket);
                    //log("ReceiveThread","Packet received.");
                    UDPSerializable receivedClass = UDPSerializable.getClassFromDatagramPacket(receivePacket);
                    if(receivedClass instanceof GameStatus){
                        frame.refreshGameStatus((GameStatus)receivedClass);
                    }
                    else {
                        if (receivedClass instanceof Handshake){
                            //Server wants to stop
                            Handshake stopHandshake = (Handshake)receivedClass;
                            if(stopHandshake.getId()==0&&stopHandshake.getName()==null){
                                send(new Handshake(null,0).toDatagramPacket(address,sendPort));
                                log("ReceiveThread", "Server wanted to stop, stopHandshake sent");
                                frame.setVisible(false);
                                messageLabel.setText("Server disconnected");
                                serverDisconnected =true;
                            }
                        }
                        else {
                            throw new ClassNotFoundException();
                        }
                    }
                }catch (SocketTimeoutException e){
                    log("ReceiveThread", "Server timeout, disconnected");
                    frame.setVisible(false);
                    messageLabel.setText("Server disconnected");
                    serverDisconnected =true;
                } catch (IOException e) {
                    frame.setVisible(false);
                    messageLabel.setText("Server disconnected");
                    serverDisconnected =true;
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    log("ReceiveThread", "Unknown class received");
                }
            }
        }
    }

    /**
     * Tester method
     */
    public ColorioFrame getFrame() {
        return frame;
    }
}
