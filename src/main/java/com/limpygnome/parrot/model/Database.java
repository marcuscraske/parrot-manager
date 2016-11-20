package com.limpygnome.parrot.model;

import com.limpygnome.parrot.model.node.DatabaseNode;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.security.Security;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

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

    public Database() throws Exception
    {
        // Add BouncyCastle as provider for JCE crypto
        Security.addProvider(new BouncyCastleProvider());

        // Build secret key
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_TYPE);

        KeySpec keySpec = new PBEKeySpec(password, salt, rounds, KEY_LENGTH);
        SecretKey tmp = secretKeyFactory.generateSecret(keySpec);
        secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    public byte[] encrypt(byte[] value) throws Exception
    {
        Cipher cipher = Cipher.getInstance(CIPHER_TYPE, BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        CipherOutputStream cos = new CipherOutputStream(baos, cipher);
        cos.write(value);
        cos.close();

        byte[] result = baos.toByteArray();
        return result;
    }

    public byte[] decrypt(byte[] value) throws Exception
    {
        Cipher cipher = Cipher.getInstance(CIPHER_TYPE, BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        ByteArrayInputStream bais = new ByteArrayInputStream(value);
        CipherInputStream cis = new CipherInputStream(bais, cipher);

        byte[] result = new byte[value.length];
        DataInputStream dis = new DataInputStream(cis);
        dis.readFully(result);

        return result;
    }

}
