package es.uc3m.fintech.imdg.market;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import es.uc3m.fintech.imdg.market.model.MarketOrder;

/**
 * Part 2 — feeder process for the {@code ordenesMercado} cache.
 *
 * Inserts 1,000 BBVA orders (Ibex35 keys) and 1,000 Intel orders (DowJones keys),
 * then issues 100 in-place updates that bump the volume on the first 100 keys.
 * The companion {@link VolumeMonitor} subscribes to this cache via a
 * {@link es.uc3m.fintech.imdg.market.listener.VolumeListener} and reacts to
 * each event in real time.
 */
public class OrderBookFeeder {

    public static void main(String[] args) {
        Config config = new Config();
        config.getNetworkConfig().getJoin().getTcpIpConfig().addMember("localhost").setEnabled(true);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

        HazelcastInstance client = Hazelcast.newHazelcastInstance(config);

        IMap<String, MarketOrder> mapCustomers = client.getMap("ordenesMercado");
        // Start clean: clear() removes the entries cluster-wide, not just locally.
        mapCustomers.clear();

        for (int i = 0; i < 1000; ++i) {
            MarketOrder bbva = new MarketOrder("BBVA", 400, 642);
            MarketOrder intel = new MarketOrder("Intel", 500, 3400);
            mapCustomers.put("Ibex35OrderID_" + i, bbva);
            mapCustomers.put("DowJonesOrderID_" + i, intel);
        }

        for (int i = 0; i < 100; ++i) {
            MarketOrder bbva = new MarketOrder("BBVA", 555, 642);
            MarketOrder intel = new MarketOrder("Intel", 1000, 3400);
            mapCustomers.put("Ibex35OrderID_" + i, bbva);
            mapCustomers.put("DowJonesOrderID_" + i, intel);
        }

        client.shutdown();
    }
}
