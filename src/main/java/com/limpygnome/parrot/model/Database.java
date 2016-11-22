package com.limpygnome.parrot.model;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.model.node.DatabaseNode;
import com.limpygnome.parrot.model.node.EncryptedAesValue;
import com.limpygnome.parrot.model.params.CryptoParams;

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

    // Params used for file crypto
    private CryptoParams fileCryptoParams;

    // Params used for memory crypto
    private CryptoParams memoryCryptoParams;

    // The root node of the database
    private DatabaseNode root;

    /**
     * Creates a new instance.
     *
     * @param controller current instance
     * @param memoryCryptoParams params for in-memory crypto
     * @param fileCryptoParams params used for file crypto; only required for writing to file later, can be null
     */
    public Database(Controller controller, CryptoParams memoryCryptoParams, CryptoParams fileCryptoParams)
    {
        this.controller = controller;
        this.memoryCryptoParams = memoryCryptoParams;
        this.fileCryptoParams = fileCryptoParams;

        // Setup an initial blank root node
        root = new DatabaseNode(null, 0, null);
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
    public synchronized void setRoot(DatabaseNode node)
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
    public synchronized EncryptedAesValue encrypt(byte[] data) throws Exception
    {
        EncryptedAesValue value = controller.getCryptographyService().encrypt(memoryCryptoParams.getSecretKey(), data);
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
    public synchronized byte[] decrypt(EncryptedAesValue data) throws Exception
    {
        byte[] value = controller.getCryptographyService().decrypt(memoryCryptoParams.getSecretKey(), data);
        return value;
    }

    /**
     * @return params used for file crypto; can be null if this DB is not written to file
     */
    public CryptoParams getFileCryptoParams()
    {
        return fileCryptoParams;
    }

    /**
     * @return params used for in-memory crypto
     */
    public CryptoParams getMemoryCryptoParams()
    {
        return memoryCryptoParams;
    }

}
