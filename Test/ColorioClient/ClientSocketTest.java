package ColorioClient;

import ColorioCommon.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Client communication tester class
 */
class ClientSocketTest {

    private final JLabel messageLabel = new JLabel();
    private DatagramSocket server = null;
    DatagramPacket receivePacket = null;
    InetAddress address;
    ClientSocket socket;

    /**
     * Initialize tests
     * @throws IOException
     */
    @BeforeEach
    public void setUp() throws IOException {
        socket = new ClientSocket(InetAddress.getByName("localhost"),"test",messageLabel);
        server = new DatagramSocket(Constants.serverPort);
        server.setSoTimeout(Constants.responseTimeOut);
        receivePacket = new DatagramPacket(new byte[1024],1024);
        address = InetAddress.getByName("localhost");
    }

    /**
     * After tests
     */
    @AfterEach
    public void closeSockets(){
        server.close();
        socket.close();
    }

    /**
     * Handshake test
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void handshakeTest() throws IOException, InterruptedException {
        socket.start();
        assertEquals("Connecting...",messageLabel.getText());
        server.receive(receivePacket);
        Handshake init = (Handshake) UDPSerializable.getClassFromDatagramPacket(receivePacket);
        assertEquals(0,init.getId());
        server.send(new Handshake(init.getName(),5).toDatagramPacket(address,Constants.clientPort));
        server.receive(receivePacket);
        KeyStatus status = (KeyStatus)UDPSerializable.getClassFromDatagramPacket(receivePacket);
        assertEquals(5,status.getPlayerId());
        GameStatus status1 = new GameStatus();
        PlayerEntry playerEntry = new PlayerEntry(5,new Player(new Centroid(50,50,50,Color.BLUE),new Centroid(50,50,50,Color.BLUE),new Centroid(50,50,50,Color.BLUE),new Centroid(50,50,50,Color.BLUE)),"tester");
        status1.addPlayerEntry(playerEntry);
        server.send(status1.toDatagramPacket(address,Constants.clientPort));
        Thread.sleep(100);
        assertEquals(5,socket.getFrame().getPlayerID());
        assertEquals(50,socket.getFrame().getStatus().getPlayers().get(0).getPlayer().getTop().x);
        assertEquals("Connected",messageLabel.getText());
    }

    /**
     * Test for communication threads
     */
    @Test
    public void communicationTest() throws IOException, InterruptedException {
        /**
         * Handshake
         */
        socket.start();
        assertEquals("Connecting...",messageLabel.getText());
        server.receive(receivePacket);
        Handshake init = (Handshake) UDPSerializable.getClassFromDatagramPacket(receivePacket);
        assertEquals(0,init.getId());
        server.send(new Handshake(init.getName(),5).toDatagramPacket(address,Constants.clientPort));
        server.receive(receivePacket);
        KeyStatus status = (KeyStatus)UDPSerializable.getClassFromDatagramPacket(receivePacket);
        assertEquals(5,status.getPlayerId());
        GameStatus status1 = new GameStatus();
        PlayerEntry playerEntry = new PlayerEntry(5,new Player(new Centroid(50,50,50,Color.BLUE),new Centroid(50,50,50,Color.BLUE),new Centroid(50,50,50,Color.BLUE),new Centroid(50,50,50,Color.BLUE)),"tester");
        status1.addPlayerEntry(playerEntry);
        server.send(status1.toDatagramPacket(address,Constants.clientPort));
        Thread.sleep(100);
        assertEquals(5,socket.getFrame().getPlayerID());
        assertEquals(50,socket.getFrame().getStatus().getPlayers().get(0).getPlayer().getTop().x);
        assertEquals("Connected",messageLabel.getText());

        /**
         * Testing, if the received GameStatuses are handled correctly
         */
        PlayerEntry playerEntry2 = new PlayerEntry(5,new Player(new Centroid(150,50,50,Color.BLUE),new Centroid(50,50,50,Color.BLUE),new Centroid(50,50,50,Color.BLUE),new Centroid(50,50,50,Color.BLUE)),"tester");
        status1.addPlayerEntry(playerEntry2);
        server.send(status1.toDatagramPacket(address,Constants.clientPort));
        Thread.sleep(100);
        assertEquals(5,socket.getFrame().getPlayerID());
        assertEquals(150,socket.getFrame().getStatus().getPlayers().get(1).getPlayer().getTop().x);

        /**
         * Testing if the right KeyStatuses will be sent
         */
        server.receive(receivePacket);
        KeyStatus status2 = (KeyStatus) UDPSerializable.getClassFromDatagramPacket(receivePacket);
        assertEquals(false,status2.isAPressed());
        assertEquals(false,status2.isWPressed());
        assertEquals(false,status2.isSPressed());
        assertEquals(false,status2.isDPressed());

    }


    /**
     * Test for closing the communication by the server
     */
    @Test
    public void communicationClosedByServerTest() throws IOException, InterruptedException {
        /**
         * Handshake
         */
        socket.start();
        assertEquals("Connecting...",messageLabel.getText());
        server.receive(receivePacket);
        Handshake init = (Handshake) UDPSerializable.getClassFromDatagramPacket(receivePacket);
        assertEquals(0,init.getId());
        server.send(new Handshake(init.getName(),5).toDatagramPacket(address,Constants.clientPort));
        server.receive(receivePacket);
        KeyStatus status = (KeyStatus)UDPSerializable.getClassFromDatagramPacket(receivePacket);
        assertEquals(5,status.getPlayerId());
        GameStatus status1 = new GameStatus();
        PlayerEntry playerEntry = new PlayerEntry(5,new Player(new Centroid(50,50,50,Color.BLUE),new Centroid(50,50,50,Color.BLUE),new Centroid(50,50,50,Color.BLUE),new Centroid(50,50,50,Color.BLUE)),"tester");
        status1.addPlayerEntry(playerEntry);
        server.send(status1.toDatagramPacket(address,Constants.clientPort));
        Thread.sleep(100);
        assertEquals(5,socket.getFrame().getPlayerID());
        assertEquals(50,socket.getFrame().getStatus().getPlayers().get(0).getPlayer().getTop().x);
        assertEquals("Connected",messageLabel.getText());

        /**
         * Waiting a second
         */
        Thread.sleep(1000);

        /**
         * Communication close test
         */
        server.send(new Handshake(null,0).toDatagramPacket(address,Constants.clientPort));
        while (!(UDPSerializable.getClassFromDatagramPacket(receivePacket) instanceof Handshake)){
            server.receive(receivePacket);
        }

        Handshake closeHandshake = (Handshake) UDPSerializable.getClassFromDatagramPacket(receivePacket);
        assertEquals(0,closeHandshake.getId());
        assertEquals(null,closeHandshake.getName());
    }

    /**
     * Test for closing the communication by the client
     */
    @Test
    public void communicationClosedByClientTest() throws IOException, InterruptedException {
        /**
         * Handshake
         */
        socket.start();
        assertEquals("Connecting...",messageLabel.getText());
        server.receive(receivePacket);
        Handshake init = (Handshake) UDPSerializable.getClassFromDatagramPacket(receivePacket);
        assertEquals(0,init.getId());
        server.send(new Handshake(init.getName(),5).toDatagramPacket(address,Constants.clientPort));
        server.receive(receivePacket);
        KeyStatus status = (KeyStatus)UDPSerializable.getClassFromDatagramPacket(receivePacket);
        assertEquals(5,status.getPlayerId());
        GameStatus status1 = new GameStatus();
        PlayerEntry playerEntry = new PlayerEntry(5,new Player(new Centroid(50,50,50,Color.BLUE),new Centroid(50,50,50,Color.BLUE),new Centroid(50,50,50,Color.BLUE),new Centroid(50,50,50,Color.BLUE)),"tester");
        status1.addPlayerEntry(playerEntry);
        server.send(status1.toDatagramPacket(address,Constants.clientPort));
        Thread.sleep(100);
        assertEquals(5,socket.getFrame().getPlayerID());
        assertEquals(50,socket.getFrame().getStatus().getPlayers().get(0).getPlayer().getTop().x);
        assertEquals("Connected",messageLabel.getText());

        /**
         * Waiting a second
         */
        Thread.sleep(1000);

        /**
         * Communication close test
         */
        socket.stop();
        while (!(UDPSerializable.getClassFromDatagramPacket(receivePacket) instanceof Handshake)){
            server.receive(receivePacket);
        }

        Handshake closeHandshake = (Handshake) UDPSerializable.getClassFromDatagramPacket(receivePacket);
        assertEquals(5,closeHandshake.getId());
        assertEquals(null,closeHandshake.getName());
        server.send(new Handshake(null,0).toDatagramPacket(address,Constants.clientPort));
        Thread.sleep(100);
        assertEquals("Server disconnected",messageLabel.getText());
    }
}