# Hazelcast IMDG — Market Volume Control

A two-part project showcasing **Hazelcast** as an in-memory data grid (IMDG) for trading-system patterns. The first part covers cluster fundamentals (TCP discovery, distributed maps, CP coordination primitives); the second models a market-data flow with a **real-time volume listener** and an **end-of-day batch `EntryProcessor`** — two of the patterns Hazelcast is most often deployed for in capital markets infrastructure (risk caches, position keepers, post-trade settlement jobs).

The project intentionally keeps the topology local: every node is a JVM running on `localhost`, and the cluster auto-forms via TCP/IP join — no Kubernetes operator, no Discovery SPI, just the bare moving parts you'd find at the bottom of any production deployment.

---

## Tech stack

| Area | Choice |
|------|--------|
| Language | Java 21 |
| Build | Maven |
| Data grid | Hazelcast 5.5 |
| Cluster join | TCP/IP discovery (multicast disabled for determinism) |
| Logging | SLF4J + Logback |

---

## Part 1 — Cluster fundamentals

Goal: understand how a Hazelcast cluster forms, how distributed data structures behave, and how cluster members coordinate via the **CP subsystem** (Raft-backed primitives).

### 1.1 Cluster bootstrap (`BasicClusterNode`)

A single JVM joins a TCP/IP cluster on `localhost`, writes one entry into the distributed `customers` `IMap`, and stays alive. Run multiple instances in parallel: each new JVM auto-joins the cluster, the membership grows from `size:1` → `size:2` → `size:3`, and Hazelcast assigns ports `5701`, `5702`, `5703` automatically.

```bash
mvn exec:java@basic-cluster-node    # start as many copies as you want, in parallel
```

### 1.2 Coordinated three-node put/get (`CoordinatedClusterNode`)

Three JVMs join the cluster, each writes its own `Person` POJO into the shared `customers` map (`nodo1`, `nodo2`, `nodo3`), then synchronise on a cluster-wide **`ICountDownLatch`** (count = 3) before reading back all three keys. The latch lives in the **CP subsystem**, so the count is consistent across the cluster — there is no node-local view of it.

The output below shows what node 3 prints once the latch reaches zero: it sees the writes from node 1 and node 2 even though the writes happened in different JVMs.

```bash
# Three terminals, in order:
mvn exec:java@coordinated-node-1
mvn exec:java@coordinated-node-2
mvn exec:java@coordinated-node-3
```

The same pattern (write own state → wait on latch → read peer state) is the building block of any cluster-wide warm-up: pricing engines pre-loading reference data, OMS replicas sharing recovered state on startup, etc.

---

## Part 2 — Market volume control

Goal: instrument a distributed cache of market orders with two complementary patterns:

- **`EntryListener`** for real-time alerts (the *risk-watchdog* pattern).
- **`EntryProcessor`** for in-place modification + cluster-wide aggregation (the *end-of-day batch* pattern).

### 2.1 Real-time volume alerts

Three actors:

- **`VolumeMonitor`** — joins the cluster, subscribes a `VolumeListener("BBVA")` to the `ordenesMercado` `IMap`, and never writes. Pure side-process.
- **`OrderBookFeeder`** — different JVM, writes 1,000 BBVA orders + 1,000 Intel orders into the cache, then in-place updates the volume on the first 100 keys of each.
- **`VolumeListener`** — implements both `EntryAddedListener` and `EntryUpdatedListener`. Filters by instrument symbol, accumulates volume, and prints an alert each time the accumulator crosses **30,000 shares** (then resets to 0).

Notably, `entryUpdated` is non-trivial: keys in `ordenesMercado` are not bound to a fixed instrument (an "Ibex35OrderID_X" key may flip between symbols across updates), so the listener must subtract the *old* volume and add the *new* one **only when each side matches the monitored instrument**. Otherwise the accumulator drifts away from the actual traded volume.

```bash
# Terminal 1 — start the monitor first
mvn exec:java@volume-monitor

# Terminal 2 — feed orders into the cache
mvn exec:java@order-feeder
```

During a real run, the monitor prints one alert each time the cumulative BBVA volume crosses 30,000 shares, then resets the accumulator.

> **Note on timing.** The feeder pushes the 1,100 BBVA writes back-to-back without a `Thread.sleep`, so the alerts pile up immediately as the listener processes the event burst. In a production feed, the cadence is set by the upstream gateway — the listener logic is identical.

### 2.2 End-of-day batch — `EntryProcessor`

`BatchVolumeProcessor` repopulates the cache, then runs an **`EntryProcessor`** cluster-wide:

```java
Map<String, Object> processed = mapCustomers.executeOnEntries(new OrderProcessor());
```

`OrderProcessor.process()` is executed **on the partition owner** of each entry. It:

1. Reads the order's volume.
2. Resets the volume to 0 in place (`entry.setValue(updated)`).
3. Returns the previous volume.

Because the processor runs on the data side, only the integers travel back to the caller — not the `MarketOrder` objects. The caller sums all returned values to compute the total volume traded during the day, and asserts the post-condition with an `EqualPredicate("volume", 0)`.

```bash
mvn exec:java@batch-processor
```

A real run aggregates **905,500** as the total daily volume across all partitions.

The post-condition (`EqualPredicate("volume", 0)`) holds: every entry in the cache ends with `volume=0`, confirming the processor reached every partition and the aggregation is consistent with the in-cache state at the moment `executeOnEntries` resolved.

---

## Quick start

**Requirements:** Java 21+, Maven 3.9+. Hazelcast 5.x ships every transport it needs.

Each main class can be launched via its dedicated `exec` execution. They are designed to run in **separate JVMs** so the cluster topology becomes visible:

```bash
# Part 1
mvn exec:java@basic-cluster-node      # repeat in parallel terminals to grow the cluster
mvn exec:java@coordinated-node-1      # then -2 and -3 in their own JVMs

# Part 2
mvn exec:java@volume-monitor          # listener side, start first
mvn exec:java@order-feeder            # writer side
mvn exec:java@batch-processor         # standalone end-of-day job
```

---

## Project layout

```
src/main/java/es/uc3m/fintech/imdg/
├── fundamentals/
│   ├── BasicClusterNode.java         # Single-node cluster bootstrap (Part 1.1)
│   ├── CoordinatedClusterNode.java   # 3-node sync via ICountDownLatch (Part 1.2)
│   └── model/
│       └── Person.java               # Serializable POJO carried across the grid
└── market/
    ├── OrderBookFeeder.java          # Writer: 1k+1k orders, then 100+100 updates
    ├── VolumeMonitor.java            # Subscribes a VolumeListener, never writes
    ├── BatchVolumeProcessor.java     # End-of-day batch: executeOnEntries
    ├── listener/
    │   └── VolumeListener.java       # EntryAdded + EntryUpdated, alert at 30k
    ├── processor/
    │   └── OrderProcessor.java       # In-place reset + return previous volume
    └── model/
        └── MarketOrder.java          # instrument / volume / price
```

---

## Design notes

- **Why TCP/IP join, not multicast.** Most production financial networks block multicast on segments that touch the trading floor. TCP/IP discovery against a fixed seed list is the realistic baseline; multicast is disabled here so the demo behaves identically across machines.
- **Why CP subsystem for the latch.** `ICountDownLatch.trySetCount(3)` and `await()` are linearisable — they survive split-brain in a way `IAtomicLong` over the standard partitioned IMap does not. For a 3-node coordinated startup it's overkill, but it's the correct primitive once you cross more than one host.
- **Why an `EntryProcessor`, not a `for` loop on the client.** A client-side aggregation would have to pull every `MarketOrder` over the wire, then push the modified value back. The processor runs on the partition owner, so the only payload that crosses the network is the returned `Integer` — orders of magnitude less data, and the modification is atomic per-entry.
- **Why `Serializable` on the listener.** Hazelcast may ship a listener instance to the partition owner depending on registration mode (`includeValue=true`, smart routing, etc.). A non-serialisable listener silently fails the registration on the partition side. Better to opt in explicitly.
- **POJO serialisation.** `Person` and `MarketOrder` use plain Java `Serializable`. For high-throughput production deployments Hazelcast's `IdentifiedDataSerializable` or `Compact` serialisation are materially faster — but plain `Serializable` keeps the demo independent of generated code.

---

## Reference

Built in the context of the *In-Memory Data Grids* track, MSc in Financial Sector Technologies (UC3M). Cluster fundamentals (Part 1) and the trading-system patterns built on top of them (Part 2) live in the same Maven project for cohesion.
