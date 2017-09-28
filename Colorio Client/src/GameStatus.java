import java.awt.*;
import java.util.ArrayList;

public class GameStatus {
    //ide jönnek majd a változók, ki kell még dolgozni
    private ArrayList<Centroid> centroids;
    public  GameStatus(){
        centroids = new ArrayList<>();
        //teszt súlypontok
        centroids.add(new Centroid(150.5,150.1, 100.0));
        centroids.add(new Centroid(250.5,350.1, 40.0));
        centroids.add(new Centroid(150.5,550.1, 50.0));
        centroids.add(new Centroid(550.5,250.1, 70.0));
    }

    public ArrayList<Centroid> getCentroids() {
        return centroids;
    }
}
