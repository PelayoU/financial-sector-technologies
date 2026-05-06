package es.uc3m.fintech.lesson9.kryo;

import es.uc3m.fintech.lesson9.model.ReferenceData;
import es.uc3m.fintech.lesson9.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Smoke test for the Kryo round-trip:
 * builds a ReferenceData, serializes it, deserializes it, and asserts equality.
 */
public class KryoTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(KryoTest.class);
    private static final KryoSerializer kryoSerializer = new KryoSerializer();

    public static void main(String[] args) {
        ReferenceData referenceData = Utils.getReferenceData();

        boolean roundTripOk = referenceData.equals(
                kryoSerializer.deserialize(kryoSerializer.serialize(referenceData)));

        LOGGER.debug("[Task 2] Kryo Serializer [{}]", roundTripOk);
    }
}
