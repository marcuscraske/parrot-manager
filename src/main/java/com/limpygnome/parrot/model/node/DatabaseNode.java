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


    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public byte[] getValue() throws Exception
    {
        byte[] decryptedBytes = database.decrypt(value.getIv(), value.getValue());
        return decryptedBytes;
    }

    public void setValue(byte[] value)
    {
    }

}
