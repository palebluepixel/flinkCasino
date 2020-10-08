package cardcountingdetector;

import java.util.Arrays;

public class Card {
    private String face;

    public Card(String face) {
        this.face = face;
    }

    public String getFace() {
        return face;
    }

    public int getCountingValue() {
        if (Arrays.asList("2", "3", "4", "5", "6").contains(face)) {
            return 1;
        } else if (Arrays.asList("10", "J", "Q", "K", "A").contains(face)) {
            return -1;
        } else {
            return 0;
        }
    }


    // Hack
    public Integer getTable() {
        return 1;
    }

    @Override
    public String toString() {
        return "Card{" +
                "face='" + face + '\'' +
                '}';
    }
}
