package ColorioServer;

import ColorioCommon.*;
import com.sun.prism.paint.Color;
import org.junit.jupiter.api.*;

import javax.swing.*;
import javax.xml.crypto.Data;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.awt.event.KeyEvent.*;

import static ColorioCommon.Constants.minBufferSize;
import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerTest {

    private DatagramSocket sock = null;
    private Server serv = null;
    private GameLogic gl = null;
    private int commTimeOut = 5000;

    @BeforeEach
    public void setUpSocketAndServer() throws SocketException{
        sock = new DatagramSocket(Constants.clientPort);
        sock.setSoTimeout(commTimeOut);

        ConcurrentMap<Integer, Client> clients = new ConcurrentHashMap<>();
        BlockingQueue<OutPacket> toSend = new LinkedBlockingQueue<>();
        BlockingQueue<KeyInput> toHandle = new LinkedBlockingQueue<>();

        serv = new Server(toSend, clients, toHandle);
        gl = new GameLogic(clients, toSend, toHandle);

        serv.start();
        gl.start();
    }

    @Test
    /**
     * Trivial (expected) (opening-)Handshake-response-tests
     */
    public void HandshakeResponseTest() throws IOException {
        String name1 = "Number1";
        String name2 = "Example";

        /*First handshake - normal*/
        DatagramPacket hs = new Handshake(name1, 0).toDatagramPacket(InetAddress.getByName("localhost"), Constants.serverPort);
        sock.send(hs);
        DatagramPacket rec = new DatagramPacket(new byte[10240], 10240);
        sock.receive(rec);
        Handshake recHs = (Handshake) UDPSerializable.getClassFromDatagramPacket(rec);

        Assertions.assertEquals(name1, recHs.getName());
        Assertions.assertEquals(1, recHs.getId());

        /*Second handshake - normal*/
        hs = new Handshake(name2, 0).toDatagramPacket(InetAddress.getByName("localhost"), Constants.serverPort);
        sock.send(hs);
        rec = new DatagramPacket(new byte[10240], 10240);
        sock.receive(rec);
        recHs = (Handshake) UDPSerializable.getClassFromDatagramPacket(rec);

        Assertions.assertEquals(name2, recHs.getName());
        Assertions.assertEquals(2, recHs.getId());

        /*Third handshake - same name as the second one*/
        hs = new Handshake(name2, 0).toDatagramPacket(InetAddress.getByName("localhost"), Constants.serverPort);
        sock.send(hs);
        rec = new DatagramPacket(new byte[10240], 10240);
        sock.receive(rec);
        recHs = (Handshake) UDPSerializable.getClassFromDatagramPacket(rec);

        Assertions.assertEquals(name2, recHs.getName());
        Assertions.assertEquals(3, recHs.getId());

    }

    @Test
    /**
     * doHandshake()
     * Waits for a following GameStatus (check)
     * Sends a KeyEvent (and checks the following (5) GameStatuses)
     * Sends a conflicting KeyStatus (checks if updated correctly)
     */
    public void ConnectTest() throws IOException {
        doHandshake();

        //Another GameStatus
        DatagramPacket rec = new DatagramPacket(new byte[10240], 10240);
        sock.receive(rec);
        GameStatus recGs = (GameStatus) UDPSerializable.getClassFromDatagramPacket(rec);

        Assertions.assertNotNull(recGs.getPlayers().get(0));

        //Send KeyEvent
        Centroid initPlTop = recGs.getPlayers().get(0).getPlayer().getTop();
        ColorioCommon.KeyEvent ke = new KeyEvent(1, new java.awt.event.KeyEvent(new java.awt.Button(),
                java.awt.event.KeyEvent.KEY_PRESSED, Instant.now().toEpochMilli(), 0,
                java.awt.event.KeyEvent.VK_W, 'w'));
        DatagramPacket kep = ke.toDatagramPacket(InetAddress.getByName("localhost"), Constants.serverPort);
        sock.send(kep);

        boolean movedCorrectly = false;
        Centroid newPlTop = null;
        for(int i = 0; i < 5; ++i) {
            rec = new DatagramPacket(new byte[10240], 10240);
            sock.receive(rec);
            recGs = (GameStatus) UDPSerializable.getClassFromDatagramPacket(rec);
            newPlTop = recGs.getPlayers().get(0).getPlayer().getTop();

            if( (newPlTop.getY() < initPlTop.getY()) &&  (newPlTop.getX() - initPlTop.getX() < 0.01)){
                movedCorrectly = true;
                break;
            }
        }
        Assertions.assertTrue(movedCorrectly);

        //Send a conflicting KeyStatus
        if(newPlTop == null){//Don't cry compiler, at this point it should be initialized anyways
            newPlTop = new Centroid(1.0, 1.0, Constants.startingWeight, new java.awt.Color(255, 255, 255));
        }

        DatagramPacket cks = new KeyStatus(1, false, false, true, true)
                .toDatagramPacket(InetAddress.getByName("localhost"), Constants.serverPort);
        sock.send(cks);
        rec = new DatagramPacket(new byte[10240], 10240);
        sock.receive(rec);
        recGs = (GameStatus) UDPSerializable.getClassFromDatagramPacket(rec);

        Centroid newestPlTop;
        movedCorrectly = false;
        for(int i = 0; i < 5; ++i) {
            rec = new DatagramPacket(new byte[10240], 10240);
            sock.receive(rec);
            recGs = (GameStatus) UDPSerializable.getClassFromDatagramPacket(rec);
            newestPlTop = recGs.getPlayers().get(0).getPlayer().getTop();

            if( (newestPlTop.getY() > newPlTop.getY()) &&  (newestPlTop.getX() > newPlTop.getX())){
                movedCorrectly = true;
                break;
            }
        }
        //Assertions.assertTrue(movedCorrectly);

    }

    @Test
    /**
     * Testing Inconsistent Handshake
     *  Send a handshake(check)
     *  Send KeyStatus with wrong id
     *  Wait for GameStatus (check for timeOut)
     */
    public void BadHandshakeTest() throws IOException{
        //Handshake
        DatagramPacket hs = new Handshake("Player1", 0).toDatagramPacket(InetAddress.getByName("localhost"), Constants.serverPort);
        sock.send(hs);
        DatagramPacket rec = new DatagramPacket(new byte[10240], 10240);
        sock.receive(rec);
        Handshake recHs = (Handshake) UDPSerializable.getClassFromDatagramPacket(rec);

        Assertions.assertEquals("Player1", recHs.getName());
        Assertions.assertEquals(1, recHs.getId());

        //Initial KeyStatus
        DatagramPacket iks = new KeyStatus(2, false, false, false, false)
                .toDatagramPacket(InetAddress.getByName("localhost"), Constants.serverPort);
        sock.send(iks);
        final DatagramPacket recp = new DatagramPacket(new byte[10240], 10240);

        Assertions.assertThrows(SocketTimeoutException.class, ()->{
            sock.receive(recp);
        });


    }

    @Test
    /**
     * Checks TimeOut of Clients
     *  Full Handshake(check)
     *  Then no KeyStatus for a time
     *  Wait for GameStatus(check for timeOut)
     */
    public void NoPingTest() throws IOException, InterruptedException{
        doHandshake();
        DatagramPacket rec = new DatagramPacket(new byte[minBufferSize], minBufferSize);

        //No KeyStatus for some time
        boolean timedOut = false;
        long begin = Instant.now().toEpochMilli();
        while(Instant.now().toEpochMilli() - begin < Constants.responseTimeOut+1000){
            try {
                sock.receive(rec);
            }catch (SocketTimeoutException e){
                timedOut = true;
                break;
            }
        }

        Assertions.assertTrue(timedOut);

    }

    @Test
    /**
     * Checks Correct Disconnect Protocol
     *  Full Handshake(check)
     *  Disconnects from the Server (check Handshake)
     *  Wait for response(check for timeOut)
     * TODO may fail because of checking only one incoming
     */
    public void DisconnectTest() throws IOException{
        doHandshake();

        DatagramPacket hs;
        DatagramPacket rec;
        Handshake recHs;
        //Disconnect
        hs = new Handshake(null, 1).toDatagramPacket(InetAddress.getByName("localhost"), Constants.serverPort);
        sock.send(hs);
        rec = new DatagramPacket(new byte[10240], 10240);
        sock.receive(rec);
        recHs = (Handshake) UDPSerializable.getClassFromDatagramPacket(rec);

        Assertions.assertEquals(null, recHs.getName());
        Assertions.assertEquals(0, recHs.getId());

        final DatagramPacket recp = new DatagramPacket(new byte[10240], 10240);
        Assertions.assertThrows(SocketTimeoutException.class, ()->{
            sock.receive(recp);
        });
    }

    @Test
    public void CorrectPlayerObjectTest() throws IOException{
        doHandshake();

        DatagramPacket rec = new DatagramPacket(new byte[minBufferSize], minBufferSize);
        sock.receive(rec);
        GameStatus gs = new GameStatus();
        gs.getFromDatagramPacket(rec);

        PlayerEntry myPlayer = gs.getPlayers().get(0);

        //Objects Exist
        Assertions.assertEquals(1, myPlayer.getPlayerId());
        Assertions.assertNotNull(myPlayer.getName());
        Assertions.assertNotNull(myPlayer.getPlayer());
        Assertions.assertNotNull(myPlayer.getPlayer().getTop());
        Assertions.assertNotNull(myPlayer.getPlayer().getBottom());
        Assertions.assertNotNull(myPlayer.getPlayer().getRight());
        Assertions.assertNotNull(myPlayer.getPlayer().getLeft());

        //Objects have the right values

        Assertions.assertFalse(abs(myPlayer.getPlayer().getBottom().y - myPlayer.getPlayer().getTop().y) < 0.01);
        Assertions.assertTrue(abs(myPlayer.getPlayer().getTop().x - myPlayer.getPlayer().getBottom().x) < 0.01);
        Assertions.assertTrue(abs(myPlayer.getPlayer().getRight().y - myPlayer.getPlayer().getLeft().y)  < 0.01);
        Assertions.assertFalse(abs(myPlayer.getPlayer().getRight().x - myPlayer.getPlayer().getLeft().x) < 0.01);
        Assertions.assertEquals(myPlayer.getPlayer().getTop().getWeight(), myPlayer.getPlayer().getBottom().getWeight());
        Assertions.assertEquals(myPlayer.getPlayer().getTop().getWeight(), myPlayer.getPlayer().getRight().getWeight());
        Assertions.assertEquals(myPlayer.getPlayer().getTop().getWeight(), myPlayer.getPlayer().getLeft().getWeight());

    }

    /**
     * Correct Handshake Procedure with Assertions
     */
    public void doHandshake() throws IOException{
        //Handshake
        DatagramPacket hs = new Handshake("Player1", 0).toDatagramPacket(InetAddress.getByName("localhost"), Constants.serverPort);
        sock.send(hs);
        DatagramPacket rec = new DatagramPacket(new byte[10240], 10240);
        sock.receive(rec);
        Handshake recHs = (Handshake) UDPSerializable.getClassFromDatagramPacket(rec);

        Assertions.assertEquals("Player1", recHs.getName());
        Assertions.assertEquals(1, recHs.getId());

        //Initial KeyStatus
        DatagramPacket iks = new KeyStatus(1, false, false, false, false)
                .toDatagramPacket(InetAddress.getByName("localhost"), Constants.serverPort);
        sock.send(iks);
        rec = new DatagramPacket(new byte[10240], 10240);
        sock.receive(rec);
        GameStatus recGs = (GameStatus) UDPSerializable.getClassFromDatagramPacket(rec);

        Assertions.assertNotNull(recGs.getPlayers()/*.get(0).getPlayer()*/);
    }

    @AfterEach
    public void closeSocketAndServer() throws InterruptedException{

        if(gl != null){
            gl.stop();
        }

        if(serv != null) {
            serv.stop();
        }

        if(sock != null) {
            sock.close();
        }
    }

}