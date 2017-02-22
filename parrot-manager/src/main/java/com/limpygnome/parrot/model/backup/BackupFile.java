package com.limpygnome.parrot.model.backup;

import org.joda.time.DateTime;

import java.io.File;

/**
 * Used to represent a backup file.
 */
public class BackupFile
{
    private String name;
    private long lastModified;

    public BackupFile(File file)
    {
        this.name = file.getName();
        this.lastModified = file.lastModified();
    }

    public String getName()
    {
        return name;
    }

    public String getCreated()
    {
        DateTime dateTime = new DateTime(lastModified);
        return dateTime.toString("dd-MM-yyyy HH:mm:ss");
    }

}
