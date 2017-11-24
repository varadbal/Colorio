package ColorioCommon;


public abstract class Constants {
    //Communication constants
    public static int responseTimeOut=5000;
    public static int serverSleep=10;
    public static int clientSleep=50;
    public static int connectionStopRepeatLimit=3;

    public static int serverPort = 49155;
    public static int clientPort = 49154;

    //Game constants
    public static double startingWeight = 100.0;
    public static double baseSpeed = 0.1;
    public static double mapMaxX = 500;
    public static double mapMaxY = 500;
}
