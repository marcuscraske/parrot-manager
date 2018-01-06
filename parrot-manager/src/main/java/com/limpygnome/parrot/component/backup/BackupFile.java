package com.limpygnome.parrot.component.backup;

import java.io.File;

/**
 * Used to represent a backup file.
 */
public class BackupFile
{
    private String name;
    private String path;
    private long lastModified;

    public BackupFile(File file)
    {
        this.name = file.getName();
        this.path = file.getAbsolutePath();
        this.lastModified = file.lastModified();
    }

    public String getPath()
    {
        return path;
    }

    public String getName()
    {
        return name;
    }

    public long getLastModified()
    {
        return lastModified;
    }

}
