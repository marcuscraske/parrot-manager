package com.limpygnome.parrot.library.io.json;

import com.limpygnome.parrot.library.crypto.EncryptedValue;
import com.limpygnome.parrot.library.db.DatabaseNodeHistory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

    void read(JSONObject json, DatabaseNodeHistory history)
    {
        // Historic values
        JSONArray jsonHistory = (JSONArray) json.get("history");

        if (jsonHistory != null)
        {
            EncryptedValue historicValue;
            JSONObject jsonHistoricValue;
            for (Object rawHistory : jsonHistory)
            {
                jsonHistoricValue = (JSONObject) rawHistory;
                historicValue = encryptedValueJsonReaderWriter.read(jsonHistoricValue);
                history.add(historicValue);
            }
        }

        // Deleted historic values
        JSONArray jsonDeletedHistory = (JSONArray) json.get("deleted-history");

        if (jsonDeletedHistory != null)
        {
            UUID id;
            for (Object rawId : jsonDeletedHistory)
            {
                id = UUID.fromString((String) rawId);
                history.addDeleted(id);
            }
        }
    }

    void write(JSONObject json, DatabaseNodeHistory history)
    {
        // Historic values
        JSONArray jsonHistory = new JSONArray();
        JSONObject jsonHistoryItem;

        for (EncryptedValue historicValue : history.fetch())
        {
            jsonHistoryItem = new JSONObject();
            encryptedValueJsonReaderWriter.write(jsonHistoryItem, historicValue);
            jsonHistory.add(jsonHistoryItem);
        }

        json.put("history", jsonHistory);

        // Deleted historic values
        JSONArray jsonDeletedHistory = new JSONArray();

        for (UUID id : history.getDeleted())
        {
            jsonDeletedHistory.add(id.toString());
        }
        json.put("deleted-history", jsonDeletedHistory);
    }

}
