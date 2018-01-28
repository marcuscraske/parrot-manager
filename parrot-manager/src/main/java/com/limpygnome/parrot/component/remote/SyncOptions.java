package com.limpygnome.parrot.component.remote;

import com.limpygnome.parrot.component.remote.ssh.SshOptions;

public class SyncOptions
{
    private SshOptions options;
    private String databasePassword;

    public SyncOptions(SshOptions options, String databasePassword)
    {
        this.options = options;
        this.databasePassword = databasePassword;
    }

    public SshOptions getOptions()
    {
        return options;
    }

    public String getDatabasePassword()
    {
        return databasePassword;
    }

}
