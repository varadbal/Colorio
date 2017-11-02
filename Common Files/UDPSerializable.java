import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;

public interface UDPSerializable extends Serializable {
    public DatagramPacket toDatagramPacket(InetAddress address, int port);
    public boolean getFromDatagramPacket(DatagramPacket packet);
}
