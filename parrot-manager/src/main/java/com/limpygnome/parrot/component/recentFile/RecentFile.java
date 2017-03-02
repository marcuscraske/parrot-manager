package com.limpygnome.parrot.component.recentFile;

/**
 * Represents a recent file opened.
 */
public class RecentFile
{
    private String fileName;
    private String fullPath;

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFullPath()
    {
        return fullPath;
    }

    public void setFullPath(String fullPath)
    {
        this.fullPath = fullPath;
    }

}
