package com.limpygnome.parrot.model.node;

import com.limpygnome.parrot.model.Database;
import java.util.Map;

/**
 * Represents a node in a database.
 *
 * Each node can then have children, which can have more nodes or just values.
 *
 * Password Storage
 * ----------------
 * Where possible, the decrypted value should not be handled as a string, as to avoid being stored immutably in memory.
 * This is a precaution against buffer overflow attacks.
 *
 * Overall this cannot be entirely prevented with regards to the presentation layer. But this at least does not expose
 * the entire database in memory, but only as and if required.
 */
public class DatabaseNode
{
    // The database to which this belongs
    private Database database;

    // Any sub-nodes which belong to this node
    public Map<String, DatabaseNode> children;

    // The name of the node
    private String name;

    // The value stored at this node
    private EncryptedAesValue value;

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
    public Map<String, DatabaseNode> getChildren()
    {
        return children;
    }

}
