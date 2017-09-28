import java.awt.*;
import java.awt.image.BufferedImage;

public class ColorioRender extends BufferedImage {
    private GameStatus status;
    public ColorioRender(int width, int height, GameStatus status0) {
        super(width, height, BufferedImage.TYPE_INT_RGB);
        status=status0;
    }
    private static double sigmoid(double x)
    {
        return 1 / (1 + Math.exp(-x));
    }
    public void render(){
        double sum=0;
        for (int i=0;i<getWidth();i++){
            for (int j=0;j<getHeight();j++){
                sum=0;
                for(Centroid centroid : status.getCentroids()){
                    sum+=1.0/Math.hypot(i-centroid.getX(),j-centroid.getY())*centroid.getWeight();
                }
                int gray=255- (int) (((sigmoid(sum)-0.5))*2*255);
                int rgb = gray;
                rgb = (rgb << 8) + gray;
                rgb = (rgb << 8) + gray;
                setRGB(i,j,rgb);
            }
        }
    }
}
