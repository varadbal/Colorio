import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class ColorioFrame extends JFrame implements KeyListener, Runnable{
    public static int width = 800;
    public static int height = 600;
    ColorioRender render;
    GameStatus status;
    public boolean wPressed = false;
    public boolean aPressed = false;
    public boolean sPressed = false;
    public boolean dPressed = false;
    private long ref;
    private long partRef;
    JLabel pictureHolder;

    ColorioFrame(){
        super("Colorio");
        setSize(width,height);
        addKeyListener(this);
        pictureHolder=new JLabel();
        add(pictureHolder);
        pack();
        status=new GameStatus();
        render = new ColorioRender(width,height,status,this);
        render.render();
        drawBufferedImage(render);
        Thread t = new Thread(this);
        t.start();
    }
    public void drawBufferedImage(BufferedImage bufferedImage){
        pictureHolder.setIcon(new ImageIcon(bufferedImage));
        pack();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyChar()=='w'){
            wPressed=true;
        }
        if(e.getKeyChar()=='a'){
            aPressed=true;
        }
        if(e.getKeyChar()=='s'){
            sPressed=true;
        }
        if(e.getKeyChar()=='d'){
            dPressed=true;
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyChar()=='w'){
            wPressed=false;
        }
        if(e.getKeyChar()=='a'){
            aPressed=false;
        }
        if(e.getKeyChar()=='s'){
            sPressed=false;
        }
        if(e.getKeyChar()=='d'){
            dPressed=false;
        }
    }



    void timerStart(){
        ref=System.nanoTime();
        partRef=ref;
        System.out.println("---Timer started---");
    }

    void timerRecord(String str, int i){
        long act= System.nanoTime();
        System.out.println(str+": "+(act-partRef)+"ns  --  "+(act-ref)+"ns\nAverage FPS: "+ 1000000000.0/(act-ref)*(double)i+" Actual FPS: " + 1000000000.0/(act-partRef)*20);
        partRef=act;
        System.out.flush();
    }

    @Override
    public void run() {
        int i=1;
        Centroid c = status.getCentroids().get(0);
        timerStart();
        while (true) {
            //try {
            if(wPressed) {
                if(c.y>0) c.setLocation(c.x,c.y-1);
            }
            if(aPressed) {
                if(c.x>0) c.setLocation(c.x-1,c.y);
            }
            if(sPressed) {
                if(c.y<height) c.setLocation(c.x,c.y+1);
            }
            if(dPressed) {
                if(c.x<width) c.setLocation(c.x + 1, c.y);
            }
            render=new ColorioRender(width,height,status,this);
            render.render();
            if(i%20==0) {
                timerRecord("render", i);
            }
            drawBufferedImage(render);
            if(i%20==0) {
                timerRecord("draw",i);
            }
            i++;
            /*} catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
    }
}
