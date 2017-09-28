import java.awt.*;
import java.awt.image.BufferedImage;

public class Main {
    public static void main(String[] args){
        ColorioFrame colorioFrame = new ColorioFrame();
        ColorioRender render = new ColorioRender(800,600, new GameStatus());
        colorioFrame.drawBufferedImage(render);
        render.render();
        colorioFrame.setVisible(true);
    }
}
