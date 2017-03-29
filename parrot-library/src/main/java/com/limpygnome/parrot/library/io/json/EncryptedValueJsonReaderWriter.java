package com.limpygnome.parrot.library.io.json;

import com.limpygnome.parrot.library.crypto.EncryptedAesValue;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONObject;

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
    EncryptedValue read(JSONObject jsonNode)
    {
        EncryptedValue value;

        if (jsonNode.containsKey("iv") && jsonNode.containsKey("data"))
        {
            UUID id = jsonNode.containsKey ("id") ? UUID.fromString((String) jsonNode.get("id")) : null;
            byte[] iv = Base64.decode((String) jsonNode.get("iv"));
            byte[] data = Base64.decode((String) jsonNode.get("data"));
            long modified = jsonNode.containsKey("modified") ? (Long) jsonNode.get("modified") : 0;

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
    void write(JSONObject jsonNode, EncryptedValue value)
    {
        if (value != null)
        {
            EncryptedAesValue aesValue = (EncryptedAesValue) value;

            String ivStr = Base64.toBase64String(aesValue.getIv());
            String dataStr = Base64.toBase64String(aesValue.getValue());

            jsonNode.put("id", aesValue.getId());
            jsonNode.put("iv", ivStr);
            jsonNode.put("data", dataStr);
            jsonNode.put("modified", aesValue.getLastModified());
        }
    }

}
