package com.limpygnome.parrot.component.sync;

import com.limpygnome.parrot.library.log.Log;

/**
 * Used as aggregate of results for 'syncFinish' trigger event.
 */
public class SyncResult
{
    private String profileId;
    private String hostName;
    private Log log;
    private boolean success;
    private boolean changes;
    private long timestamp;

    public SyncResult(SyncProfile profile, Log log, boolean success, boolean changes)
    {
        if (profile == null)
        {
            throw new IllegalStateException("Profile arg is mandatory");
        }

        this.profileId = profile.getId();
        this.hostName = profile.getName();
        this.log = log;
        this.success = success;
        this.changes = changes;
        this.timestamp = System.currentTimeMillis();
    }

    public String getProfileId()
    {
        return profileId;
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
    // TODO profile id?
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
