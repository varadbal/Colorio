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

/**
 * This class realizes the main window of the game, it is drawing the rendered frames
 */
public class ColorioFrame extends JFrame implements Runnable, ComponentListener{
    public static int width = 800;
    public static int height = 600;
    private boolean isRenderingActive = false;
    private Render render;
    private GameStatus status;
    private int playerID=-1;
    private long ref;
    private long partRef;
    private JLabel pictureHolder;
    Thread runOn;
    ClientSocket clientSocket;

    /**
     * Constructor
     * @param clientSocket the ClientSocket, which communicates with the server
     */
    ColorioFrame(ClientSocket clientSocket){
        super("Colorio");
        this.clientSocket = clientSocket;
        setSize(width,height);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        //addKeyListener(this);
        addComponentListener(this);
        pictureHolder=new JLabel();
        add(pictureHolder);
        pack();
        status=new GameStatus();
        render = new Render(width,height,status);
        render.render();
        drawBufferedImage(render);
    }

    public int getPlayerID(){return playerID;}

    /**
     * Draws the given BufferedImage in the window
     * @param bufferedImage the image to draw
     */
    private void drawBufferedImage(BufferedImage bufferedImage){
        pictureHolder.setIcon(new ImageIcon(bufferedImage));
        pack();
    }

    /**
     * setter method
     * @param playerID
     */
    public void setPlayerID(int playerID){
        this.playerID = playerID;
    }

    /**
     * it starts simple timer to calculate average fps
     */
    private void timerStart(){
        ref=System.nanoTime();
        partRef=ref;
        System.out.println("---Timer started---");
    }

    /**
     * it prints out the measured time
     * @param str message for the measured time
     * @param i the number of the rendered frames since timer started
     */
    private void timerRecord(String str, int i){
        long act= System.nanoTime();
        System.out.println(str+": "+(act-partRef)+"ns  --  "+(act-ref)+"ns\nAverage FPS: "+ 1000000000.0/(act-ref)*(double)i+" Actual FPS: " + 1000000000.0/(act-partRef)*20);
        partRef=act;
        System.out.flush();
    }

    /**
     * it refreshes the status of the game
     * @param status
     */
    public void refreshGameStatus(GameStatus status){
        this.status=status;
        int s=5;
    }

    /**
     * overridden method of thread, it does the rendering periodically
     */
    @Override
    public void run() {
        int i=1;
        //Centroid c = status.getCentroids().get(0);
        timerStart();
        while (isRenderingActive) {
            /*if(wPressed) {
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
            }*/
            render=new Render(width,height,status);
            render.render();
            if(i%20==0) {
                timerRecord("render", i);
            }
            drawBufferedImage(render);
            i++;
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {

    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    /**
     * overridden method, it will be called, if the window appears on the screen, it starts the rendering
     * @param e received event
     */
    @Override
    public void componentShown(ComponentEvent e) {
        runOn=new Thread(this);
        isRenderingActive = true;
        runOn.start();
    }

    /**
     * overridden method, it will be called, if the window disappears from the screen, it stops the rendering
     * @param e received event
     */
    @Override
    public void componentHidden(ComponentEvent e) {
        isRenderingActive=false;
    }


    public GameStatus getStatus() {
        return status;
    }
}
