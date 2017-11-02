import java.awt.*;
import java.util.ArrayList;

public class GameStatus {
    //ide jönnek majd a változók, ki kell még dolgozni
    private ArrayList<Centroid> centroids;
    public  GameStatus(){
        centroids = new ArrayList<>();
        //teszt súlypontok
        centroids.add(new Centroid(150.5,150.1, 50.0,Color.BLUE));
        centroids.add(new Centroid(250.5,350.1, 2.0,Color.RED));
        centroids.add(new Centroid(150.5,550.1, 25.0,Color.CYAN));
        centroids.add(new Centroid(550.5,250.1, 35.0,Color.green));
    }

    public ArrayList<Centroid> getCentroids() {
        return centroids;
    }
}
