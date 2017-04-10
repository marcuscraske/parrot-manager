package com.limpygnome.parrot.library.io.json;

import com.google.gson.JsonObject;
import com.limpygnome.parrot.library.crypto.EncryptedAesValue;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import org.bouncycastle.util.encoders.Base64;

import java.util.UUID;

/**
 * Reads/writes encrypted values.
 *
 * Current implementation is to use AES - refer to {@link EncryptedAesValue}.
 */
class EncryptedValueJsonReaderWriter
{

    /**
     * Reads an encrypted value from a node's serialized JSON.
     *
     * @param jsonNode serialized JSON object
     * @return the value, or null
     */
    EncryptedValue read(JsonObject jsonNode)
    {
        EncryptedValue value;

        if (jsonNode.has("iv") && jsonNode.has("data"))
        {
            UUID id = jsonNode.has ("id") ? UUID.fromString(jsonNode.get("id").getAsString()) : null;
            byte[] iv = Base64.decode(jsonNode.get("iv").getAsString());
            byte[] data = Base64.decode(jsonNode.get("data").getAsString());
            long modified = jsonNode.has("modified") ? jsonNode.get("modified").getAsLong() : 0;

            value = new EncryptedAesValue(id, modified, iv, data);
        }
        else
        {
            value = null;
        }

        return value;
    }

    /**
     * Writes encrypted value to JSON for a node.
     *
     * @param jsonNode JSON object to which to write the encrypted value's data
     * @param value the encrypted value to be written; can be null
     */
    void write(JsonObject jsonNode, EncryptedValue value)
    {
        if (value != null)
        {
            EncryptedAesValue aesValue = (EncryptedAesValue) value;

            String ivStr = Base64.toBase64String(aesValue.getIv());
            String dataStr = Base64.toBase64String(aesValue.getValue());

            jsonNode.addProperty("id", aesValue.getId().toString());
            jsonNode.addProperty("iv", ivStr);
            jsonNode.addProperty("data", dataStr);
            jsonNode.addProperty("modified", aesValue.getLastModified());
        }
    }

}
