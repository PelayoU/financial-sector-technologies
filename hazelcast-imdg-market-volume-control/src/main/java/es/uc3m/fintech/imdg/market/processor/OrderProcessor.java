package es.uc3m.fintech.imdg.market.processor;

import com.hazelcast.map.EntryProcessor;
import es.uc3m.fintech.imdg.market.model.MarketOrder;

import java.util.Map;

/**
 * EntryProcessor that resets the volume of every order in the cache to zero
 * (marking it as "settled") and returns the previous volume.
 *
 * EntryProcessors are executed locally on the partition owner of each entry,
 * so only the result — an {@code Integer} per entry — travels back to the
 * caller, instead of shipping the full {@code MarketOrder} graph over the
 * wire. That makes them the right tool for cluster-wide aggregations during
 * end-of-day batch jobs.
 */
public class OrderProcessor implements EntryProcessor<String, MarketOrder, Object> {

    @Override
    public Object process(Map.Entry<String, MarketOrder> entry) {
        MarketOrder order = entry.getValue();
        Integer previousVolume = order.getVolume();

        order.setVolume(0);
        entry.setValue(order);

        // Return value is wrapped in Object by Hazelcast.
        return previousVolume;
    }
}
