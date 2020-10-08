A flink toy program to simulate a player counting cards at a blackjack table, while the dealer tries to catch them. 

Flink is a data stream processor. It has sources, which produce data (in this example our source is a java class which endlessly generates random playing cards, but in real life it might be a kafka topic or a script that listens for user input); tasks, which process/transform data from sources or other tasks, and output their own data  (these are java code); and sinks, which consume data to do something with it (like printing it to stdout, or writing it to a db).

The purpose of this toy program is as a demonstration of how to use `KeyedCoProcessFunction`s, that is, functions which consume data from two different streams of data and keep track of state related to both. 

Start with `src/main/java/cardcountingdetector/jobs/CardCountingDetectionJob.java`. This is the job that we submit to the flink server. It creates a few things:
1. A source. This emits an endless stream of cards, simulating 10 regular 52 card decks combined together (just like how a casino would do it). When the deck runs out, a new set of 10 decks is shuffled. 
2. A card counting task. This task consumes the cards and keeps track of the running count using a basic high/low system. Every time a new card is "dealt" by the source, it emits the count.
3. Three player tasks. Each player consumes the count, and emits bets every time the count is updated. One player is a cheater, they make betting decisions based on the count. The two other players are "fairers", they ignore the count (but still consume the count stream so that getting a new count triggers them to bet).
4. A card counting detector task. This task consumes the union of all player bets, as well as the count. It tracks each player's average bet and emits a cheating alert if any player is frequently betting high when the count is high and betting low when the count is low (compared to their average). 

