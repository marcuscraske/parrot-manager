package com.limpygnome.parrot.component.remote;

import com.limpygnome.parrot.component.remote.ssh.SshOptions;

public abstract class RemoteSyncThread
{

    public abstract SyncResult execute(SshOptions options);

}
