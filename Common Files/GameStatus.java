import java.awt.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

public class GameStatus implements UDPSerializable{
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

    @Override
    public DatagramPacket toDatagramPacket(InetAddress address, int port) {
        try {
            // Serializing the packet
            ByteArrayOutputStream baos = new ByteArrayOutputStream(6400);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.flush();
            byte[] bytes = baos.toByteArray();
            return new DatagramPacket(bytes,bytes.length,address,port);
        } catch (IOException e) {
            System.out.println("Serialization problem");
        }
        return null;
    }

    @Override
    public boolean getFromDatagramPacket(DatagramPacket packet) {
        ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());

        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bais);
        } catch (IOException e) {
            System.out.println("ObjectInputStreem error: {0}");
            return false;
        }

        GameStatus recivedPacket = null;
        try {
            recivedPacket = (GameStatus) ois.readObject();
        } catch (IOException e) {
            System.out.println("Read object error: {0}");
            return false;
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: {0}");
            return false;
        }
        centroids = recivedPacket.centroids;
        return true;
    }
}
