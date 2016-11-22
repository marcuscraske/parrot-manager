package com.limpygnome.parrot.model.node;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.model.Database;

import java.util.List;

/**
 * Represents a node in a database.
 *
 * Each node can then have children, which can have more nodes.
 */
public class DatabaseNode
{
    // Any sub-nodes which belong to this node
    public List<DatabaseNode> children;

    // The name of the node
    private String name;

    // The epoch time of when the node was last changed
    private long lastModified;

    // The value stored at this node
    private EncryptedAesValue value;

    private DatabaseNode(String name, long lastModified)
    {
        this.name = name;
        this.lastModified = lastModified;
    }

    /**
     * Creates a new node with already encrypted data.
     *
     * @param name the name of this node
     * @param lastModified the epoch time at which this node was last modified
     * @param value the encrypted value
     */
    public DatabaseNode(String name, long lastModified, EncryptedAesValue value)
    {
        this(name, lastModified);
        this.value = value;
    }

    public DatabaseNode(Database database, String name, long lastModified, byte[] data) throws Exception
    {
        this(name, lastModified);

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
     * Decrypts the value stored at this node and returns the data.
     *
     * This can be an empty array if the node does not store a value i.e. acts as a directory/label for a set of child
     * nodes.
     *
     * @return the decrypted value stored at this node
     * @throws Exception
     */
    public byte[] getValue() throws Exception
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
