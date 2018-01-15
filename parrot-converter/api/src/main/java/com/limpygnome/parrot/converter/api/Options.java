package com.limpygnome.parrot.converter.api;

/**
 * Generic conversion options.
 *
 * For format-specific options, extend this class.
 *
 * TODO no IDs / name-only identification / identify by name not ID
 */
public class Options
{
    private String format;
    private boolean remoteSync;

    public Options() { }

    /**
     * @return the format for conversion
     */
    public String getFormat()
    {
        return format;
    }

    /**
     * @param format the format for conversion
     */
    public void setFormat(String format)
    {
        this.format = format;
    }

    /**
     * @return indicates whether remote sync data, stored in database, should be converted (otherwise ignored)
     */
    public boolean isRemoteSync()
    {
        return remoteSync;
    }

    /**
     * @param remoteSync sets whether sync data is converted
     */
    public void setRemoteSync(boolean remoteSync)
    {
        this.remoteSync = remoteSync;
    }

}
