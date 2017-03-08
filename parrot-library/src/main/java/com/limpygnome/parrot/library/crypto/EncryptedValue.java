package com.limpygnome.parrot.library.crypto;

import org.joda.time.DateTime;

/**
 * Immutable generic encrypted value.
 *
 * Use {@link CryptoReaderWriter} for interaction.
 */
public abstract class EncryptedValue
{
    // The epoch time at which this value was last modified
    private long lastModified;

    protected EncryptedValue(long lastModified)
    {
        this.lastModified = lastModified;
    }

    /**
     * @return epoch time of when this value was last modified
     */
    public final long getLastModified()
    {
        return lastModified;
    }

    /**
     * @return formatted date time
     */
    public final String getFormattedLastModified()
    {
        DateTime dateTime = new DateTime(lastModified);
        return dateTime.toString("dd-MM-yyyy HH:mm:ss");
    }

    /**
     * @return cloned instance
     */
    public abstract EncryptedValue clone();

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EncryptedValue that = (EncryptedValue) o;

        return lastModified == that.lastModified;

    }

    @Override
    public int hashCode()
    {
        return (int) (lastModified ^ (lastModified >>> 32));
    }

}
