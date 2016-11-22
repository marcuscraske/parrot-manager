package com.limpygnome.parrot.model.params;

import com.limpygnome.parrot.Controller;
import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONObject;

import javax.crypto.SecretKey;

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

    // The actual secret key
    private SecretKey secretKey;

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
    public CryptoParams(Controller controller, char[] password, int rounds) throws Exception
    {
        this.salt = controller.getCryptographyService().generateRandomSalt();
        this.rounds = rounds;

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
    public CryptoParams(Controller controller, char[] password, byte[] salt, int rounds) throws Exception
    {
        this.salt = salt;
        this.rounds = rounds;

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
     * @return the secret key used for encryption/decryption
     */
    public SecretKey getSecretKey()
    {
        return secretKey;
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
        int rounds = (int) object.get("rounds");

        CryptoParams params = new CryptoParams(controller, password, salt, rounds);
        return params;
    }

}
