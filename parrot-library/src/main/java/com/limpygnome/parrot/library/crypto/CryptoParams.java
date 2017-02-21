package com.limpygnome.parrot.library.crypto;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Stores parameters used for cryptography.
 *
 * This is used as a wrapper to allow the underlying implementation of parameters and types to change, when and if
 * required.
 *
 * Intended to be immutable.
 */
public class CryptoParams implements Serializable
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

    /*
        The underlying secret key, kept to only this package.
     */
    SecretKey getSecretKey()
    {
        return secretKey;
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
