package es.uc3m.fintech.lesson9.proto;

import es.uc3m.fintech.lesson9.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Smoke test for the Protocol Buffers round-trip:
 * builds a ReferenceData, serializes it, deserializes it, and asserts equality.
 */
public class ProtoTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtoTest.class);
    private static final ProtoSerializer protoSerializer = new ProtoSerializer();

    public static void main(String[] args) {
        Lesson9.ReferenceData referenceDataProto = Utils.getProtoReferenceData();

        boolean roundTripOk = referenceDataProto.equals(
                protoSerializer.deserialize(protoSerializer.serialize(referenceDataProto)));

        LOGGER.debug("[Task 1] Proto Serializer [{}]", roundTripOk);
    }
}
