package cardcountingdetector;

import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

// Consume the current count and emit bets. For simplicity, we have infinite money, and emit a bet
// every time the count gets updated. Think of it as the bet we WOULD make, if we could at that moment.
// Stateless.
//
// Keyed by: integer representing player id
// Consumes: integer of count
// Emits: integer of bet
public class Cheater extends ProcessFunction<Count, Bet> {

    private int playerId;

    public Cheater(int playerId) {
        this.playerId = playerId;
    }

    @Override
    public void processElement(Count countObj, Context ctx, Collector<Bet> out) throws Exception {
        // Strategy: if the count is less than 5, bet the minimum.
        // If the count is high, bet the minimum * (current count / 5).
        int count = countObj.getCount();
        if (count < 5) {
            out.collect(new Bet(10, playerId));
        } else {
            float bet = 10 * (((float) count) / 5.0f);
            out.collect(new Bet((int) bet, playerId));
        }
    }
}
