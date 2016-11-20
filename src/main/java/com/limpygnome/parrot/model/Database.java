package com.limpygnome.parrot.model;

import com.limpygnome.parrot.model.node.DatabaseNode;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * Represents a database for storing confidential details.
 *
 * Simple tree structure with a root node, which breaks down into recursive child nodes.
 */
public class Database
{
    // The length of secret keys
    private static final int KEY_LENGTH = 256;
    // The type of secret key
    private static final String SECRET_KEY_TYPE = "PBKDF2WithHmacSHA256";
    // The type of cipher to use
    private static final String CIPHER_TYPE = "AES/CBC/PKCS5Padding";

    // The salt randomly generated when the DB was created
    private byte[] salt = "12345".getBytes();

    // The password for the database
    // TODO: actually read from somewhere
    private char[] password = "TEST".toCharArray();

    // Number of rounds to perform - TODO: store in database
    private int rounds = 65536;

    // The actual secret key
    private SecretKey secretKey;

    // The root node of the database
    private DatabaseNode root;

    private SecureRandom random;

    public Database() throws Exception
    {
        random = new SecureRandom();

//        // Add BouncyCastle as provider for JCE crypto
//        Security.addProvider(new BouncyCastleProvider());

        // Build secret key
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_TYPE);

        KeySpec keySpec = new PBEKeySpec(password, salt, rounds, KEY_LENGTH);
        SecretKey tmp = secretKeyFactory.generateSecret(keySpec);
        secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    public byte[] encrypt(byte[] value) throws Exception
    {
//        Cipher cipher = Cipher.getInstance(CIPHER_TYPE, BouncyCastleProvider.PROVIDER_NAME);
//        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//        CipherOutputStream cos = new CipherOutputStream(baos, cipher);
//        cos.write(value);
//        cos.close();
//
//        byte[] result = baos.toByteArray();
//        return result;

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
        byte[] result = processCrypto(aes, value);
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

    public byte[] decrypt(byte[] iv, byte[] value) throws Exception
    {
//        Cipher cipher = Cipher.getInstance(CIPHER_TYPE, BouncyCastleProvider.PROVIDER_NAME);
//        cipher.init(Cipher.DECRYPT_MODE, secretKey);
//
//        ByteArrayInputStream bais = new ByteArrayInputStream(value);
//        CipherInputStream cis = new CipherInputStream(bais, cipher);
//
//        byte[] result = new byte[value.length];
//        DataInputStream dis = new DataInputStream(cis);
//        dis.readFully(result);
//
//        return result;

        CipherParameters cipherParams = new ParametersWithIV(new KeyParameter(secretKey.getEncoded()), iv);
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
        aes.init(false, cipherParams);

        // Process data
        byte[] result = processCrypto(aes, value);
        return result;
    }

}
