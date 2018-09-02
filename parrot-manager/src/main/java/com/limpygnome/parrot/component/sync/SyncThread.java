package com.limpygnome.parrot.component.sync;

public abstract class SyncThread
{

    public abstract SyncResult execute(SyncOptions options, SyncProfile profile);

}
