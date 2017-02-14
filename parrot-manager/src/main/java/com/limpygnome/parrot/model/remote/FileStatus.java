package com.limpygnome.parrot.model.remote;

/**
 * Used to track the status of a file upload/download.
 */
public class FileStatus
{
    private long current;
    private long max;

    /**
     * @return indicates if the download/upload has started
     */
    public boolean isStarted()
    {
        return max > 0;
    }

    /**
     * @return number of bytes transferred
     */
    public long getCurrent()
    {
        return current;
    }

    public void setCurrent(long current)
    {
        this.current = current;
    }

    /**
     * @return maximum number of bytes
     */
    public long getMax()
    {
        return max;
    }

    public void setMax(long max)
    {
        this.max = max;
    }

}
