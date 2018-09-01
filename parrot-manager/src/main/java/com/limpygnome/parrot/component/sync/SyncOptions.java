package com.limpygnome.parrot.component.sync;

public class SyncOptions
{
    private String databasePassword;

    public SyncOptions() { }

    public SyncOptions(String databasePassword)
    {
        this.databasePassword = databasePassword;
    }

    public void setDatabasePassword(String databasePassword)
    {
        this.databasePassword = databasePassword;
    }

    public String getDatabasePassword()
    {
        return databasePassword;
    }

}
