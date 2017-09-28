import javax.swing.*;
import java.awt.image.BufferedImage;

public class ColorioFrame extends JFrame {
    ColorioFrame(){
        super("Colorio");
        setSize(800,600);
    }
    public void drawBufferedImage(BufferedImage bufferedImage){
        add(new JLabel(new ImageIcon(bufferedImage)));
        pack();
    }
}
