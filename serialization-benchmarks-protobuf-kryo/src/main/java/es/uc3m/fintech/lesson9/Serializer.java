package es.uc3m.fintech.lesson9;

/**
 * Common contract for the binary serializers benchmarked in this project.
 *
 * @param <K> domain object type (e.g. {@code ReferenceData})
 * @param <T> serialized representation (e.g. {@code byte[]})
 */
public interface Serializer<K, T> {
    T serialize(K referenceData);

    K deserialize(T rawData);
}
