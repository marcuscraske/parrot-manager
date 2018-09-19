package com.limpygnome.parrot.component.sync;

public class SyncOptions
{
    private String databasePassword;
    private String destinationPath;

    public SyncOptions() { }

    public SyncOptions(SyncOptions options)
    {
        if (options != null)
        {
            this.databasePassword = options.databasePassword;
            this.destinationPath = options.destinationPath;
        }
    }

    public SyncOptions(String databasePassword, String destinationPath)
    {
        this.databasePassword = databasePassword;
        this.destinationPath = destinationPath;
    }

    public boolean isDatabasePassword()
    {
        return databasePassword != null && databasePassword.length() > 0;
    }

    public String getDatabasePassword()
    {
        return databasePassword;
    }

    public void setDatabasePassword(String databasePassword)
    {
        this.databasePassword = databasePassword;
    }

    public String getDestinationPath()
    {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath)
    {
        this.destinationPath = destinationPath;
    }

}
