package com.limpygnome.parrot.component.sync;

import com.limpygnome.parrot.component.ui.WebStageInitService;
import com.limpygnome.parrot.event.DatabaseChangingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to hold sync result data (to prevent GC) and pass it to the front-end through events.
 */
@Service
public class SyncResultService implements DatabaseChangingEvent
{
    @Autowired
    private WebStageInitService webStageInitService;

    // Stores results to prevent GC
    private Map<String, SyncResult> resultMap;

    // Cache of results; again, prevents GC and also improves performance
    private SyncResult[] results;

    public SyncResultService()
    {
        this.resultMap = new HashMap<>();
        this.results = new SyncResult[0];
    }

    public synchronized void add(SyncResult syncResult)
    {
        if (syncResult != null)
        {
            resultMap.put(syncResult.getHostName(), syncResult);
            updateCache();
            raiseEventChanged();
        }
    }

    public synchronized void clear()
    {
        resultMap.clear();
        updateCache();
        raiseEventChanged();
    }

    public SyncResult[] getResults()
    {
        return results;
    }

    public String getResultsAsText()
    {
        StringBuilder sb = new StringBuilder();
        String ls = System.getProperty("line.separator");
        for (SyncResult syncResult : results)
        {
            sb.append(syncResult.asText()).append(ls);
        }
        return sb.toString();
    }

    @Override
    public void eventDatabaseChanged(boolean open)
    {
        // Wipe data when database changing
        clear();
    }

    private void raiseEventChanged()
    {
        webStageInitService.triggerEvent("document", "remoteSyncLogChange", results);
    }

    private void updateCache()
    {
        results = resultMap.values().toArray(new SyncResult[resultMap.size()]);
    }

}
