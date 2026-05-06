# Serialization Benchmarks — Protocol Buffers vs Kryo

A focused **performance comparison of two binary serialization libraries** — **Google Protocol Buffers** (schema-driven) and **Kryo** (reflection-driven, with class registration) — over a market reference-data payload typical of trading platforms (`marketId`, `algorithmIdentifier`, list of `Instrument`).

The benchmark targets a question that comes up early in any low-latency design: *do we pay the schema discipline of Protobuf, or do we let Kryo introspect Java classes at runtime?* The numbers below come from a 5-million-iteration run over a fixed payload of five Spanish blue-chips (BBVA, SAN, FDAX, TEF, IBER).

---

## Tech stack

| Area | Choice |
|------|--------|
| Language | Java 21 |
| Build | Maven |
| Protobuf | `protobuf-java` 4.28.3, codegen via `protobuf-maven-plugin` 0.6.1 |
| Kryo | `com.esotericsoftware:kryo` 5.6.2 |
| Logging | SLF4J + Logback |
| OS resolution | `kr.motd.maven:os-maven-plugin` (resolves the right `protoc` binary per platform) |

---

## What it measures

Three back-to-back loops at **5,000,000 iterations** each, over the same `ReferenceData` instance:

1. **Pure serialization** — `obj → byte[]` (also captures payload size).
2. **Pure deserialization** — `byte[] → obj`.
3. **Round-trip** — `obj → byte[] → obj`.

This is *not* a JMH microbenchmark — there is no warm-up phase, no blackhole, no fork isolation. The intent is to surface the order-of-magnitude difference between schema-driven and reflection-driven serializers under a fixed payload, not to publish a peer-reviewed number.

---

## Results

Run on Apple Silicon (Java 21, single warm process, default JVM flags):

| Phase | Protobuf | Kryo | Winner |
|---|---:|---:|:---:|
| Serialization (mean ns/op) | **124** | 688 | Protobuf — **~5.5× faster** |
| Deserialization (mean ns/op) | 271 | 288 | Practically tied |
| Serialization + Deserialization (mean ns/op) | **468** | 514 | Protobuf (~10% faster) |
| Payload size (bytes) | 56 | **32** | Kryo (~43% smaller) |

### Why Protobuf wins on serialization speed

Protobuf has the schema burnt into generated code: it knows every field's tag, type and order at compile time, and emits a minimal sequence of `writeVarint` / `writeBytes` calls. Kryo, by contrast, uses **reflection** over the registered classes — it has to walk the field descriptors at runtime, even if class registration shortens the path. With a payload this small (~50 bytes), the reflection overhead dominates the actual byte-pushing work.

### Why deserialization is a near-tie

Both libraries have to allocate the target object and populate fields from a byte stream. The work is dominated by **memory allocation** (`new ReferenceData()`, `new Instrument()`, `new ArrayList()`) rather than by parsing. Protobuf's parser still does slightly less, but the gap collapses to ~6%.

### Why Kryo's payload is smaller here

Counter-intuitive, given that Protobuf is usually the size champion. Two factors:
- Kryo uses **pre-registered class IDs**: a registered class is referenced with a one-byte varint instead of a Protobuf field-tag-per-field. For a payload with a small number of fields, that overhead matters.
- Protobuf encodes a `(tag, wire_type)` pair for every populated field, including the repeated `Instrument` entries. With five instruments × two fields each, the cumulative tag bytes add up.

For larger payloads with many fields and big strings, this typically reverses and Protobuf becomes the smaller wire format. The tradeoff direction depends on payload shape.

---

## Trading-system takeaway

In a market data path where messages are small and frequent (heartbeats, order updates, top-of-book deltas), **schema-driven serializers (Protobuf, FlatBuffers, SBE) clearly beat reflection-driven serializers (Kryo, Java native) on the hot path**. The CPU spent serializing dominates the network wire savings — a 5×–6× difference in encode time will swamp a few extra bytes on the wire.

For *cold* paths (snapshot persistence, intra-JVM cache eviction, debug endpoints), Kryo is convenient: zero schema definition, transparent support for arbitrary Java graphs. The price is reflection in the hot path and a wire format that is **not stable across class layout changes** unless you pin field IDs explicitly with custom serializers.

---

## Quick start

**Requirements:** Java 21+, Maven 3.9+. The Protobuf compiler (`protoc`) is downloaded automatically by `protobuf-maven-plugin` at build time — nothing to install manually.

```bash
# Smoke tests: round-trip equality on each serializer
mvn exec:java@proto-test
mvn exec:java@kryo-test

# Full benchmark (takes a couple of minutes on a modern laptop)
mvn exec:java@benchmark
```

The benchmark prints means in nanoseconds and payload sizes in bytes:

```
[INFO] Results for Serialization: (mean per iteration in ns)
[INFO]   Proto mean: 124
[INFO]   Kryo  mean: 688
[INFO] Object size: (bytes)
[INFO]   Proto size: 56
[INFO]   Kryo  size: 32
[INFO] Results for Deserialization: (mean per iteration in ns)
[INFO]   Proto mean: 271
[INFO]   Kryo  mean: 288
[INFO] Results for Serialization + Deserialization (mean per iteration in ns)
[INFO]   Proto mean: 468
[INFO]   Kryo  mean: 514
```

---

## Project layout

```
src/main/proto/
└── lesson9.proto          # Protobuf schema (Instrument, ReferenceData)

src/main/java/es/uc3m/fintech/lesson9/
├── Serializer.java        # Common interface for both implementations
├── Measurement.java       # Performance harness — main benchmark entry point
├── model/
│   ├── Instrument.java    # POJO consumed by Kryo
│   └── ReferenceData.java # POJO consumed by Kryo
├── proto/
│   ├── ProtoSerializer.java # Wraps protobuf-generated builder/parser
│   └── ProtoTest.java       # Round-trip smoke test for Protobuf
├── kryo/
│   ├── KryoSerializer.java  # Class-registered Kryo serializer
│   └── KryoTest.java        # Round-trip smoke test for Kryo
└── utils/
    └── Utils.java         # Builds the canonical benchmark payload

src/main/resources/
└── logback.xml            # Console appender, DEBUG by default
```

The Protobuf-generated `Lesson9.java` is emitted into `target/generated-sources/` at build time and is not committed to the repo.

---

## Design notes

- **No JMH on purpose.** The exercise targets the order-of-magnitude difference, not microbenchmark precision. The numbers above will move ±20% across hardware and JVM versions, but the relative ranking and the explanation hold.
- **Pre-registered Kryo classes.** `KryoSerializer` registers `ReferenceData`, `Instrument` and `ArrayList` upfront. Without registration Kryo falls back to writing fully-qualified class names into the byte stream, which kills both speed and size.
- **Deterministic payload.** `Utils.getReferenceData()` returns the same five-instrument list every time; both serializers operate on byte-identical inputs so any timing difference is attributable to the libraries, not the data.
- **One-shot loop, no GC pinning.** The harness allocates a fresh `byte[]` per iteration on the serialization path. That's deliberate — production code tends to do exactly that, and the benchmark stays representative.

---

## Reference

Built in the context of the *Message serialization and optimization* track, MSc in Financial Sector Technologies (UC3M). The library choices (Protobuf, Kryo) and the `ReferenceData` payload follow the course brief; the benchmarking harness, analysis, and conclusions are mine.
