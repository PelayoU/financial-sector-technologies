package es.uc3m.fintech.lesson5.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Symmetric AES encrypt/decrypt helper.
 *
 * Holds a single shared secret key used for both encryption and decryption.
 * The {@link Cipher} instances are reused for the lifetime of the helper —
 * synchronisation is the responsibility of {@link #encode(byte[])} and
 * {@link #decode(byte[])}, which serialize access to each cipher to keep the
 * class thread-safe under concurrent producers/consumers.
 */
public class AESCrypto {

    private static final Random RND = new Random(System.currentTimeMillis());

    /** AES key length in bytes (128-bit). */
    public static final int KEY_SIZE = 16;

    private final byte[] aesKey;
    private final Cipher cipher;
    private final Cipher decipher;

    /**
     * Factory: create a helper backed by a freshly generated random AES key.
     */
    public static AESCrypto createNewInstance() throws Exception {
        byte[] key = new byte[KEY_SIZE];
        RND.nextBytes(key);
        return new AESCrypto(key);
    }

    /**
     * Create a helper bound to an externally provided AES key. The byte array
     * is defensively cloned so callers cannot mutate the internal state.
     */
    public AESCrypto(final byte[] key) throws Exception {
        this.aesKey = key.clone();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

        try {
            this.cipher = Cipher.getInstance("AES");
            this.cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            this.decipher = Cipher.getInstance("AES");
            this.decipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        } catch (final NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new Exception("Error creating AES encoding classes", e);
        }
    }

    public byte[] getAESKey() {
        return this.aesKey;
    }

    public byte[] encode(final byte[] msg) throws Exception {
        synchronized (this.cipher) {
            try {
                return this.cipher.doFinal(msg);
            } catch (final IllegalBlockSizeException | BadPaddingException e) {
                throw new Exception("Unexpected error performing AES encode", e);
            }
        }
    }

    public byte[] decode(final byte[] msg) throws Exception {
        synchronized (this.decipher) {
            try {
                return this.decipher.doFinal(msg);
            } catch (final IllegalBlockSizeException | BadPaddingException e) {
                throw new Exception("Unexpected error performing AES decode", e);
            }
        }
    }
}
