package es.uc3m.fintech.imdg.fundamentals;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.ICountDownLatch;
import com.hazelcast.map.IMap;
import es.uc3m.fintech.imdg.fundamentals.model.Person;

import java.util.concurrent.TimeUnit;

/**
 * Part 1 — three-node coordinated put/get over a distributed {@code ICountDownLatch}.
 *
 * Each node:
 *   1. Joins the TCP/IP cluster on {@code localhost}.
 *   2. Resolves the cluster-wide {@code syncLatch} (initialised to 3).
 *   3. Writes its own entry into the distributed {@code customers} map.
 *   4. Decrements the latch and waits (up to 1 minute) for the other two nodes.
 *   5. Reads the three entries — proving the writes from the other nodes are
 *      visible cluster-wide once the latch reaches zero.
 *
 * Pass the node id (1, 2 or 3) as the only argument:
 *
 *   mvn exec:java@coordinated-node-1
 *   mvn exec:java@coordinated-node-2
 *   mvn exec:java@coordinated-node-3
 *
 * One of the nodes must call {@code latch.trySetCount(3)}; here that is the
 * responsibility of node 1, which runs that call as a no-op if the latch
 * already exists.
 */
public class CoordinatedClusterNode {

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 1) {
            System.err.println("Usage: CoordinatedClusterNode <nodeId 1..3>");
            System.exit(1);
        }
        int nodeId = Integer.parseInt(args[0]);
        if (nodeId < 1 || nodeId > 3) {
            System.err.println("nodeId must be 1, 2 or 3");
            System.exit(1);
        }

        Config config = new Config();
        config.getNetworkConfig().getJoin().getTcpIpConfig().addMember("localhost").setEnabled(true);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);

        // Cluster-wide barrier shared by all three nodes (CP subsystem).
        ICountDownLatch latch = hz.getCPSubsystem().getCountDownLatch("syncLatch");
        if (nodeId == 1) {
            latch.trySetCount(3);
        }

        IMap<String, Person> mapCustomers = hz.getMap("customers");

        String key = "nodo" + nodeId;
        mapCustomers.put(key, new Person("PU-" + nodeId, 28000 + nodeId, String.valueOf(nodeId), String.valueOf(nodeId)));
        System.out.println("Nodo " + nodeId + " ha hecho su put()");

        latch.countDown();

        System.out.println("Nodo " + nodeId + " esperando a que los demás terminen.....");
        latch.await(1, TimeUnit.MINUTES);

        System.out.println(mapCustomers.get("nodo1"));
        System.out.println(mapCustomers.get("nodo2"));
        System.out.println(mapCustomers.get("nodo3"));

        while (true) ;
    }
}
