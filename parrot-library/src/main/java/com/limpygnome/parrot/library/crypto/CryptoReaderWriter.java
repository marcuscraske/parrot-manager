package com.limpygnome.parrot.library.crypto;

import java.security.SecureRandom;
import javax.crypto.SecretKey;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * Created by limpygnome on 20/02/17.
 */
public class CryptoReaderWriter
{

    /**
     * The default rounds used by cryptographic hashing functions.
     */
    public static final int ROUNDS_DEFAULT = 65536;

    private SecureRandom random;

    public CryptoReaderWriter()
    {
        random = new SecureRandom();
    }

    /**
     * Encrypts provided value.
     *
     * @param secretKey the secret ket
     * @param value the value to be encrypted
     * @return the encrypted wrapper
     * @throws Exception if the specified value cannot be encrypted
     */
    public EncryptedAesValue encrypt(SecretKey secretKey, byte[] value) throws Exception
    {
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
        EncryptedAesValue result = new EncryptedAesValue(iv, rawResult);
        return result;
    }

    /**
     * Decrypts provided wrapper object.
     *
     * @param secretKey the secret ket
     * @param value the encrypted (wrapper) object
     * @return the decrypted value
     * @throws Exception if the specified value cannot be decrypted
     */
    public byte[] decrypt(SecretKey secretKey, EncryptedAesValue value) throws Exception
    {
        CipherParameters cipherParams = new ParametersWithIV(new KeyParameter(secretKey.getEncoded()), value.getIv());
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
        aes.init(false, cipherParams);

        // Process data
        byte[] result = processCrypto(aes, value.getValue());
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
