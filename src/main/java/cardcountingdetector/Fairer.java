package cardcountingdetector;

import java.util.Arrays;
import java.util.Random;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

// A nice player who does not cheat. Bets in a similar pattern to the cheater, but does so randomly.
// That is, they usually bet the minimum, but 1/3 the time will bet a random number between min and min*2,
// and 1/6 of the time between min*2 and min*3.
public class Fairer extends ProcessFunction<Count, Bet> {

    private int playerId;

    public Fairer(int playerId) {
        this.playerId = playerId;
    }

    @Override
    public void processElement(Count count, Context ctx, Collector<Bet> out) throws Exception {
        Random random = new Random();
        int riskiness = random.nextInt(6);
        int minBet = 0;
        int maxBet = 0;
        if (riskiness <= 2) {
            minBet = 10;
            maxBet = 10;
        } else if (riskiness <= 4) {
            minBet = 10;
            maxBet = 20;
        } else if (riskiness <= 5) {
             minBet = 20;
             maxBet = 30;
        }

        int range = (maxBet - minBet) + 1;
        int bet = random.nextInt(range) + minBet;
        out.collect(new Bet(bet, playerId));

    }
}
