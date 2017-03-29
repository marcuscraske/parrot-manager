package com.limpygnome.parrot.library.io.json;

import com.limpygnome.parrot.library.crypto.EncryptedValue;
import com.limpygnome.parrot.library.db.DatabaseNodeHistory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

    DatabaseNodeHistory read(JSONObject json, DatabaseNodeHistory history)
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
        // TODO: complete...
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
        // TODO: complete...
    }

}
