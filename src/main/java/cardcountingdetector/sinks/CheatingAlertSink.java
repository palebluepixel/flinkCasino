package cardcountingdetector.sinks;

import cardcountingdetector.Bet;
import cardcountingdetector.CheatingAlert;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class CheatingAlertSink implements SinkFunction<CheatingAlert> {

    public void invoke(CheatingAlert bet, Context context) {
        System.out.println("!!!!!!!!!! Player " + bet.getPlayerId() + " IS CHEATING!!!!!!");
    }
}
