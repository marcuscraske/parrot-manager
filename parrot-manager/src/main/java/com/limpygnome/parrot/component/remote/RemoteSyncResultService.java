package com.limpygnome.parrot.component.remote;

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
public class RemoteSyncResultService implements DatabaseChangingEvent
{
    @Autowired
    private WebStageInitService webStageInitService;

    // Stores results to prevent GC
    private Map<String, SyncResult> resultMap;

    // Cache of results; again, prevents GC and also improves performance
    private SyncResult[] results;

    public RemoteSyncResultService()
    {
        this.resultMap = new HashMap<>();
        this.results = new SyncResult[0];
    }

    public synchronized void add(SyncResult result)
    {
        resultMap.put(result.getHostName(), result);
        updateCache();
        raiseEventFinished(result);
        raiseEventChanged();
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

    @Override
    public void eventDatabaseChanged(boolean open)
    {
        // Wipe data when database changing
        clear();
    }

    private void raiseEventFinished(SyncResult result)
    {
        webStageInitService.triggerEvent("document", "remoteSyncFinish", result);
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
