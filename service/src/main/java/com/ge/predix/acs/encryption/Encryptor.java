package com.ge.predix.acs.encryption;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Encryptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Encryptor.class);
    private static final String ALGO = "Blowfish";
    private static final int KEY_LENGTH_IN_BYTES = 16;

    private SecretKeySpec secretKeySpec;
    private final ThreadLocal<Cipher> encipher;
    private final ThreadLocal<Cipher> decipher;

    private Encryptor() {
        throw new AssertionError("Encryptor class requires an encryption key upon construction");
    }

    public Encryptor(final String encryptionKey) {
        if (null == encryptionKey || encryptionKey.length() != KEY_LENGTH_IN_BYTES) {
            throw new SymmetricKeyValidationException("Encryption key must be string of length " + KEY_LENGTH_IN_BYTES);
        }

        this.secretKeySpec = new SecretKeySpec(encryptionKey.getBytes(), ALGO);
        this.encipher = ThreadLocal.withInitial(() -> initCipherInstance(Cipher.ENCRYPT_MODE));
        this.decipher = ThreadLocal.withInitial(() -> initCipherInstance(Cipher.DECRYPT_MODE));
    }

    private Cipher initCipherInstance(final int opmode) {
        try {
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(opmode, this.secretKeySpec);
            return cipher;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new CipherInitializationFailureException(e);
        }
    }

    public String encrypt(final String plainText) {
        try {
            byte[] encrypted = plainText.getBytes();
            encrypted = this.encipher.get().doFinal(encrypted);
            return Base64.encodeBase64String(encrypted);
        } catch (Throwable e) {
            LOGGER.error("Unable to encrypt");
            throw new EncryptionFailureException(e);
        }
    }

    public String decrypt(final String encrypted) {
        try {
            byte[] decrypted = Base64.decodeBase64(encrypted);
            decrypted = this.decipher.get().doFinal(decrypted);
            return new String(decrypted);
        } catch (Throwable e) {
            LOGGER.error("Unable to decrypt");
            throw new DecryptionFailureException(e);
        }
    }
}
