package ColorioCommon;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;

public interface UDPSerializable extends Serializable {
    DatagramPacket toDatagramPacket(InetAddress address, int port);
    boolean getFromDatagramPacket(DatagramPacket packet);

    public static UDPSerializable getClassFromDatagramPacket(DatagramPacket packet){
            ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());

            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(bais);
            } catch (IOException e) {
                System.out.println("ObjectInputStream error: {0}");
                return null;
            }

            KeyEvent recivedPacket = null;
            try {
                recivedPacket = (KeyEvent) ois.readObject();
            } catch (IOException e) {
                System.out.println("Read object error: {0}");
                return null;
            } catch (ClassNotFoundException e) {
                System.out.println("Class not found: {0}");
                return null;
            }
            catch (ClassCastException e){
                System.out.println("Wrong class!");
                return null;
            }
            return recivedPacket;
    }
}
