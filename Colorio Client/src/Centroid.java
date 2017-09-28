import java.awt.*;

public class Centroid extends Point {
    public double weight;
    public Centroid(double x, double y, double weight0){
        setLocation(x,y);
        weight= weight0;
    }

    public double getWeight() {
        return weight;
    }
}
