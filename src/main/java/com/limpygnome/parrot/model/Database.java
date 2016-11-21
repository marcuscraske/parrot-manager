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

    // The salt randomly generated when the DB was created
    private byte[] salt;

    // The password for the database
    // TODO: actually read from somewhere
    private char[] password;

    // Number of rounds to perform - TODO: store in database
    private int rounds;

    // The actual secret key
    private SecretKey secretKey;

    // The root node of the database
    private DatabaseNode root;

    public Database()
    {
    }

    public Database(Controller controller, char[] password, int rounds) throws Exception
    {
        this();

        this.password = password;
        this.rounds = rounds;

        // Generate random salt (with random length - 32 to 64 bytes)
        salt = controller.getCryptographyService().generateRandomSalt();


        // Setup initial root node
        root = new DatabaseNode(this, null, (EncryptedAesValue) null);

        // Setup secret key
        secretKey = controller.getCryptographyService().createSecretKey(password, salt, rounds);
    }

    public Database(Controller controller, byte[] salt, char[] password, int rounds) throws Exception
    {
        this();

        this.salt = salt;
        this.password = password;
        this.rounds = rounds;

        secretKey = controller.getCryptographyService().createSecretKey(password, salt, rounds);
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



}
