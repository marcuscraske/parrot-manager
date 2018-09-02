package com.limpygnome.parrot.component.sync;

public class SyncOptions
{
    private String databasePassword;
    private String destinationPath;

    public SyncOptions() { }

    public SyncOptions(String databasePassword, String destinationPath)
    {
        this.databasePassword = databasePassword;
        this.destinationPath = destinationPath;
    }

    public String getDatabasePassword()
    {
        return databasePassword;
    }

    public String getDestinationPath()
    {
        return destinationPath;
    }

}
