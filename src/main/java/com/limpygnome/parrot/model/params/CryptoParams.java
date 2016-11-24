package com.limpygnome.parrot.model.params;

import com.limpygnome.parrot.Controller;
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
    private byte[] salt;

    // Number of rounds to perform
    private int rounds;

    // THe time at which the params were last modified
    private long lastModified;

    // Generated from above - the actual secret key
    private transient SecretKey secretKey;

    /**
     * Construct an instance.
     *
     * This will generate a salt and a secret key, intended for initial first usage.
     *
     * @param controller
     * @param password
     * @param rounds
     * @throws Exception
     */
    public CryptoParams(Controller controller, char[] password, int rounds, long lastModified) throws Exception
    {
        this.salt = controller.getCryptographyService().generateRandomSalt();
        this.rounds = rounds;
        this.lastModified = lastModified;

        // Generate secret key
        secretKey = controller.getCryptographyService().createSecretKey(password, salt, rounds);
    }

    /**
     * Constructs an instance.
     *
     * This will generate a secret key.
     *
     * @param controller
     * @param password
     * @param salt
     * @param rounds
     * @throws Exception
     */
    public CryptoParams(Controller controller, char[] password, byte[] salt, int rounds, long lastModified) throws Exception
    {
        this.salt = salt;
        this.rounds = rounds;
        this.lastModified = lastModified;

        // Generate secret key
        secretKey = controller.getCryptographyService().createSecretKey(password, salt, rounds);
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

    /**
     * Parses an instance from JSON.
     *
     * The following attribs are expected:
     * - salt - base64 of byte data
     * - rounds - integer/short
     *
     * @param object
     */
    public static CryptoParams parse(Controller controller, JSONObject object, char[] password) throws Exception
    {
        byte[] salt = Base64.decode((String) object.get("salt"));
        int rounds = (int) (long) object.get("rounds");
        long lastModified = (long) object.get("modified");

        CryptoParams params = new CryptoParams(controller, password, salt, rounds, lastModified);
        return params;
    }

}
