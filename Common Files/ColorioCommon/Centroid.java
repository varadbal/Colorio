package ColorioCommon;

import java.awt.*;
import java.awt.geom.Point2D;

public class Centroid extends Point2D.Double {
    public Color color;
    public double weight;
    private double redIntensityFactor;
    private double greenIntensityFactor;
    private double blueIntensityFactor;
    public Centroid(double x, double y, double weight0, Color color){
        setLocation(x,y);
        this.color=color;
        weight= weight0;
        redIntensityFactor=(1.0-(double) color.getRed()/255);
        greenIntensityFactor=(1.0-(double) color.getGreen()/255);
        blueIntensityFactor =(1.0-(double) color.getBlue()/255);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public double getWeight() {
        return weight;
    }
    public Color getColor() {return color;}

    public double getRedIntensityFactor() {
        return redIntensityFactor;
    }

    public double getGreenIntensityFactor() {
        return greenIntensityFactor;
    }

    public double getBlueIntensityFactor() {
        return blueIntensityFactor;
    }
}
