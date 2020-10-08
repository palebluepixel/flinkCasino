package cardcountingdetector.jobs;

import cardcountingdetector.Bet;
import cardcountingdetector.Card;
import cardcountingdetector.CardCounter;
import cardcountingdetector.CardCountingDetector;
import cardcountingdetector.Cheater;
import cardcountingdetector.Count;
import cardcountingdetector.Fairer;
import cardcountingdetector.sinks.BetPrinter;
import cardcountingdetector.sinks.CheatingAlertSink;
import cardcountingdetector.sinks.CountPrinter;
import cardcountingdetector.sources.CardGeneratingSource;
import cardcountingdetector.CheatingAlert;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

// Job to start a card counting task which consumes from a randomly generated deck
// and outputs the count to stdout. This is what we submit to the flink server.
public class CardCountingDetectionJob {
    public static void main(String[] args) throws Exception {
        // We add sources, sinks, and tasks to the stream env
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Add source of cards
        DataStream<Card> cards = env.addSource(new CardGeneratingSource()).name("Card Source");

        // Add task to count them
        DataStream<Count> counts = cards.keyBy(Card::getTable).process(new CardCounter()).name("Card counter");

        // Add sink for counts that just prints
        counts.addSink(new CountPrinter()).name("Count sink");

        // Add a cheating player who consumes the current count and makes bets based on it
        DataStream<Bet> cheaterBets = counts.process(new Cheater(1)).name("Cheater bets");
        cheaterBets.addSink(new BetPrinter()).name("Cheater bets sink");

        // Add two fair players who bet randomly
        DataStream<Bet> fairerBets1 = counts.process(new Fairer(2)).name("Fair bets 1");
        fairerBets1.addSink(new BetPrinter()).name("Fair bets 1 sink");
        DataStream<Bet> fairerBets2 = counts.process(new Fairer(3)).name("Fair bets 2");
        fairerBets2.addSink(new BetPrinter()).name("Fair bets 2 sink");

        // Add a cheating detector: coprocess the count and players bets to determine who is cheating
        DataStream<Bet> allBets = cheaterBets.union(fairerBets1, fairerBets2);
        ConnectedStreams<Count, Bet> countsNBets = counts.connect(allBets);
        DataStream<CheatingAlert> cheatingAlerts = countsNBets.keyBy(Count::getKey, Bet::getPlayerId).process(new CardCountingDetector());
        cheatingAlerts.addSink(new CheatingAlertSink());

        env.execute("Count Cards");
    }
}
