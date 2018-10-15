package com.limpygnome.parrot.library.io.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.limpygnome.parrot.library.db.DatabaseNode;

import java.util.Map;

/**
 * Reads and writes node properties.
 *
 * These are persisted locally only.
 */
class DatabaseNodePropertiesReaderWriter
{

    void read(JsonObject json, DatabaseNode node)
    {
        JsonObject properties = json.getAsJsonObject("local-properties");

        if (properties != null)
        {
            String key, value;
            for (Map.Entry<String, JsonElement> kv : properties.entrySet())
            {
                key = kv.getKey();
                value = kv.getValue().getAsString();
                node.setLocalProperty(key, value, false);
            }
        }
    }

    void write(JsonObject json, DatabaseNode node)
    {
        Map<String, String> properties = node.getLocalProperties();

        if (properties != null && !properties.isEmpty())
        {
            JsonObject jsonProperties = new JsonObject();
            for (Map.Entry<String, String> kv : properties.entrySet())
            {
                jsonProperties.addProperty(kv.getKey(), kv.getValue());
            }

            json.add("local-properties", jsonProperties);
        }
    }

}
