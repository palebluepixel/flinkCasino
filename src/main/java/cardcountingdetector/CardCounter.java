package cardcountingdetector;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

// Keyed by: integer representing player id
// Consumes card: most recent card to hit the table
// Emits integer: count
public class CardCounter extends KeyedProcessFunction<Integer, Card, Count> {

    // Track overall count so far
    private transient ValueState<Integer> count;

    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<Integer> countDescriptor = new ValueStateDescriptor<>(
                "count",
                Types.INT);
        count = getRuntimeContext().getState(countDescriptor);
    }

    @Override
    public void processElement(Card card, Context ctx, Collector<Count> out) throws Exception {
        // Initialization case
        if (count.value() == null) {
            count.update(0);
        }

        // Update count
        int localCount = count.value();
        localCount = localCount + card.getCountingValue();
        count.update(localCount);

        // Emit latest count
        out.collect(new Count(count.value()));
    }
}
