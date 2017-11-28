package ColorioClient;

import ColorioCommon.Centroid;
import ColorioCommon.GameStatus;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * This class renders the frames
 */
public class Render extends BufferedImage{
    private GameStatus status;
    private static final double sigmoidStep=0.0005;
    private static final int sigmoidTableSize=32768;
    private final double whiteLimit=0.01;
    private final double renderRange=3.0;
    private final double minRenderRange=30;
    private static double sigmoidCache[];
    private static boolean isSigmoidCacheFilled=false;
    private Graphics2D graphics2D;

    /**
     * Constructor
     * @param width width of the image to render
     * @param height height of the image to render
     * @param status the status of the game to render
     */
    public Render(int width, int height, GameStatus status) {
        super(width, height, BufferedImage.TYPE_INT_RGB);
        this.status =status;
        this.graphics2D  = createGraphics();
        if(!isSigmoidCacheFilled){
            sigmoidCache=new double[sigmoidTableSize];
            for (int i=0;i<sigmoidTableSize;i++){
                sigmoidCache[i]=sigmoid(i*sigmoidStep);
            }
            isSigmoidCacheFilled=true;
        }
    }

    /**
     * This is a sigmoid function
     * @param x the value
     * @return sigmoid value od x
     */
    private static double sigmoid(double x)
    {
        return 1 / (1 + Math.exp(-x));
    }

    /**
     * This is a sigmoid function, accelerated with cache table
     * @param x the value
     * @return sigmoid value od x
     */
    private double cacheSigmoid(double x){
        int q = (int) (x/sigmoidStep);
        if(q>(sigmoidTableSize-1)) return 1;
        else {
            return sigmoidCache[q];
        }
    }

    /**
     * This function renders the frame from gameStatus
     */
    public void render(){
        //System.out.println(status.toString());
        ArrayList<Centroid> centroids = status.getPlayerCentroids();
        double screen[][] = new double[ColorioFrame.width][ColorioFrame.height];
        double screenRed[][] = new double[ColorioFrame.width][ColorioFrame.height];
        double screenGreen[][] = new double[ColorioFrame.width][ColorioFrame.height];
        double screenBlue[][] = new double[ColorioFrame.width][ColorioFrame.height];/*
        for(Centroid centroid : centroids){
            int startPointX = (int) (centroid.getX()-minRenderRange/2);
            int endPointX = (int) (startPointX+minRenderRange);
            int startPointY = (int) (centroid.getY()-minRenderRange/2);
            int endPointY = (int) (startPointY+minRenderRange);
            if (centroid.getWeight()*renderRange>minRenderRange) {
                startPointX = (int) (centroid.getX()-centroid.getWeight()*renderRange);
                endPointX = (int) (startPointX + 2*centroid.getWeight()*renderRange);
            }
            if(startPointX<0) startPointX=0;
            if(endPointX>getWidth()) endPointX = getWidth();
            if(startPointY<0) startPointY=0;
            if(endPointY>getHeight()) endPointY=getHeight();
            for (int i=startPointX;i<endPointX;i++){
                for (int j=startPointY;j<endPointY;j++){
                    double intensity = (2.0/((i-centroid.getX())*(i-centroid.getX())+(j-centroid.getY())*(j-centroid.getY()))*centroid.getWeight());
                    screen[i][j]+=intensity;
                    screenRed[i][j]+=intensity*(1-centroid.color.getRed()/255.0);
                    screenGreen[i][j]+=intensity*(1-centroid.color.getGreen()/255.0);
                    screenBlue[i][j]+=intensity*(1-centroid.color.getBlue()/255.0);
                }
            }
        }*/
        for (int i=0;i<getWidth();i++){
            for (int j=0;j<getHeight();j++){
                for (Centroid centroid : centroids) {
                    double intensity = (2.0 / ((i - centroid.getX()) * (i - centroid.getX()) + (j - centroid.getY()) * (j - centroid.getY())) * centroid.getWeight());
                    screen[i][j] += intensity;
                    screenRed[i][j] += intensity * (1 - centroid.color.getRed() / 255.0);
                    screenGreen[i][j] += intensity * (1 - centroid.color.getGreen() / 255.0);
                    screenBlue[i][j] += intensity * (1 - centroid.color.getBlue() / 255.0);
                }
            }
        }

        for (int i=0;i<getWidth();i++) {
            for (int j = 0; j < getHeight(); j++) {
                if (screen[i][j]>whiteLimit) {
                    //double intensity=((cacheSigmoid(screen[i][j])-0.5))*2;
                    int red=255 - (int) (((cacheSigmoid(screenRed[i][j])-0.5))*2*255);
                    int green=255 - (int) (((cacheSigmoid(screenGreen[i][j])-0.5))*2*255);
                    int blue=255 - (int) (((cacheSigmoid(screenBlue[i][j])-0.5))*2*255);
                    int rgb = red;
                    rgb = (rgb << 8) + green;
                    rgb = (rgb << 8) + blue;
                    setRGB(i,j,rgb);
                }
                else setRGB(i,j,0xFFFFFF);
            }
        }

        ArrayList<Centroid> foods = status.getFoods();

        for (Centroid food : foods){
            graphics2D.setColor(food.getColor());
            int weightInt=(int)(food.getWeight()/10);
            graphics2D.fillOval((int)food.getX(),(int)food.getY(),weightInt, weightInt);
        }
    }
}
