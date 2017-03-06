package com.limpygnome.parrot.library.crypto;

import java.util.Arrays;

/**
 * Used to hold AES encrypted binary / byte array.
 */
public class EncryptedAesValue extends EncryptedValue
{
    private byte[] iv;
    private byte[] value;

    /**
     * creates a new instance.
     *
     * @param lastModified epoch time at which last modified
     * @param iv initialization vector
     * @param value encrypted byte data
     */
    public EncryptedAesValue(long lastModified, byte[] iv, byte[] value)
    {
        super(lastModified);

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
                getLastModified(), iv.clone(), value.clone()
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
