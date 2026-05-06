package es.uc3m.fintech.imdg.market;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import es.uc3m.fintech.imdg.market.listener.VolumeListener;
import es.uc3m.fintech.imdg.market.model.MarketOrder;

/**
 * Part 2 — monitoring node.
 *
 * Joins the cluster, attaches a {@link VolumeListener} to the
 * {@code ordenesMercado} map and never writes to it. Acts as a stand-in for
 * the kind of side-process that watches the trading cache for breach
 * conditions — risk limits, position alerts, kill-switch triggers — without
 * mutating the data itself.
 *
 * Run this node first, then launch {@link OrderBookFeeder} in another JVM.
 */
public class VolumeMonitor {

    public static void main(String[] args) {
        Config config = new Config();
        config.getNetworkConfig().getJoin().getTcpIpConfig().addMember("localhost").setEnabled(true);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

        HazelcastInstance client = Hazelcast.newHazelcastInstance(config);
        IMap<String, MarketOrder> mapCustomers = client.getMap("ordenesMercado");

        mapCustomers.addEntryListener(new VolumeListener("BBVA"), true);
    }
}
