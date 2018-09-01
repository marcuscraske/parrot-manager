package com.limpygnome.parrot.component.sync;

import com.limpygnome.parrot.component.sync.SyncResult;
import com.limpygnome.parrot.component.sync.ssh.SshOptions;

public abstract class SyncThread
{

    public abstract SyncResult execute(SshOptions options);

}
