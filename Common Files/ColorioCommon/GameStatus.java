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
 * One ArrayList for Players and one for Foods
 */
public class GameStatus implements UDPSerializable{
    /**
     * Instance variables
     */
    private ArrayList<PlayerEntry> players;
    private ArrayList<Centroid> foods;          //Non-player objects?
    /**
     * Constructor
     */
    public GameStatus() {
        players = new ArrayList<>();
        foods = new ArrayList<>();
    }

    /**
     * Getters, Setters
     */
    public ArrayList<PlayerEntry> getPlayers() {
        return players;
    }

    public ArrayList<Centroid> getPlayerCentroids(){
        ArrayList<Centroid> ret = new ArrayList<>(players.size()*4);
        for(PlayerEntry player : players){
            ret.add(player.getPlayer().getBottom());
            ret.add(player.getPlayer().getTop());
            ret.add(player.getPlayer().getRight());
            ret.add(player.getPlayer().getLeft());
        }
        return ret;
    }

    public ArrayList<Centroid> getFoods() {
        return foods;
    }

    /**
     * Add element to ArrayLists
     */

    public void addPlayerEntry (PlayerEntry toAdd) {
        players.add(toAdd);
    }
    public void addFood(Centroid toAdd){
        foods.add(toAdd);
    }

    //FIXME this
    /*@Override
    public String toString() {

        String ret = new String("");
        for (Centroid centroid : centroids){
            ret+=centroid.toString();
        }
        return ret;
        return "";
    }*/

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
            System.out.println("Serialization problem on " + Thread.currentThread().getName());
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
        players = recivedPacket.players;
        foods = recivedPacket.foods;
        return true;
    }
}
