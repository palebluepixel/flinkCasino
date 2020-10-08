package cardcountingdetector;

// A bet is a numeric value and the id of the player that made it
public class Bet {
    private int value;

    private int playerId;

    public Bet(int value, int playerId) {
        this.value = value;
        this.playerId = playerId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getValue() {
        return value;
    }
}
