package ColorioCommon;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;

public interface UDPSerializable extends Serializable {
    DatagramPacket toDatagramPacket(InetAddress address, int port);
    boolean getFromDatagramPacket(DatagramPacket packet);
}
