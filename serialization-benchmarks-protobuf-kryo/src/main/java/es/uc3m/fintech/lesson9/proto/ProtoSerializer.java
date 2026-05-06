package es.uc3m.fintech.lesson9.proto;

import es.uc3m.fintech.lesson9.Serializer;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol Buffers serializer for {@link Lesson9.ReferenceData}.
 *
 * Schema-driven: relies on the descriptor compiled from {@code lesson9.proto},
 * so no reflection is needed at runtime.
 */
public class ProtoSerializer implements Serializer<Lesson9.ReferenceData, byte[]> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtoSerializer.class);

    public byte[] serialize(Lesson9.ReferenceData referenceData) {
        return referenceData.toByteArray();
    }

    public Lesson9.ReferenceData deserialize(byte[] rawData) {
        try {
            return Lesson9.ReferenceData.parseFrom(rawData);
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("Failed to parse protobuf payload", e);
            return null;
        }
    }
}
