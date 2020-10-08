package cardcountingdetector.sinks;

import cardcountingdetector.Bet;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class BetPrinter implements SinkFunction<Bet> {

    public void invoke(Bet bet, Context context) {
        System.out.println("Player " + bet.getPlayerId() + " bets " + bet.getValue());
    }
}
