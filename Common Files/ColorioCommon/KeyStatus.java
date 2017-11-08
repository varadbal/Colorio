package ColorioCommon;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class KeyStatus implements UDPSerializable {
    private boolean wPressed;
    private boolean aPressed;
    private boolean sPressed;
    private boolean dPressed;

    public KeyStatus(boolean wPressed, boolean aPressed, boolean sPressed, boolean dPressed) {
        this.wPressed = wPressed;
        this.aPressed = aPressed;
        this.sPressed = sPressed;
        this.dPressed = dPressed;
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

        KeyStatus recivedPacket = null;
        try {
            recivedPacket = (KeyStatus) ois.readObject();
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
        wPressed=recivedPacket.wPressed;
        aPressed=recivedPacket.aPressed;
        sPressed=recivedPacket.sPressed;
        dPressed=recivedPacket.dPressed;
        return true;
    }
}
