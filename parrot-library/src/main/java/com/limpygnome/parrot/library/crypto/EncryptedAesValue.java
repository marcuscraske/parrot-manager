package com.limpygnome.parrot.library.crypto;

import java.util.Arrays;
import java.util.UUID;

/**
 * Used to hold AES encrypted binary / byte array.
 */
public class EncryptedAesValue extends EncryptedValue
{
    private byte[] iv;
    private byte[] value;

    /**
     * Creates a new instance.
     *
     * @param lastModified epoch time at which last modified
     * @param iv initialization vector
     * @param value encrypted byte data
     */
    public EncryptedAesValue(long lastModified, byte[] iv, byte[] value)
    {
        this(null, lastModified, iv, value);
    }

    /**
     * Creates a new instance.
     *
     * @param id unique identifier for this value; can be null (although a random value will be generated)
     * @param lastModified epoch time at which last modified
     * @param iv initialization vector
     * @param value encrypted byte data
     */
    public EncryptedAesValue(UUID id, long lastModified, byte[] iv, byte[] value)
    {
        super(id, lastModified);

        this.iv = iv;
        this.value = value;
    }

    /**
     * @return initialization vector
     */
    public byte[] getIv()
    {
        return iv;
    }

    /**
     * @return encrypted byte data
     */
    public byte[] getValue()
    {
        return value;
    }

    @Override
    public EncryptedValue clone()
    {
        EncryptedAesValue clone = new EncryptedAesValue(
                getUuid(), getLastModified(), iv.clone(), value.clone()
        );

        return clone;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EncryptedAesValue that = (EncryptedAesValue) o;

        if (!Arrays.equals(iv, that.iv)) return false;
        return Arrays.equals(value, that.value);

    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + Arrays.hashCode(iv);
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }

}
