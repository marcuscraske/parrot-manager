package com.limpygnome.parrot.library.crypto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import javax.crypto.SecretKey;
import java.security.SecureRandom;

/**
 * Used for encrypting and decrypting data.
 *
 * THe current implementation is to use AES - refer to {@link EncryptedAesValue}.
 */
public class CryptoReaderWriter
{
    private static final Logger LOG = LogManager.getLogger(CryptoReaderWriter.class);

    private SecureRandom random;

    /**
     * Creates a new instance.
     */
    public CryptoReaderWriter()
    {
        random = new SecureRandom();
    }

    /**
     * Encrypts provided value.
     *
     * @param cryptoParams params
     * @param value the value to be encrypted
     * @return the encrypted wrapper
     * @throws Exception if the specified value cannot be encrypted
     */
    public EncryptedValue encrypt(CryptoParams cryptoParams, byte[] value) throws Exception
    {
        SecretKey secretKey = cryptoParams.getSecretKey();
        byte[] iv, encryptedBytes;

        if (value != null)
        {
            // Build IV
            iv = new byte[16];
            random.nextBytes(iv);

            // Setup cipher
            CipherParameters cipherParams = new ParametersWithIV(new KeyParameter(secretKey.getEncoded()), iv);
            PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
            aes.init(true, cipherParams);

            // Process data
            encryptedBytes = processCrypto(aes, value);
        }
        else
        {
            iv = null;
            encryptedBytes = null;
        }

        // Build encrypted result
        EncryptedValue result = new EncryptedAesValue(System.currentTimeMillis(), iv, encryptedBytes);
        return result;
    }

    /**
     * Decrypts provided wrapper object.
     *
     * @param cryptoParams params
     * @param value the encrypted (wrapper) object
     * @return the decrypted value
     * @throws Exception if the specified value cannot be decrypted
     */
    public byte[] decrypt(CryptoParams cryptoParams, EncryptedValue value) throws Exception
    {
        SecretKey secretKey = cryptoParams.getSecretKey();
        EncryptedAesValue aesValue = (EncryptedAesValue) value;

        byte[] iv = aesValue.getIv();
        byte[] encryptedBytes = aesValue.getValue();
        byte[] result;

        if (iv != null && encryptedBytes != null)
        {
            try
            {
                CipherParameters cipherParams = new ParametersWithIV(new KeyParameter(secretKey.getEncoded()), aesValue.getIv());
                PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
                aes.init(false, cipherParams);

                // Process data
                result = processCrypto(aes, aesValue.getValue());
            }
            catch (Exception e)
            {
                LOG.error("fatal - failed to decrypt value", e);
                result = null;
            }
        }
        else
        {
            result = null;
        }

        return result;
    }

    private byte[] processCrypto(PaddedBufferedBlockCipher aes, byte[] data) throws Exception
    {
        int minSize = aes.getOutputSize(data.length);
        byte[] outBuf = new byte[minSize];
        int length1 = aes.processBytes(data, 0, data.length, outBuf, 0);
        int length2 = aes.doFinal(outBuf, length1);
        int actualLength = length1 + length2;
        byte[] result = new byte[actualLength];
        System.arraycopy(outBuf, 0, result, 0, result.length);
        return result;
    }

}
