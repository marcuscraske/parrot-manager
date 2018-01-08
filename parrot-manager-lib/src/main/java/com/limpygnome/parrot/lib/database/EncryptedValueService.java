package com.limpygnome.parrot.lib.database;

import com.google.gson.JsonObject;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import com.limpygnome.parrot.library.db.Database;
import org.springframework.stereotype.Service;

/**
 * A bridge between the presentation and persistence layer of encrypted data.
 */
@Service
public class EncryptedValueService
{

    /**
     * Creates an encrypted value from a string.
     *
     * @param database database
     * @param text value to be encrypted; can be null
     * @return an instance, or null if the provided value is null
     * @throws Exception
     */
    public EncryptedValue fromString(Database database, String text) throws Exception
    {
        EncryptedValue result = null;

        if (text != null)
        {
            byte[] data = text.getBytes("UTF-8");
            result = database.encrypt(data);
        }

        return result;
    }

    public String asString(Database database, EncryptedValue encryptedValue) throws Exception
    {
        String result = null;

        if (encryptedValue != null)
        {
            byte[] decrypted = database.decrypt(encryptedValue);

            if (decrypted != null)
            {
                result = new String(decrypted, "UTF-8");
            }
        }

        return result;
    }

    public EncryptedValue fromJson(Database database, JsonObject json) throws Exception
    {
        EncryptedValue result = null;

        if (json != null)
        {
            String text = json.toString();
            result = fromString(database, text);
        }

        return result;
    }

}
