package com.limpygnome.parrot.component.sync;

import com.limpygnome.parrot.library.log.Log;

/**
 * Used as aggregate of results for 'syncFinish' trigger event.
 */
public class SyncResult
{
    private String hostName;
    private Log log;
    private boolean success;
    private boolean changes;
    private long timestamp;

    public SyncResult(String hostName, Log log, boolean success, boolean changes)
    {
        this.hostName = hostName;
        this.log = log;
        this.success = success;
        this.changes = changes;
        this.timestamp = System.currentTimeMillis();
    }

    public String getHostName()
    {
        return hostName;
    }

    public Log getLog()
    {
        return log;
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

    // TODO needs message?
    public String asText()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(hostName).append(" [success: ").append(success)
                .append(", changes: ").append(changes)
                .append(", timestamp: ").append(timestamp).append("]")
                .append(System.getProperty("line.separator"))
                .append(log.asText());
        return sb.toString();
    }

}
