package com.limpygnome.parrot.model.params;

import com.limpygnome.parrot.service.CryptographyService;
import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * A factory for creating instances of {@link CryptoParams}.
 */
@Component
public class CryptoParamsFactory
{
    @Autowired
    private CryptographyService cryptographyService;

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
        byte[] salt = cryptographyService.generateRandomSalt();
        CryptoParams cryptoParams = create(password, rounds, lastModified, salt);
        return cryptoParams;
    }

    public CryptoParams create(char[] password, int rounds, long lastModified, byte[] salt) throws Exception
    {
        // Generate secret key
        SecretKey secretKey = cryptographyService.createSecretKey(password, salt, rounds);

        // Create instance
        CryptoParams instance = new CryptoParams(salt, rounds, lastModified, secretKey);
        return instance;
    }

    public CryptoParams clone(CryptoParams original, char[] password) throws Exception
    {
        // Generate secret key
        SecretKey secretKey = cryptographyService.createSecretKey(password, original.salt, original.rounds);

        // Create new cloned instance
        CryptoParams cryptoParams = new CryptoParams(
                original.salt.clone(), original.rounds, original.lastModified, secretKey
        );
        return cryptoParams;
    }

}
