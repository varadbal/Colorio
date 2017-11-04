package ColorioClient;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class StartDialog extends JFrame{
    private JTextField ipTextField;
    private JPanel mainPanel;
    private JButton connectButton;
    private JTextField nameTextField;
    private JLabel messageLabel;

    public StartDialog(){
        setContentPane(mainPanel);
        setSize(180,175);
        setResizable(false);
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                InetAddress address;
                try {
                    address=InetAddress.getByName(ipTextField.getText());
                    ClientSocket clientSocket = new ClientSocket(address,nameTextField.getText(),messageLabel);
                    clientSocket.start();
                } catch (UnknownHostException e1) {
                    JOptionPane.showMessageDialog(null,"Invalid IP address");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
}
