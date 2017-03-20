package com.limpygnome.parrot.library.io.json;

import com.limpygnome.parrot.library.crypto.CryptoParams;
import com.limpygnome.parrot.library.crypto.CryptoParamsFactory;
import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONObject;

/**
 * Manages reading and writing of {@link CryptoParams} in JSON.
 */
class CryptoParamsJsonReaderWriter
{
    private CryptoParamsFactory cryptoParamsFactory;

    CryptoParamsJsonReaderWriter(CryptoParamsFactory cryptoParamsFactory)
    {
        this.cryptoParamsFactory = cryptoParamsFactory;
    }

    /**
     * Parses a {@link JSONObject} into {@link CryptoParams} instance.
     *
     * @param json json object
     * @param password password for creating secret key
     * @return instance
     * @throws Exception when unable to parse
     */
    CryptoParams parse(JSONObject json, char[] password) throws Exception
    {
        byte[] salt = Base64.decode((String) json.get("cryptoParams.salt"));
        int rounds = (int) (long) json.get("cryptoParams.rounds");
        long lastModified = (long) json.get("cryptoParams.modified");

        CryptoParams instance = cryptoParamsFactory.create(password, rounds, lastModified, salt);
        return instance;
    }

    /**
     * Writes {@link CryptoParams} instance to an instance of {@link JSONObject}.
     *
     * @param object target instance
     * @param cryptoParams params to be written
     */
    void write(JSONObject object, CryptoParams cryptoParams)
    {
        object.put("cryptoParams.salt", Base64.toBase64String(cryptoParams.getSalt()));
        object.put("cryptoParams.rounds", cryptoParams.getRounds());
        object.put("cryptoParams.modified", cryptoParams.getLastModified());
    }

}
