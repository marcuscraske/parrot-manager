package com.limpygnome.parrot.library.io.json;

import com.google.gson.JsonObject;
import com.limpygnome.parrot.library.crypto.CryptoParams;
import com.limpygnome.parrot.library.crypto.CryptoParamsFactory;
import org.bouncycastle.util.encoders.Base64;

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
     * Parses a {@link JsonObject} into {@link CryptoParams} instance.
     *
     * @param json json object
     * @param password password for creating secret key
     * @return instance
     * @throws Exception when unable to parse
     */
    CryptoParams parse(JsonObject json, char[] password) throws Exception
    {
        byte[] salt = Base64.decode(json.get("cryptoParams.salt").getAsString());
        int rounds = json.get("cryptoParams.rounds").getAsInt();
        long lastModified = json.get("cryptoParams.modified").getAsLong();

        CryptoParams instance = cryptoParamsFactory.create(password, rounds, lastModified, salt);
        return instance;
    }

    /**
     * Writes {@link CryptoParams} instance to an instance of {@link JsonObject}.
     *
     * @param object target instance
     * @param cryptoParams params to be written
     */
    void write(JsonObject object, CryptoParams cryptoParams)
    {
        object.addProperty("cryptoParams.salt", Base64.toBase64String(cryptoParams.getSalt()));
        object.addProperty("cryptoParams.rounds", cryptoParams.getRounds());
        object.addProperty("cryptoParams.modified", cryptoParams.getLastModified());
    }

}
