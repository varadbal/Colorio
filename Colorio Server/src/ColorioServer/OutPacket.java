package ColorioServer;

import ColorioCommon.UDPSerializable;

public class OutPacket {
    private int target_id;
    private UDPSerializable packet;

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
