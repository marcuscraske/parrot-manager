package com.limpygnome.parrot.model.node;

import com.limpygnome.parrot.model.Database;

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
    public List<DatabaseNode> children;

    // The name of the node
    private String name;

    // The value stored at this node
    private EncryptedAesValue value;

    public DatabaseNode(Database database, String name, EncryptedAesValue value)
    {
        this.database = database;
        this.name = name;
        this.value = value;
    }

    public DatabaseNode(Database database, String name, byte[] data) throws Exception
    {
        this.database = database;
        this.name = name;
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
