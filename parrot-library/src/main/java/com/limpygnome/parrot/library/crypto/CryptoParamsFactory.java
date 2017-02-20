package com.limpygnome.parrot.library.crypto;

import javax.crypto.SecretKey;
import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONObject;

/**
 * A factory for creating instances of {@link CryptoParams}.
 */
public class CryptoParamsFactory
{
    private CryptoFactory cryptoFactory;

    public CryptoParamsFactory()
    {
        this.cryptoFactory = new CryptoFactory();
    }

    /*
        TODO: JSON should not be at this level!
     */
    public CryptoParams parse(JSONObject json, char[] password) throws Exception
    {
        byte[] salt = Base64.decode((String) json.get("cryptoParams.salt"));
        int rounds = (int) (long) json.get("cryptoParams.rounds");
        long lastModified = (long) json.get("cryptoParams.modified");

        CryptoParams instance = create(password, rounds, lastModified, salt);
        return instance;
    }

    public CryptoParams create(char[] password, int rounds, long lastModified) throws Exception
    {
        byte[] salt = cryptoFactory.generateRandomSalt();
        CryptoParams cryptoParams = create(password, rounds, lastModified, salt);
        return cryptoParams;
    }

    public CryptoParams create(char[] password, int rounds, long lastModified, byte[] salt) throws Exception
    {
        // Generate secret key
        SecretKey secretKey = cryptoFactory.createSecretKey(password, salt, rounds);

        // Create instance
        CryptoParams instance = new CryptoParams(salt, rounds, lastModified, secretKey);
        return instance;
    }

    public CryptoParams clone(CryptoParams original, char[] password) throws Exception
    {
        // Generate secret key
        SecretKey secretKey = cryptoFactory.createSecretKey(password, original.salt, original.rounds);

        // Create new cloned instance
        CryptoParams cryptoParams = new CryptoParams(
                original.salt.clone(), original.rounds, original.lastModified, secretKey
        );
        return cryptoParams;
    }

}
