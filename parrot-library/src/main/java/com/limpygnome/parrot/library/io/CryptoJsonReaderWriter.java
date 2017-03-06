package com.limpygnome.parrot.library.io;

import com.limpygnome.parrot.library.crypto.EncryptedAesValue;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONObject;

/**
 * Reads/writes encrypted values.
 *
 * Current implementation is to use AES - refer to {@link EncryptedAesValue}.
 */
public class CryptoJsonReaderWriter
{

    /**
     * Reads an encrypted value from a node's serialized JSON.
     *
     * @param jsonNode serialized JSON object
     * @return the value, or null
     */
    public EncryptedValue read(JSONObject jsonNode)
    {
        EncryptedValue value;

        if (jsonNode.containsKey("iv") && jsonNode.containsKey("data"))
        {
            byte[] iv = Base64.decode((String) jsonNode.get("iv"));
            byte[] data = Base64.decode((String) jsonNode.get("data"));
            long modified = (Long) jsonNode.get("modified");

            value = new EncryptedAesValue(modified, iv, data);
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
    public void write(JSONObject jsonNode, EncryptedValue value)
    {
        if (value != null)
        {
            EncryptedAesValue aesValue = (EncryptedAesValue) value;

            String ivStr = Base64.toBase64String(aesValue.getIv());
            String dataStr = Base64.toBase64String(aesValue.getValue());

            jsonNode.put("iv", ivStr);
            jsonNode.put("data", dataStr);
        }
    }

}
