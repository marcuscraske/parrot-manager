package com.limpygnome.parrot.library.crypto;

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

    /**
     * The default rounds used by cryptographic hashing functions.
     */
    public static final int ROUNDS_DEFAULT = 65536;

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

        // In the result of null, just set to empty...
        if (value == null)
        {
            value = new byte[0];
        }

        // Build IV
        byte[] iv = new byte[16];
        random.nextBytes(iv);

        // Setup cipher
        CipherParameters cipherParams = new ParametersWithIV(new KeyParameter(secretKey.getEncoded()), iv);
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
        aes.init(true, cipherParams);

        // Process data
        byte[] rawResult = processCrypto(aes, value);

        // Build encrypted result
        EncryptedValue result = new EncryptedAesValue(System.currentTimeMillis(), iv, rawResult);
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

        CipherParameters cipherParams = new ParametersWithIV(new KeyParameter(secretKey.getEncoded()), aesValue.getIv());
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
        aes.init(false, cipherParams);

        // Process data
        byte[] result = processCrypto(aes, aesValue.getValue());
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
