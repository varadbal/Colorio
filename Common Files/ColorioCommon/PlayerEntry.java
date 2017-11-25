package ColorioCommon;


import java.io.Serializable;

/**
 * Wrapper class for player with client-id and name
 */
public class PlayerEntry implements Serializable{
    private int playerId;
    private Player player;
    private String name;

    public PlayerEntry(int playerId, Player player, String name) {
        this.playerId = playerId;
        this.player = player;
        this.name = name;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Player getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }
}
