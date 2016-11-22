package com.limpygnome.parrot.model;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.model.node.DatabaseNode;
import com.limpygnome.parrot.model.node.EncryptedAesValue;

import javax.crypto.SecretKey;

/**
 * Represents a database for storing confidential details.
 *
 * Simple tree structure with a root node, which breaks down into recursive child nodes.
 *
 * This is also responsible for all cryptography for the database.
 */
public class Database
{
    // An instance of the current controller
    private Controller controller;

    // The salt randomly generated when the DB was created
    private byte[] salt;

    // Number of rounds to perform
    private int rounds;

    // The actual secret key
    private SecretKey secretKey;

    // The root node of the database
    private DatabaseNode root;

    private Database(Controller controller)
    {
        this.controller = controller;
    }

    public Database(Controller controller, char[] password, int rounds) throws Exception
    {
        this(controller);

        this.rounds = rounds;

        // Generate random salt (with random length - 32 to 64 bytes)
        salt = controller.getCryptographyService().generateRandomSalt();

        // Setup initial root node
        root = new DatabaseNode(null, 0, null);

        // Setup secret key
        secretKey = controller.getCryptographyService().createSecretKey(password, salt, rounds);
    }

    public Database(Controller controller, byte[] salt, char[] password, int rounds) throws Exception
    {
        this(controller);

        this.salt = salt;
        this.rounds = rounds;

        secretKey = controller.getCryptographyService().createSecretKey(password, salt, rounds);
    }

    /**
     * @return the root node of this database
     */
    public DatabaseNode getRoot()
    {
        return root;
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
     * Performs memory encryption on data.
     *
     * Intended for encrypting data for nodes.
     *
     * @param data the data to be encrypted
     * @return the encrypted wrapper
     * @throws Exception
     */
    public EncryptedAesValue encrypt(byte[] data) throws Exception
    {
        EncryptedAesValue value = controller.getCryptographyService().encrypt(secretKey, data);
        return value;
    }

    /**
     * Performs memory decryption.
     *
     * Intended for decrypting data for nodes.
     *
     * @param data the encrypted wrapper
     * @return the decrypted data
     * @throws Exception
     */
    public byte[] decrypt(EncryptedAesValue data) throws Exception
    {
        byte[] value = controller.getCryptographyService().decrypt(secretKey, data);
        return value;
    }

    /**
     * @return the salt used for in-memory encryption
     */
    public byte[] getSalt()
    {
        return salt;
    }

    /**
     * @return the rounds used for in-memory encryption
     */
    public int getRounds()
    {
        return rounds;
    }

}
