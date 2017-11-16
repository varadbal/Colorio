package ColorioClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientSocketTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final JLabel messageLabel = new JLabel();

    @BeforeEach
    public void setUpStreams() throws IOException {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @Test
    public void startClientTest() throws IOException {
        ClientSocket socket = new ClientSocket(InetAddress.getByName("localhost"),"test",messageLabel);
        socket.start();
        assertEquals("Connecting...",messageLabel.getText());
    }

}