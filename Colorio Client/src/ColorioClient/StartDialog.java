package ColorioClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This class is the opening window of the client
 */
public class StartDialog extends JFrame implements ComponentListener{
    private JTextField ipTextField;
    private JPanel mainPanel;
    private JButton connectButton;
    private JTextField nameTextField;
    private JLabel messageLabel;
    private ClientSocket clientSocket;

    /**
     * Constructor
     */
    public StartDialog(){
        addComponentListener(this);
        setContentPane(mainPanel);
        setSize(280,175);
        setResizable(false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        try {
            setIconImage(ImageIO.read(new java.io.File("colorioIcon.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InetAddress address;
                try {
                    address=InetAddress.getByName(ipTextField.getText());
                    clientSocket = new ClientSocket(address,nameTextField.getText(),messageLabel);
                    clientSocket.start();
                } catch (UnknownHostException e1) {
                    JOptionPane.showMessageDialog(null,"Invalid IP address");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    @Override
    public void componentResized(ComponentEvent e) {

    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    /**
     * This It stops the program correctly
     * @param e the received event
     */
    @Override
    public void componentHidden(ComponentEvent e) {
        clientSocket.stop();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        clientSocket.close();
        System.exit(0);
    }
}
