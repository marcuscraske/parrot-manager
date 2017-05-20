package com.limpygnome.parrot.component.remote;

/**
 * Used as aggregate of results for 'remoteSyncFinish' trigger event.
 */
public class SyncResult
{
    private String messages;
    private boolean success;
    private boolean changes;
    private String hostName;

    public SyncResult(String messages, boolean success, boolean changes, String hostName)
    {
        this.messages = messages;
        this.success = success;
        this.changes = changes;
        this.hostName = hostName;
    }

    public String getMessages()
    {
        return messages;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public boolean isChanges()
    {
        return changes;
    }

    public String getHostName()
    {
        return hostName;
    }

}
