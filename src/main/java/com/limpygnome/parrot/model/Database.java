package com.limpygnome.parrot.model;

import com.limpygnome.parrot.model.node.DatabaseNode;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.limpygnome.parrot.model.node.EncryptedAesValue;
import org.bouncycastle.asn1.dvcs.Data;
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
 *
 * This is also responsible for all cryptography for the database.
 */
public class Database
{
    // The length of secret keys
    private static final int KEY_LENGTH = 256;

    // The type of secret key
    private static final String SECRET_KEY_TYPE = "PBKDF2WithHmacSHA256";

    // THe minimum length of random bytes for a salt
    private static final int SALT_LENGTH_MIN = 32;

    // The maximum length of random bytes for a salt
    private static final int SALT_LENGTH_MAX = 64;

    // Default rounds
    public static final int ROUNDS_DEFAULT = 65536;

    // The salt randomly generated when the DB was created
    private byte[] salt;

    // The password for the database
    // TODO: actually read from somewhere
    private char[] password;

    // Number of rounds to perform - TODO: store in database
    private int rounds;

    // The actual secret key
    private SecretKey secretKey;

    // RNG to generate random bytes for IVs
    private SecureRandom random;

    // The root node of the database
    private DatabaseNode root;

    public Database()
    {
        // Setup RNG
        random = new SecureRandom();
    }

    public Database(char[] password, int rounds) throws Exception
    {
        this();

        this.password = password;
        this.rounds = rounds;

        // Generate random salt (with random length - 32 to 64 bytes)
        int saltLength = SALT_LENGTH_MIN + random.nextInt(SALT_LENGTH_MAX - SALT_LENGTH_MIN + 1);
        salt = new byte[saltLength];
        random.nextBytes(salt);

        // Setup initial root node
        root = new DatabaseNode(this, null, null);

        // Setup secret key
        setupSecretKey();
    }

    public Database(byte[] salt, char[] password, int rounds) throws Exception
    {
        this();

        this.salt = salt;
        this.password = password;
        this.rounds = rounds;

        setupSecretKey();
    }

    private void setupSecretKey() throws Exception
    {
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(SECRET_KEY_TYPE);

        KeySpec keySpec = new PBEKeySpec(password, salt, rounds, KEY_LENGTH);
        SecretKey tmp = secretKeyFactory.generateSecret(keySpec);
        secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    /**
     * Sets the root node of this database.
     *
     * @param node the node to become root
     */
    public void setRoot(DatabaseNode node)
    {
        this.root = node;
    }

    /**
     * Encrypts provided value.
     *
     * @param value the value to be encrypted
     * @return the encrypted wrapper
     * @throws Exception if the specified value cannot be encrypted
     */
    public EncryptedAesValue encrypt(byte[] value) throws Exception
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
     * @param value the encrypted (wrapper) object
     * @return the decrypted value
     * @throws Exception if the specified value cannot be decrypted
     */
    public byte[] decrypt(EncryptedAesValue value) throws Exception
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
