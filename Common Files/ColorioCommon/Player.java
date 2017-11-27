package ColorioCommon;

import java.io.Serializable;

import static ColorioCommon.Constants.baseSpeed;
import static ColorioCommon.Constants.mapMaxX;
import static ColorioCommon.Constants.mapMaxY;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class Player implements Serializable{
    private Centroid top;
    private Centroid bottom;
    private Centroid left;
    private Centroid right;

    public Player(Centroid top, Centroid bottom, Centroid left, Centroid right) {
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

    public Centroid getTop() {
        return top;
    }

    public Centroid getBottom() {
        return bottom;
    }

    public Centroid getLeft() {
        return left;
    }

    public Centroid getRight() {
        return right;
    }

    /**
     * Moves all centroids in the given direction(s) in the given interval (linear)
     * @param horizontalDir positive, negative or no (0) movement on the x-Axis
     * @param verticalDir positive, negative or no (0) movement on the y-Axis
     * @param timeInterval how long the centroids moved (for linear movement)
     */
    public void movePlayer(int horizontalDir, int verticalDir, long timeInterval){

        //Forcing -1/0/1
        int hDir = 0;
        int vDir = 0;
        if(horizontalDir > 0){
            hDir = 1;
        }else if(horizontalDir < 0){
            hDir = -1;
        }
        if(verticalDir > 0){
            vDir = 1;
        }else if(verticalDir < 0){
            vDir = -1;
        }

        double prevTopLocX = top.getX();
        double prevTopLocY = top.getY();
        double prevBotLocX = bottom.getX();
        double prevBotLocY = bottom.getY();
        double prevRightLocX = right.getX();
        double prevRightLocY = right.getY();
        double prevLeftLocX = left.getX();
        double prevLeftLocY = left.getY();

        double nextTopLocX = top.getX() + hDir * baseSpeed * timeInterval ;// (abs(hDir)+abs(vDir) == 2 ? sqrt(2) : 1);
        double nextTopLocY = top.getY() + vDir * baseSpeed * timeInterval ;// (abs(hDir)+abs(vDir) == 2 ? sqrt(2) : 1);
        double nextBotLocX = bottom.getX() + hDir * baseSpeed * timeInterval ;// (abs(hDir)+abs(vDir) == 2 ? sqrt(2) : 1);
        double nextBotLocY = bottom.getY() + vDir * baseSpeed * timeInterval ;// (abs(hDir)+abs(vDir) == 2 ? sqrt(2) : 1);
        double nextRightLocX = right.getX() + hDir * baseSpeed * timeInterval ;// (abs(hDir)+abs(vDir) == 2 ? sqrt(2) : 1);
        double nextRightLocY = right.getY() + vDir * baseSpeed * timeInterval ;// (abs(hDir)+abs(vDir) == 2 ? sqrt(2) : 1);
        double nextLeftLocX = left.getX() + hDir * baseSpeed * timeInterval ;// (abs(hDir)+abs(vDir) == 2 ? sqrt(2) : 1);
        double nextLeftLocY = left.getY() + vDir * baseSpeed * timeInterval ;// (abs(hDir)+abs(vDir) == 2 ? sqrt(2) : 1);

        boolean horizontalOk = true;
        boolean verticalOk = true;
        if(     nextTopLocX > 0 && nextBotLocX > 0 && nextRightLocX > 0 && nextLeftLocX > 0 &&
                nextTopLocX < mapMaxX && nextBotLocX < mapMaxX && nextRightLocX < mapMaxX && nextLeftLocX < mapMaxX){
            horizontalOk = true;
        }
        if(     nextTopLocY > 0 && nextBotLocY > 0 && nextRightLocY > 0 && nextLeftLocY > 0 &&
                nextTopLocY < mapMaxY && nextBotLocY < mapMaxY && nextRightLocY < mapMaxY && nextLeftLocY < mapMaxY){
            verticalOk = true;
        }

        top.setLocation(    horizontalOk ? nextTopLocX : prevTopLocX,       verticalOk ? nextTopLocY : prevTopLocY);
        bottom.setLocation( horizontalOk ? nextBotLocX : prevBotLocX,       verticalOk ? nextBotLocY : prevBotLocY);
        right.setLocation(  horizontalOk ? nextRightLocX : prevRightLocX,   verticalOk ? nextRightLocY : prevRightLocY);
        left.setLocation(   horizontalOk ? nextLeftLocX : prevLeftLocX,      verticalOk ? nextLeftLocX : prevLeftLocY);

       /* top.setLocation(nextTopLocX, nextTopLocY);
        bottom.setLocation(nextBotLocX, nextBotLocY);
        right.setLocation(nextRightLocX, nextRightLocY);
        left.setLocation(nextLeftLocX, nextLeftLocX);*/

    }

    /**
     * Grows all Centroids equally distributing the given weight between them
     * @param weight The weight to grow the player with
     */
    public void growPlayer(double weight){
        top.weight += weight / 4;
        bottom.weight += weight / 4;
        left.weight += weight / 4;
        right.weight += weight / 4;
    }
}
