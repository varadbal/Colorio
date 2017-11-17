package ColorioClient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class StartDialog extends JFrame implements ComponentListener{
    private JTextField ipTextField;
    private JPanel mainPanel;
    private JButton connectButton;
    private JTextField nameTextField;
    private JLabel messageLabel;
    private ClientSocket clientSocket;

    public StartDialog(){
        addComponentListener(this);
        setContentPane(mainPanel);
        setSize(280,175);
        setResizable(false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
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

    @Override
    public void componentHidden(ComponentEvent e) {
        clientSocket.stop();
        dispose();
    }
}
