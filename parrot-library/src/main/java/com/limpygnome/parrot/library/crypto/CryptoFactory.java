package com.limpygnome.parrot.library.crypto;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by limpygnome on 20/02/17.
 */
public class CryptoFactory
{
    // The length of secret keys
    private static final int KEY_LENGTH = 256;

    // The type of secret key
    private static final String SECRET_KEY_TYPE = "PBKDF2WithHmacSHA256";

    // THe minimum length of random bytes for a salt
    private static final int SALT_LENGTH_MIN = 32;

    // The maximum length of random bytes for a salt
    private static final int SALT_LENGTH_MAX = 64;

    // RNG to generate random bytes for IVs
    private SecureRandom random;

    public CryptoFactory()
    {
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

}
