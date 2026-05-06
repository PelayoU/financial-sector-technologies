package es.uc3m.fintech.lesson5.benchmark;

import es.uc3m.fintech.lesson5.crypto.AESCrypto;
import es.uc3m.fintech.lesson5.crypto.RSACrypto;

import java.nio.charset.StandardCharsets;

/**
 * Standalone performance harness comparing AES (symmetric) and RSA (asymmetric)
 * encrypt/decrypt over three payload sizes.
 *
 * Each measurement is the mean of {@link #ITERATIONS} runs. Results print to
 * {@code stdout} so they can be captured directly into the practice memory.
 *
 * <p>Run via Maven:</p>
 * <pre>
 *   mvn -q exec:java -Dexec.mainClass="es.uc3m.fintech.lesson5.benchmark.Benchmark"
 * </pre>
 */
public class Benchmark {

    private static final int ITERATIONS = 1000;

    private static final String[] LABELS = {
            "Hi",
            "This is a medium length message",
            "RSA limitation: It can only encrypt small amounts of data per RSA operation block."
    };

    public static void main(String[] args) throws Exception {
        printHeader();

        AESCrypto aes = AESCrypto.createNewInstance();
        RSACrypto rsa = new RSACrypto();

        byte[][] payloads = new byte[LABELS.length][];
        for (int i = 0; i < LABELS.length; i++) {
            payloads[i] = LABELS[i].getBytes(StandardCharsets.UTF_8);
        }

        System.out.println();
        System.out.println("============================================================");
        System.out.println("Phase 1: AES (Symmetric) Encryption Analysis");
        System.out.println("============================================================");

        for (int i = 0; i < payloads.length; i++) {
            byte[] payload = payloads[i];
            String label = LABELS[i];

            byte[] encrypted = aes.encode(payload);

            long encNs = timeNanos(() -> aes.encode(payload));
            long decNs = timeNanos(() -> aes.decode(encrypted));

            printResult(i + 1, label, payload.length, encNs, decNs);
        }

        System.out.println();
        System.out.println("============================================================");
        System.out.println("Phase 2: RSA (Asymmetric) Encryption Analysis");
        System.out.println("============================================================");

        for (int i = 0; i < payloads.length; i++) {
            byte[] payload = payloads[i];
            String label = LABELS[i];

            byte[] encrypted = rsa.encodeWithPubKey(payload);

            long encNs = timeNanos(() -> rsa.encodeWithPubKey(payload));
            long decNs = timeNanos(() -> rsa.decodeWithOwnPrivKey(encrypted));

            printResult(i + 1, label, payload.length, encNs, decNs);
        }

        System.out.println();
        System.out.println("Done.");
    }

    private interface CheckedRunnable {
        void run() throws Exception;
    }

    private static long timeNanos(CheckedRunnable op) throws Exception {
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            op.run();
        }
        return (System.nanoTime() - start) / ITERATIONS;
    }

    private static void printResult(int idx, String content, int byteLen, long encNs, long decNs) {
        System.out.println();
        System.out.printf("Message %d (%d bytes):%n", idx, byteLen);
        System.out.printf("Content: \"%s\"%n", content);
        System.out.printf("  Encryption: %.3f ms (avg over %d iterations)%n", encNs / 1_000_000.0, ITERATIONS);
        System.out.printf("  Decryption: %.3f ms (avg over %d iterations)%n", decNs / 1_000_000.0, ITERATIONS);
        System.out.printf("  Total time: %.3f ms%n", (encNs + decNs) / 1_000_000.0);
    }

    private static void printHeader() {
        System.out.println("==================================================");
        System.out.println("CRYPTOGRAPHIC PERFORMANCE ANALYSIS");
        System.out.println("==================================================");
        System.out.println("Algorithm Comparison: AES (Symmetric) vs RSA (Asymmetric)");
        System.out.printf("Test Configuration: %d iterations per measurement%n", ITERATIONS);
        System.out.println("Objective: Measure and compare encryption/decryption performance");
    }
}
