import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;

public class Main {
    public static void main(String[] args){
        /*ColorioFrame colorioFrame = new ColorioFrame();
        colorioFrame.setVisible(true);*/
        try {
            ColorioClientSocket clientSocket = new ColorioClientSocket(InetAddress.getByName("192.168.1.2"),12345, "Test");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
