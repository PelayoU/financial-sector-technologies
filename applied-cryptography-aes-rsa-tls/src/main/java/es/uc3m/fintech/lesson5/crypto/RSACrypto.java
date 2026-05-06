package es.uc3m.fintech.lesson5.crypto;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Asymmetric RSA encrypt/decrypt helper.
 *
 * Generates and owns a single 1024-bit RSA key pair on instantiation. Two
 * {@link Cipher} instances are kept hot:
 *   - {@code ownPublKeyDecoder} initialised in ENCRYPT mode with the public key
 *     (used to encrypt outbound messages addressed to this owner).
 *   - {@code ownPrivKeyDecoder} initialised in DECRYPT mode with the private
 *     key (used to recover messages that were encrypted with this owner's
 *     public key).
 *
 * Hardcoded to {@code "RSA"} (PKCS#1 v1.5 padding by default), which constrains
 * the maximum payload size to {@code keySizeBytes - 11 = 117 bytes} for a
 * 1024-bit key. RSA is intentionally used here for *small* payloads only —
 * the canonical use is wrapping a symmetric session key (see the README).
 */
public class RSACrypto {

    public static final int RSA_KEY_SIZE = 1024;
    public static final String SIGNATURE_CODEC = "SHA1withRSA";
    public static final String RSA_CODEC = "RSA";

    private final KeyPair keyPair;
    private final Cipher ownPrivKeyDecoder;
    private final Cipher ownPublKeyDecoder;

    public RSACrypto() throws Exception {
        this.keyPair = generateKeyPair();
        this.ownPrivKeyDecoder = initCipher(Cipher.DECRYPT_MODE, this.keyPair.getPrivate());
        this.ownPublKeyDecoder = initCipher(Cipher.ENCRYPT_MODE, this.keyPair.getPublic());
    }

    private KeyPair generateKeyPair() throws Exception {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_CODEC);
            keyGen.initialize(RSA_KEY_SIZE);
            return keyGen.generateKeyPair();
        } catch (final NoSuchAlgorithmException e) {
            throw new Exception("Error generating key pair", e);
        }
    }

    private Cipher initCipher(final int mode, final Key key) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance(RSA_CODEC);
            cipher.init(mode, key);
            return cipher;
        } catch (final NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new Exception("Error initializing cipher classes", e);
        }
    }

    public byte[] encodeWithPubKey(final byte[] msg) throws Exception {
        try {
            return this.ownPublKeyDecoder.doFinal(msg);
        } catch (final IllegalBlockSizeException | BadPaddingException e) {
            throw new Exception("Unexpected error encoding RSA message", e);
        }
    }

    public byte[] decodeWithOwnPrivKey(final byte[] msg) throws Exception {
        try {
            return this.ownPrivKeyDecoder.doFinal(msg);
        } catch (final IllegalBlockSizeException | BadPaddingException e) {
            throw new Exception("Unexpected error decoding RSA message", e);
        }
    }
}
