package com.limpygnome.parrot.library.crypto;

import javax.crypto.SecretKey;

/**
 * A factory for creating instances of {@link CryptoParams}.
 */
public class CryptoParamsFactory
{
    private CryptoFactory cryptoFactory;

    /**
     * Creates a new factory.
     */
    public CryptoParamsFactory()
    {
        this.cryptoFactory = new CryptoFactory();
    }

    CryptoParamsFactory(CryptoFactory cryptoFactory)
    {
        this.cryptoFactory = cryptoFactory;
    }

    /**
     * Creates an instance.
     *
     * @param password password
     * @param rounds rounds/permutations
     * @param lastModified epoch time of when last modified (used for merging)
     * @return an instance
     * @throws Exception when cannot create instance
     */
    public CryptoParams create(char[] password, int rounds, long lastModified) throws Exception
    {
        byte[] salt = cryptoFactory.generateRandomSalt();
        CryptoParams cryptoParams = create(password, rounds, lastModified, salt);
        return cryptoParams;
    }

    /**
     * Creates an instance.
     *
     * @param password password
     * @param rounds rounds/permutations
     * @param lastModified epoch time of when last modified (used for merging)
     * @param salt salt
     * @return an instance
     * @throws Exception when cannot create instance
     */
    public CryptoParams create(char[] password, int rounds, long lastModified, byte[] salt) throws Exception
    {
        // Generate secret key
        SecretKey secretKey = cryptoFactory.createSecretKey(password, salt, rounds);

        // Create instance
        CryptoParams instance = new CryptoParams(salt, rounds, lastModified, secretKey);
        return instance;
    }

    /**
     * Clones an existing instance, so that no references exist (hard).
     *
     * @param original instance to be cloned
     * @param password password originally used for instance, as this is not stored
     * @return a newly cloned instance
     * @throws Exception when cannot create instance
     */
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
