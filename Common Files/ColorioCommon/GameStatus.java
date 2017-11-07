package ColorioCommon;

import com.sun.istack.internal.NotNull;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * UDPSerializable wrapper class for an ArrayList
 * ATM holds Centroids, maybe change that later
 * First one should be the client's own
 */
public class GameStatus implements UDPSerializable{
    /**
     * Instance variables
     */
    private ArrayList<Centroid> centroids;

    /**
     * Constructor
     */
    public  GameStatus(){
        centroids = new ArrayList<>();
    }

    /**
     * Getters, Setters
     */
    public ArrayList<Centroid> getCentroids() {
        return centroids;
    }
    public void setCentroids(@NotNull ArrayList<Centroid> newCents){ centroids = newCents; }
    public void addCentroid (Centroid cent) {
        if(centroids == null){
            centroids = new ArrayList<Centroid>();
        }
        centroids.add(cent);
    }
    public void addCentroid (int index, Centroid cent){
        if(centroids == null){
            centroids = new ArrayList<Centroid>();
        }
        centroids.add(index, cent);
    }

    /**
     * UDPSerializable implementation
     */
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
        catch (ClassCastException e){
            System.out.println("Wrong class!");
            return false;
        }
        centroids = recivedPacket.centroids;
        return true;
    }
}
