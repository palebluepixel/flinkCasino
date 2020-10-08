package cardcountingdetector.sinks;

import cardcountingdetector.Count;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class CountPrinter implements SinkFunction<Count> {

    public void invoke(Count count, Context context) {
        System.out.println("Count: " + count.getCount());
    }

}
