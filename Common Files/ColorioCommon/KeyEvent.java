package ColorioCommon;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class KeyEvent implements UDPSerializable {

    private char keyChar;
    private long timeStamp;
    private int id;

    public KeyEvent(java.awt.event.KeyEvent e){
        keyChar=e.getKeyChar();
        timeStamp=e.getWhen();
        id=e.getID();
    }

    public int getId(){return id;}

    public long getTimeStamp(){return timeStamp;}

    public char getKeyChar(){return keyChar;}

    @Override
    public String toString() {
        return ("Keychar: "+keyChar+" id: "+id);
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
            System.out.println("ObjectInputStream error: {0}");
            return false;
        }

        KeyEvent recivedPacket = null;
        try {
            recivedPacket = (KeyEvent) ois.readObject();
        } catch (IOException e) {
            System.out.println("Read object error: {0}");
            return false;
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: {0}");
            return false;
        }
        keyChar=recivedPacket.keyChar;
        timeStamp=recivedPacket.timeStamp;
        return true;
    }
}
