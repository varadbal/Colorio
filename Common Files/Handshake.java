import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Handshake implements UDPSerializable{
    private String name;
    private int id;

    public Handshake(String name, int id) {
        this.name = name;
        this.id = id;
    }
    public Handshake(){
        name=null;
        id=0;
    }

    public int getId(){return id;}

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return ("name: " + name + ", id: " + id);
    }

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

    public boolean getFromDatagramPacket(DatagramPacket packet) {
        ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());

        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bais);
        } catch (IOException e) {
            System.out.println("ObjectInputStreem error: {0}");
            return false;
        }

        Handshake recivedPacket = null;
        try {
            recivedPacket = (Handshake) ois.readObject();
        } catch (IOException e) {
            System.out.println("Read object error: {0}");
            return false;
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: {0}");
            return false;
        }
        name=recivedPacket.name;
        id=recivedPacket.id;
        return true;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
