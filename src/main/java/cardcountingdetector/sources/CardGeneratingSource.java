package cardcountingdetector.sources;

import cardcountingdetector.Card;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

// Source that outputs cards from a deck which is shuffled when it runs out.
public class CardGeneratingSource implements SourceFunction<Card> {

    // Generate a fresh, unshuffled deck
    private List<Card> generateDeck() {
        ArrayList<Card> deck = new ArrayList<>();
        for (String value : Arrays.asList("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A")) {
            for (int i = 0; i < 4; i++) {
                deck.add(new Card(value));
            }
        }
        return deck;
    }

    // Shuffle a deck
    private List<Card> shuffleDeck(List<Card> deck) {
        List<Card> shuffledDeck = new ArrayList<>();
        Random random = new Random();
        while (deck.size() != 0) {
            int i = random.nextInt(deck.size());
            shuffledDeck.add(deck.get(i));
            deck.remove(i);
        }
        return shuffledDeck;
    }

    // Generate n decks, combine them, and shuffle into a random order.
    private List<Card> generateAndShuffleDecks(int n) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            deck.addAll(generateDeck());
        }
        return shuffleDeck(deck);
    }

    // Draw n cards off the top of a deck (removing them). If the deck runs out, just return.
    private List<Card> drawN(List<Card> deck, int n) {
        List<Card> drawn = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (deck.size() == 0) {
                return drawn;
            }
            // pop
            drawn.add(deck.get(0));
            deck.remove(0);
        }
        return drawn;
    }

    // NOTE: We output values by passing a value to ctx.collect
    @Override
    public void run(SourceContext<Card> ctx) throws Exception {
        List<Card> deck = generateAndShuffleDecks(10);
        while (true) {
            // If our deck has no cards, generate a new one
            if (deck == null || deck.size() == 0) {
                deck = generateAndShuffleDecks(10);
            }

            // Draw 10 cards off the top and emit these
            List<Card> draw = drawN(deck, 10);
            for (Card card : draw) {
                ctx.collect(card);
            }

            // Sleep for 1 second to throttle
            Thread.sleep(1000);
        }
    }

    @Override
    public void cancel() {
    }
}
