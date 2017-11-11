package ColorioClient;
import ColorioCommon.Centroid;
import ColorioCommon.KeyEvent;
import ColorioCommon.GameStatus;
import ColorioCommon.KeyStatus;

import javax.swing.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class ColorioFrame extends JFrame implements KeyListener, Runnable, ComponentListener{
    public static int width = 800;
    public static int height = 600;
    private Render render;
    private GameStatus status;
    public boolean wPressed = false;
    public boolean aPressed = false;
    public boolean sPressed = false;
    public boolean dPressed = false;
    private long ref;
    private long partRef;
    private JLabel pictureHolder;
    private java.awt.event.KeyEvent keyEvent;
    Thread runOn;
    ClientSocket clientSocket;

    ColorioFrame(ClientSocket clientSocket){
        super("Colorio");
        this.clientSocket = clientSocket;
        setSize(width,height);
        addKeyListener(this);
        pictureHolder=new JLabel();
        add(pictureHolder);
        pack();
        status=new GameStatus();
        render = new Render(width,height,status,this);
        render.render();
        drawBufferedImage(render);
    }
    public void drawBufferedImage(BufferedImage bufferedImage){
        pictureHolder.setIcon(new ImageIcon(bufferedImage));
        pack();
    }

    @Override
    public void keyTyped(java.awt.event.KeyEvent e) {

    }

    @Override
    public void keyPressed(java.awt.event.KeyEvent e) {
        keyEvent=e;
        try {
            notify();
        } catch (IllegalMonitorStateException e1) {
            System.out.println("there is no thread to listen to the keyinput!");
        }
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
    public void keyReleased(java.awt.event.KeyEvent e) {
        keyEvent=e;
        try {
            notify();
        } catch (IllegalMonitorStateException e1) {
            System.out.println("there is no thread to listen to the keyinput!");
        }
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


    public KeyEvent keyInput(){
        try {
            wait();
            return new KeyEvent(1, keyEvent); //TODO provide proper playerId
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
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

    public KeyStatus getKeyStatus(){
        return new KeyStatus(1, wPressed,aPressed,sPressed,dPressed); //TODO provide proper playerId
    }

    public void refreshGameStatus(GameStatus status){
        this.status=status;
    }

    @Override
    public void run() {
        int i=1;
        Centroid c = status.getCentroids().get(0);
        timerStart();
        while (isVisible()) {
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
            render=new Render(width,height,status,this);
            render.render();
            if(i%20==0) {
                timerRecord("render", i);
            }
            drawBufferedImage(render);
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {

    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {
        runOn=new Thread(this);
        runOn.start();
    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}
