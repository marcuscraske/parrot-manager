package com.limpygnome.parrot.model.node;

import com.limpygnome.parrot.model.Database;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a node in a database.
 *
 * Each node can then have children, which can have more nodes.
 */
public class DatabaseNode
{
    // The database to which this belongs
    private Database database;

    // Any sub-nodes which belong to this node
    private List<DatabaseNode> children;

    // The name of the node
    private String name;

    // The epoch time of when the node was last changed
    private long lastModified;

    // The value stored at this node
    private EncryptedAesValue value;

    private DatabaseNode(Database database, String name, long lastModified)
    {
        this.database = database;
        this.name = name;
        this.lastModified = lastModified;

        this.children = new LinkedList<>();
    }

    /**
     * Creates a new node with already encrypted data.
     *
     * @param database the DB to which this belongs
     * @param name the name of this node
     * @param lastModified the epoch time at which this node was last modified
     * @param value the encrypted value
     */
    public DatabaseNode(Database database, String name, long lastModified, EncryptedAesValue value)
    {
        this(database, name, lastModified);
        this.value = value;
    }

    /**
     * Creates a new node for unecrypted data, which is encrypted by this constructor.
     *
     * @param database the DB to whcih this belongs
     * @param name the name of this node
     * @param lastModified the epoch time at which this node was last modified
     * @param data unencrypted data
     * @throws Exception thrown if the data cannot be encrypted
     */
    public DatabaseNode(Database database, String name, long lastModified, byte[] data) throws Exception
    {
        this(database, name, lastModified);

        // Encrypt the data
        this.value = database.encrypt(data);
    }

    /**
     * @return the name of the node; purely for presentation purposes
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the last modified date
     */
    public long getLastModified()
    {
        return lastModified;
    }

    /**
     * @return the encrypted value, as stored in memory
     */
    public EncryptedAesValue getValue()
    {
        return value;
    }

    /**
     * Decrypts the value stored at this node and returns the data.
     *
     * This can be an empty array if the node does not store a value i.e. acts as a directory/label for a set of child
     * nodes.
     *
     * @return the decrypted value stored at this node
     * @throws Exception
     */
    public byte[] getDecryptedValue() throws Exception
    {
        byte[] result = database.decrypt(value);
        return result;
    }

    /**
     * @return the child nodes
     */
    public List<DatabaseNode> getChildren()
    {
        return children;
    }

}
