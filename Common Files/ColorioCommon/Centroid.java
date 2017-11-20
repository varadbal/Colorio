package ColorioCommon;

import java.awt.*;
import java.awt.geom.Point2D;

public class Centroid extends Point2D.Double {
    public Color color;
    public double weight;
    public Centroid(double x, double y, double weight0, Color color){
        setLocation(x,y);
        this.color=color;
        weight= weight0;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public double getWeight() {
        return weight;
    }
    public Color getColor() {return color;}
}
