package Common;

import ColorioClient.ClientSocket;
import ColorioCommon.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerClientTest {

    private final JLabel messageLabel = new JLabel();
    private DatagramSocket server = null;
    /**
     * Initialize tests
     * @throws IOException
     */
    @BeforeEach
    public void setUp() throws IOException {
       server = new DatagramSocket(Constants.serverPort);

    }

    @Test
    public void handshakeTest() throws IOException {
        /**
         * TODO start a server properly
         */

        /**
         * Starting a client properly
         */
        ClientSocket socket = new ClientSocket(InetAddress.getByName("localhost"),"test",messageLabel);
        socket.start();
        assertEquals("Connecting...", messageLabel.getText());

    }

}