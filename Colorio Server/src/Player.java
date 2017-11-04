import java.time.Instant;

public class Player {
    private Centroid top;
    private Centroid bottom;
    private Centroid right;
    private Centroid left;
    private long lastModified;

    Player(Centroid top0, Centroid bottom0, Centroid right0, Centroid left0){
        top = top0;
        bottom = bottom0;
        right = right0;
        left = left0;

        avgWeights();
        lastModified = Instant.now().toEpochMilli();
    }

    void avgWeights(){
        /*TODO implement*/
    }



}
