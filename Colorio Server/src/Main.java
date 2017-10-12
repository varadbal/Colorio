public class Main {
    public static void main(String[] args) {

        ColorioServer s1 = new ColorioServer( "Thread-1");
        s1.start();

        ColorioServerTester t1 = new ColorioServerTester( "Thread-2");
        t1.start();

    }
}
