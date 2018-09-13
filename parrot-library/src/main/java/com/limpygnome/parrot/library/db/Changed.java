package com.limpygnome.parrot.library.db;

/**
 * Tracks whether local or remote changes have occurred.
 */
public class Changed
{
    private boolean local;
    private boolean remote;

    public void localChanged()
    {
        local = true;
    }

    public void remoteChanged()
    {
        remote = true;
    }

    public boolean isLocal()
    {
        return local;
    }

    public boolean isRemote()
    {
        return remote;
    }

    public boolean isAnyChange()
    {
        return local || remote;
    }

    public void merge(Changed... changes)
    {
        for (Changed changed : changes)
        {
            local |= changed.local;
            remote |= changed.remote;
        }
    }

}
