package com.limpygnome.parrot.model.node;

/**
 * Created by limpygnome on 20/11/16.
 */
public class EncryptedAesValue
{
    private byte[] iv;
    private byte[] value;

    public EncryptedAesValue(byte[] iv, byte[] value)
    {
        this.iv = iv;
        this.value = value;
    }

    public byte[] getIv()
    {
        return iv;
    }

    public byte[] getValue()
    {
        return value;
    }

}
