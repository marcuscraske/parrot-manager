package com.limpygnome.parrot.library.io.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import com.limpygnome.parrot.library.db.DatabaseNodeHistory;

import java.util.UUID;

/**
 * Reads and writes instances of {@link com.limpygnome.parrot.library.db.DatabaseNodeHistory} in JSON.
 */
class DatabaseNodeHistoryReaderWriter
{
    private EncryptedValueJsonReaderWriter encryptedValueJsonReaderWriter;

    DatabaseNodeHistoryReaderWriter(EncryptedValueJsonReaderWriter encryptedValueJsonReaderWriter)
    {
        this.encryptedValueJsonReaderWriter = encryptedValueJsonReaderWriter;
    }

    void read(JsonObject json, DatabaseNodeHistory history)
    {
        // Historic values
        JsonArray jsonHistory = (JsonArray) json.get("history");

        if (jsonHistory != null)
        {
            EncryptedValue historicValue;
            JsonObject jsonHistoricValue;

            for (JsonElement rawHistory : jsonHistory)
            {
                jsonHistoricValue = rawHistory.getAsJsonObject();
                historicValue = encryptedValueJsonReaderWriter.read(jsonHistoricValue);
                history.add(historicValue);
            }
        }

        // Deleted historic values
        if (json.has("deleted-history"))
        {
            JsonArray jsonDeletedHistory = json.get("deleted-history").getAsJsonArray();

            UUID id;
            String txtId;

            for (JsonElement jsonElement : jsonDeletedHistory)
            {
                txtId = jsonElement.getAsString();
                id = UUID.fromString(txtId);
                history.addDeleted(id);
            }
        }
    }

    void write(JsonObject json, DatabaseNodeHistory history)
    {
        // Historic values
        JsonArray jsonHistory = new JsonArray();
        JsonObject jsonHistoryItem;

        for (EncryptedValue historicValue : history.fetch())
        {
            jsonHistoryItem = new JsonObject();
            encryptedValueJsonReaderWriter.write(jsonHistoryItem, historicValue);
            jsonHistory.add(jsonHistoryItem);
        }

        json.add("history", jsonHistory);

        // Deleted historic values
        JsonArray jsonDeletedHistory = new JsonArray();

        for (UUID id : history.getDeleted())
        {
            jsonDeletedHistory.add(id.toString());
        }

        json.add("deleted-history", jsonDeletedHistory);
    }

}
