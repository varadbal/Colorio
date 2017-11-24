package ColorioCommon;

import static ColorioCommon.Constants.baseSpeed;

public class Player {
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
     *
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
            verticalDir = 1;
        }else if(verticalDir < 0){
            verticalDir = -1;
        }

        //Moving the centroids FIXME way too linear (two-directional movement)
        top.setLocation(top.getX() + hDir * baseSpeed * timeInterval, top.getY() + vDir * baseSpeed * timeInterval);
        bottom.setLocation(bottom.getX() + hDir * baseSpeed * timeInterval, bottom.getY() + vDir * baseSpeed * timeInterval);
        right.setLocation(right.getX() + hDir * baseSpeed * timeInterval, right.getY() + vDir * baseSpeed * timeInterval);
        left.setLocation(left.getX() + hDir * baseSpeed * timeInterval, left.getY() + vDir * baseSpeed * timeInterval);
    }

}
