package com.limpygnome.parrot.service;

import com.limpygnome.parrot.model.db.EncryptedAesValue;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

/**
 * A service for performing cryptography.
 */
public class CryptographyService {

    // The length of secret keys
    private static final int KEY_LENGTH = 256;

    // The type of secret key
    private static final String SECRET_KEY_TYPE = "PBKDF2WithHmacSHA256";

    // THe minimum length of random bytes for a salt
    private static final int SALT_LENGTH_MIN = 32;

    // The maximum length of random bytes for a salt
    private static final int SALT_LENGTH_MAX = 64;

    // Default rounds
    public static final int ROUNDS_DEFAULT = 65536;

    // RNG to generate random bytes for IVs
    private SecureRandom random;

    public CryptographyService()
    {
        // TODO: consider if needs seed
        random = new SecureRandom();
    }

    /**
     * @return random set of bytes of variable length
     */
    public byte[] generateRandomSalt()
    {
        int saltLength = SALT_LENGTH_MIN + random.nextInt(SALT_LENGTH_MAX - SALT_LENGTH_MIN + 1);
        byte[] salt = new byte[saltLength];
        random.nextBytes(salt);
        return salt;
    }

    public SecretKey createSecretKey(char[] password, byte[] salt, int rounds) throws Exception
    {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_TYPE);

        KeySpec keySpec = new PBEKeySpec(password, salt, rounds, KEY_LENGTH);
        SecretKey tmp = secretKeyFactory.generateSecret(keySpec);
        SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
        return secretKey;
    }

    /**
     * Encrypts provided value.
     *
     * @param secretKey the secret ket
     * @param value the value to be encrypted
     * @return the encrypted wrapper
     * @throws Exception if the specified value cannot be encrypted
     */
    public EncryptedAesValue encrypt(SecretKey secretKey, byte[] value) throws Exception
    {
        // In the result of null, just set to empty...
        if (value == null)
        {
            value = new byte[0];
        }

        // Build IV
        byte[] iv = new byte[16];
        random.nextBytes(iv);

        // Setup cipher
        CipherParameters cipherParams = new ParametersWithIV(new KeyParameter(secretKey.getEncoded()), iv);
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
        aes.init(true, cipherParams);

        // Process data
        byte[] rawResult = processCrypto(aes, value);

        // Build encrypted result
        EncryptedAesValue result = new EncryptedAesValue(iv, rawResult);
        return result;
    }

    /**
     * Decrypts provided wrapper object.
     *
     * @param secretKey the secret ket
     * @param value the encrypted (wrapper) object
     * @return the decrypted value
     * @throws Exception if the specified value cannot be decrypted
     */
    public byte[] decrypt(SecretKey secretKey, EncryptedAesValue value) throws Exception
    {
        CipherParameters cipherParams = new ParametersWithIV(new KeyParameter(secretKey.getEncoded()), value.getIv());
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
        aes.init(false, cipherParams);

        // Process data
        byte[] result = processCrypto(aes, value.getValue());
        return result;
    }

    private byte[] processCrypto(PaddedBufferedBlockCipher aes, byte[] data) throws Exception
    {
        int minSize = aes.getOutputSize(data.length);
        byte[] outBuf = new byte[minSize];
        int length1 = aes.processBytes(data, 0, data.length, outBuf, 0);
        int length2 = aes.doFinal(outBuf, length1);
        int actualLength = length1 + length2;
        byte[] result = new byte[actualLength];
        System.arraycopy(outBuf, 0, result, 0, result.length);
        return result;
    }

}
