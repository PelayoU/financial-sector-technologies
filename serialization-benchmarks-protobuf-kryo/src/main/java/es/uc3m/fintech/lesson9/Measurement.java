package es.uc3m.fintech.lesson9;

import es.uc3m.fintech.lesson9.kryo.KryoSerializer;
import es.uc3m.fintech.lesson9.model.ReferenceData;
import es.uc3m.fintech.lesson9.proto.Lesson9;
import es.uc3m.fintech.lesson9.proto.ProtoSerializer;
import es.uc3m.fintech.lesson9.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performance harness comparing Protocol Buffers and Kryo over the
 * canonical {@link ReferenceData} payload.
 *
 * Runs three back-to-back loops at {@link #NUM_ITERATIONS} iterations each:
 *   1. Pure serialization (ns/op + payload size in bytes).
 *   2. Pure deserialization (ns/op).
 *   3. Round-trip serialization + deserialization (ns/op).
 *
 * The objective is not microbenchmark precision (no JMH, no warm-up) but
 * to surface the order-of-magnitude differences between schema-driven and
 * reflection-driven serializers under a fixed payload.
 */
public class Measurement {
    private static final long NUM_ITERATIONS = 5_000_000;

    private static final Logger LOGGER = LoggerFactory.getLogger(Measurement.class);

    private static final KryoSerializer kryoSerializer = new KryoSerializer();
    private static final ProtoSerializer protoSerializer = new ProtoSerializer();

    public static void main(String[] args) {
        ReferenceData referenceData = Utils.getReferenceData();
        Lesson9.ReferenceData referenceDataProto = Utils.getProtoReferenceData();

        LOGGER.debug("[Lesson 9] Size of referenceData instrument list {}",
                referenceData.getListOfInstruments().size());
        LOGGER.debug("[Lesson 9] Algorithm identifier {}", referenceData.getAlgorithmIdentifier());
        LOGGER.debug("[Lesson 9] Algorithm marketId {}", referenceData.getMarketId());

        LOGGER.debug("[Lesson 9] Proto Serializer [{}]", referenceDataProto.equals(
                protoSerializer.deserialize(protoSerializer.serialize(referenceDataProto))));

        LOGGER.debug("[Lesson 9] Kryo Serializer [{}]", referenceData.equals(
                kryoSerializer.deserialize(kryoSerializer.serialize(referenceData))));

        testPerformanceSerialization(referenceData, referenceDataProto);
        testPerformanceDeSerialization(kryoSerializer.serialize(referenceData), referenceDataProto.toByteArray());
        testPerformanceSerializationAndDeserialization(referenceData, referenceDataProto);
    }

    private static void testPerformanceSerialization(ReferenceData referenceData,
                                                     Lesson9.ReferenceData referenceDataProto) {
        long protoIni = System.nanoTime();
        byte[] objProto = null;
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            objProto = protoSerializer.serialize(referenceDataProto);
        }
        long protoFin = System.nanoTime();
        long meanProto = (protoFin - protoIni) / NUM_ITERATIONS;

        long kryoIni = System.nanoTime();
        byte[] objKryo = null;
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            objKryo = kryoSerializer.serialize(referenceData);
        }
        long kryoFin = System.nanoTime();
        long meanKryo = (kryoFin - kryoIni) / NUM_ITERATIONS;

        LOGGER.info("Results for Serialization: (mean per iteration in ns)");
        LOGGER.info("  Proto mean: {}", meanProto);
        LOGGER.info("  Kryo  mean: {}", meanKryo);
        LOGGER.info("Object size: (bytes)");
        LOGGER.info("  Proto size: {}", objProto.length);
        LOGGER.info("  Kryo  size: {}", objKryo.length);
    }

    private static void testPerformanceDeSerialization(byte[] kryoSerialized,
                                                       byte[] protoSerialized) {
        long protoIni = System.nanoTime();
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            protoSerializer.deserialize(protoSerialized);
        }
        long protoFin = System.nanoTime();
        long meanProto = (protoFin - protoIni) / NUM_ITERATIONS;

        long kryoIni = System.nanoTime();
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            kryoSerializer.deserialize(kryoSerialized);
        }
        long kryoFin = System.nanoTime();
        long meanKryo = (kryoFin - kryoIni) / NUM_ITERATIONS;

        LOGGER.info("Results for Deserialization: (mean per iteration in ns)");
        LOGGER.info("  Proto mean: {}", meanProto);
        LOGGER.info("  Kryo  mean: {}", meanKryo);
    }

    private static void testPerformanceSerializationAndDeserialization(ReferenceData referenceData,
                                                                       Lesson9.ReferenceData referenceDataProto) {
        long protoIni = System.nanoTime();
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            byte[] bytes = protoSerializer.serialize(referenceDataProto);
            protoSerializer.deserialize(bytes);
        }
        long protoFin = System.nanoTime();
        long meanProto = (protoFin - protoIni) / NUM_ITERATIONS;

        long kryoIni = System.nanoTime();
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            byte[] bytes = kryoSerializer.serialize(referenceData);
            kryoSerializer.deserialize(bytes);
        }
        long kryoFin = System.nanoTime();
        long meanKryo = (kryoFin - kryoIni) / NUM_ITERATIONS;

        LOGGER.info("Results for Serialization + Deserialization (mean per iteration in ns)");
        LOGGER.info("  Proto mean: {}", meanProto);
        LOGGER.info("  Kryo  mean: {}", meanKryo);
    }
}
