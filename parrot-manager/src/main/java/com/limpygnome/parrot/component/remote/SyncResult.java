package com.limpygnome.parrot.component.remote;

import com.limpygnome.parrot.library.db.log.MergeLog;

/**
 * Used as aggregate of results for 'remoteSyncFinish' trigger event.
 */
public class SyncResult
{
    private String hostName;
    private MergeLog mergeLog;
    private boolean success;
    private boolean changes;
    private long timestamp;

    public SyncResult(String hostName, MergeLog mergeLog, boolean success, boolean changes)
    {
        this.hostName = hostName;
        this.mergeLog = mergeLog;
        this.success = success;
        this.changes = changes;
        this.timestamp = System.currentTimeMillis();
    }

    public String getHostName()
    {
        return hostName;
    }

    public MergeLog getMergeLog()
    {
        return mergeLog;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public boolean isChanges()
    {
        return changes;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

}
