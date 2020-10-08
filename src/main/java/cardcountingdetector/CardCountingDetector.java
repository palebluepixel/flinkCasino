package cardcountingdetector;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.util.Collector;

// A player is possibly counting cards if they increase their bets when the current
// count favors them. We join two streams:
// 1. The current count
// 2. The union of all player bets.
// And look for players who always bet high when the count is high, and always bet low
// when the count is low. Those people are cheaters, and we emit an alert for them.
// This process is keyed by player ID so we are only looking at one player at a time.
public class CardCountingDetector extends KeyedCoProcessFunction<Integer, Count, Bet, CheatingAlert> {

    // Count of the cards
    private transient ValueState<Integer> count;

    // Track the player's average bet so we know when they are going high/low.
    // We do this by tracking the sum and count to compute the average.
    private transient ValueState<Integer> totalBet;
    private transient ValueState<Integer> nBets;

    // Track how many times a player bets suspiciously / not suspiciously
    private transient ValueState<Integer> susActions;
    private transient ValueState<Integer> nonSusActions;

    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<Integer> countDescriptor = new ValueStateDescriptor<>("count", Types.INT);
        count = getRuntimeContext().getState(countDescriptor);

        ValueStateDescriptor<Integer> totalBetDescriptor = new ValueStateDescriptor<>("total bet", Types.INT);
        totalBet = getRuntimeContext().getState(totalBetDescriptor);
        ValueStateDescriptor<Integer> nBetsDescriptor = new ValueStateDescriptor<>("number bets", Types.INT);
        nBets = getRuntimeContext().getState(nBetsDescriptor);
        ValueStateDescriptor<Integer> susActionsDescriptor = new ValueStateDescriptor<>("sus count", Types.INT);
        susActions = getRuntimeContext().getState(susActionsDescriptor);
        ValueStateDescriptor<Integer> nonSusActionsDescriptor = new ValueStateDescriptor<>("non sus count", Types.INT);
        nonSusActions = getRuntimeContext().getState(nonSusActionsDescriptor);
    }


    @Override
    public void processElement1(Count newCount, Context ctx, Collector<CheatingAlert> out) throws Exception {
        // Just keep track of the count
        count.update(newCount.getCount());
    }

    @Override
    public void processElement2(Bet value, Context ctx, Collector<CheatingAlert> out) throws Exception {
        // Specifically, we will consider any time the player goes above their average bet by more
        // than 5 when the count is above 10, or goes below their average bet by more than 5 when
        // the count is below 10 to be "sus", and record how many "sus" actions a player takes. If
        // a player has taken more than 50 "sus" moves, we alert.
        // This could obviously be made more sophisticated!

        // Update all state about this player
        int playerId = value.getPlayerId();
        int bet = value.getValue();
        incValue(totalBet, bet);
        incValue(nBets, 1);
        int average = totalBet.value() / nBets.value();

        if (actionIsSus(count.value() == null ? 0 : count.value(), average, bet)) {
            incValue(susActions, 1);
        } else {
            incValue(nonSusActions,1);
        }

        // check if player is sus and emit alert
        if (playerIsSus(susActions.value(), nonSusActions.value())) {
            out.collect(new CheatingAlert(playerId));
        }

        // Log output tmp
        //System.out.println("Cheating detection hints: player " + playerId + " avgbet: " + average +
        //        " susActions: " + susActions.value() + " nonSusActions: " + nonSusActions.value());
    }

    // True if the count is high and the bet is high, or count is low and bet is low.
    private boolean actionIsSus(int count, int average, int bet) {
        if (count < 10 &&
                bet < average &&
                average - bet >= 5) {
            return true;
        }

        if (count > 10 &&
                bet > average &&
                bet - average >= 5) {
            return true;
        }

        // A perfectly normal bet.
        return false;
    }

    // A player must have made 50 sus bets to be sus.
    private boolean playerIsSus(Integer susActions, Integer nonSusActions) {
        if (susActions == null) {
            susActions = 0;
        }
        if (nonSusActions == null) {
            nonSusActions = 0;
        }
        /*int totalActions = susActions + nonSusActions;
        if (totalActions > 50) {
            if (((float) susActions) / ((float) totalActions) > 0.5f) {
                return true;
            }
        }*/
        if (susActions > 50) {
            return true;
        }

        return false;
    }

    // Util to inc the value by the given amount. Sets the value to the given amount if the value is null.
    private void incValue(ValueState<Integer> value, int amount) throws Exception{
        if (value.value() == null) {
            value.update(new Integer(amount));
        } else {
            value.update(value.value() + amount);
        }

    }
}