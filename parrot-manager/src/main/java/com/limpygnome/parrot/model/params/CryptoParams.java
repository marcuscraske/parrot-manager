package com.limpygnome.parrot.model.params;

import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONObject;

import javax.crypto.SecretKey;
import java.util.Arrays;

/**
 * Stores, and able to sometimes generate, cryptography information.
 *
 * This should never need to store the password.
 *
 * Intended to be immutable.
 */
public class CryptoParams
{
    // The salt randomly generated when the DB was created
    byte[] salt;

    // Number of rounds to perform
    int rounds;

    // THe time at which the params were last modified
    long lastModified;

    // Generated from above - the actual secret key; cannot be serialized
    transient SecretKey secretKey;

    CryptoParams(byte[] salt, int rounds, long lastModified, SecretKey secretKey) throws Exception
    {
        this.salt = salt;
        this.rounds = rounds;
        this.lastModified = lastModified;
        this.secretKey = secretKey;
    }

    /**
     * @return the salt used by the secret key
     */
    public byte[] getSalt()
    {
        return salt;
    }

    /**
     * @return the number of rounds used by the secret key
     */
    public int getRounds()
    {
        return rounds;
    }

    /**
     * @return the epoch time at which these params were last modified
     */
    public long getLastModified()
    {
        return lastModified;
    }

    /**
     * @return the secret key used for encryption/decryption
     */
    public SecretKey getSecretKey()
    {
        return secretKey;
    }

    /**
     * Writes the parameters of this instance to a JSON object.
     *
     * @param object the target object
     */
    public void write(JSONObject object)
    {
        object.put("cryptoParams.salt", Base64.toBase64String(salt));
        object.put("cryptoParams.rounds", rounds);
        object.put("cryptoParams.modified", lastModified);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CryptoParams that = (CryptoParams) o;

        if (rounds != that.rounds) return false;
        if (lastModified != that.lastModified) return false;
        if (!Arrays.equals(salt, that.salt)) return false;
        return secretKey != null ? secretKey.equals(that.secretKey) : that.secretKey == null;

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(salt);
        result = 31 * result + rounds;
        result = 31 * result + (int) (lastModified ^ (lastModified >>> 32));
        result = 31 * result + (secretKey != null ? secretKey.hashCode() : 0);
        return result;
    }

}
