package com.limpygnome.parrot.component.recentFile;

import java.io.File;

/**
 * Represents a recent file opened.
 */
public class RecentFile
{
    private String fileName;
    private String fullPath;

    public RecentFile() { }

    public RecentFile(File file)
    {
        // Read file-name (but exclude database extension)
        final String EXTENSION_REMOVED_FROM_FILENAME = ".parrot";

        String fileName = file.getName();

        if (fileName.endsWith(EXTENSION_REMOVED_FROM_FILENAME) && fileName.length() > EXTENSION_REMOVED_FROM_FILENAME.length())
        {
            fileName = fileName.substring(0, fileName.length() - EXTENSION_REMOVED_FROM_FILENAME.length());
        }

        this.fileName = fileName;

        // Read full path for opening later
        this.fullPath = file.getAbsolutePath();
    }

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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RecentFile that = (RecentFile) o;

        if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) return false;
        return fullPath != null ? fullPath.equals(that.fullPath) : that.fullPath == null;

    }

    @Override
    public int hashCode()
    {
        int result = fileName != null ? fileName.hashCode() : 0;
        result = 31 * result + (fullPath != null ? fullPath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "RecentFile{" +
                "fileName='" + fileName + '\'' +
                ", fullPath='" + fullPath + '\'' +
                '}';
    }

}
