package ColorioCommon;


public abstract class Constants {
    //Communication constants
    public static int responseTimeOut=5000;
    public static int serverSleep=10;
    public static int clientSleep=50;
    public static int connectionStopRepeatLimit=3;
    public static int minBufferSize = 6400;

    public static int serverPort = 49155;
    public static int clientPort = 49154;

    //Game constants
    public static double foodWeight = 100;           //Default Food Weight
    public static double startingWeight = 100.0;    //Initial Player-Centroid Weight
    public static double baseSpeed = 0.1;           //Initial Movement Speed
    public static double mapMaxX = 800;             //Game Map X-Boundary
    public static double mapMaxY = 600;             //Game Map Y-Boundary
    public static double radius(double weight){     //Returns the (expected) player-radius based on the weight of one centroid
        return weight/10;
    }
}
