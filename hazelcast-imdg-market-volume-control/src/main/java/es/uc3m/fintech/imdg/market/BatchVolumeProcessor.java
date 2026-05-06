package es.uc3m.fintech.imdg.market;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.query.impl.predicates.EqualPredicate;
import es.uc3m.fintech.imdg.market.model.MarketOrder;
import es.uc3m.fintech.imdg.market.processor.OrderProcessor;

import java.util.Map;

/**
 * Part 2 — end-of-day batch.
 *
 * Repopulates the {@code ordenesMercado} cache with the same dataset used by
 * {@link OrderBookFeeder} (no listener needed — this is a settlement job, not
 * a live feed) and then runs an {@link OrderProcessor} cluster-wide via
 * {@code IMap.executeOnEntries}.
 *
 * The processor flips every order's volume to 0 and returns the previous
 * volume; this main sums those returned values to compute the total volume
 * traded during the day, and asserts that no entry remains with a non-zero
 * volume by querying with an {@code EqualPredicate}.
 */
public class BatchVolumeProcessor {

    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.getNetworkConfig().getJoin().getTcpIpConfig().addMember("localhost").setEnabled(true);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

        HazelcastInstance client = Hazelcast.newHazelcastInstance(config);
        IMap<String, MarketOrder> mapCustomers = client.getMap("ordenesMercado");

        seedCache(mapCustomers);

        // Returns one entry per cache key with (key, returnValueOfProcessor).
        Map<String, Object> processed = mapCustomers.executeOnEntries(new OrderProcessor());

        int totalVolume = 0;
        for (Map.Entry<String, Object> entry : processed.entrySet()) {
            totalVolume += (Integer) entry.getValue();
        }

        System.out.println("Volumen total negociado durante el dia: " + totalVolume);

        if (mapCustomers.entrySet(new EqualPredicate("volume", 0)).isEmpty()) {
            throw new Exception("Los volumenes de todos los elementos deben quedar a 0");
        }

        client.shutdown();
    }

    private static void seedCache(IMap<String, MarketOrder> mapCustomers) {
        mapCustomers.clear();

        for (int i = 0; i < 1000; ++i) {
            mapCustomers.set("Ibex35OrderID_" + i, new MarketOrder("BBVA", 400, 642));
            mapCustomers.set("DowJonesOrderID_" + i, new MarketOrder("Intel", 500, 3400));
        }

        for (int i = 0; i < 100; ++i) {
            mapCustomers.set("Ibex35OrderID_" + i, new MarketOrder("BBVA", 555, 642));
            mapCustomers.set("DowJonesOrderID_" + i, new MarketOrder("Intel", 1000, 3400));
        }
    }
}
