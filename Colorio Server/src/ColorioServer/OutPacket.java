package ColorioServer;

import ColorioCommon.UDPSerializable;

/**
 * Class for communication between the Server (communication-side) and GameLogic
 * @author Balazs Varady
 */
public class OutPacket {
    /**The id of the client to send the packet to*/
    private int target_id;
    /**The packet itself*/
    private UDPSerializable packet;

    /**
     * Constructor
     * @param target_id The id of the client to send the packet to
     * @param packet The packet to send
     */
    public OutPacket(int target_id, UDPSerializable packet){
        this.target_id = target_id;
        this.packet = packet;
    }

    public int getTargetId() {
        return target_id;
    }

    public UDPSerializable getPacket() {
        return packet;
    }
}
