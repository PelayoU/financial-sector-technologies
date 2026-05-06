package es.uc3m.fintech.imdg.fundamentals;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

/**
 * Part 1 — minimal Hazelcast cluster member.
 *
 * Boots a single JVM as a node of a TCP/IP cluster on {@code localhost},
 * writes one entry into the distributed {@code customers} map, and then
 * keeps the JVM alive so additional instances launched in parallel can
 * discover this node and join the cluster.
 *
 * Run several instances simultaneously to observe how new nodes are
 * auto-detected and the cluster grows: the first member binds 5701, the
 * second 5702, the third 5703.
 */
public class BasicClusterNode {

    public static void main(String[] args) {
        Config config = new Config();
        // TCP/IP discovery against a fixed seed list. Multicast is disabled to make the
        // demo deterministic and friendly to networks where multicast is filtered.
        config.getNetworkConfig().getJoin().getTcpIpConfig().addMember("localhost").setEnabled(true);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);

        IMap<String, String> customers = hz.getMap("customers");
        customers.put("Test", "PU");

        // Block forever so the JVM stays in the cluster.
        while (true) ;
    }
}
