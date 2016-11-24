package com.limpygnome.parrot.model.node;

import com.limpygnome.parrot.model.Database;
import com.limpygnome.parrot.model.params.CryptoParams;

import java.util.*;

/**
 * Represents a node in a database.
 *
 * Each node can then have children, which can have more nodes.
 *
 * TODO: database/UI should have option to purge old delete history on a DB...
 */
public class DatabaseNode
{
    // The database to which this belongs
    private Database database;

    // Any sub-nodes which belong to this node
    private Map<UUID, DatabaseNode> children;

    // A list of previously deleted children; used for merging
    private List<UUID> deletedChildren;

    // A unique ID for this node
    private UUID id;

    // The name of the node
    private String name;

    // The epoch time of when the node was last changed
    private long lastModified;

    // The value stored at this node
    private EncryptedAesValue value;

    private DatabaseNode(Database database, UUID id, String name, long lastModified)
    {
        this.database = database;
        this.id = id;
        this.name = name;
        this.lastModified = lastModified;

        this.children = new HashMap<>(0);
        this.deletedChildren = new LinkedList<>();
    }

    /**
     * Creates a new node with already encrypted data.
     *
     * @param database the DB to which this belongs
     * @param id unique identifier
     * @param name the name of this node
     * @param lastModified the epoch time at which this node was last modified
     * @param value the encrypted value
     */
    public DatabaseNode(Database database, UUID id, String name, long lastModified, EncryptedAesValue value)
    {
        this(database, id, name, lastModified);
        this.value = value;
    }

    /**
     * Creates a new node for unecrypted data, which is encrypted by this constructor.
     *
     * @param database the DB to whcih this belongs
     * @param id unique identifier
     * @param name the name of this node
     * @param lastModified the epoch time at which this node was last modified
     * @param data unencrypted data
     * @throws Exception thrown if the data cannot be encrypted
     */
    public DatabaseNode(Database database, UUID id, String name, long lastModified, byte[] data) throws Exception
    {
        this(database, id, name, lastModified);

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
     * @return child nodes
     */
    public Map<UUID, DatabaseNode> getChildren()
    {
        return children;
    }

    /**
     * @return deleted children
     */
    public List<UUID> getDeletedChildren()
    {
        return deletedChildren;
    }

    // TODO: make protected and move into same package as DB, prolly put on DB but have it call a wrapper - keep it in a nice unit :)
    public void rebuildCrypto(CryptoParams oldMemoryCryptoParams) throws Exception
    {
        // De-crypt current value
        byte[] decrypted = database.decrypt(value, oldMemoryCryptoParams);

        // Re-encrypt
        value = database.encrypt(decrypted);

        // Perform on child nodes
        for (DatabaseNode child : children.values())
        {
            child.rebuildCrypto(oldMemoryCryptoParams);
        }
    }

    /**
     * @param database the database to contain the new cloned node
     * @return a cloned instance of this node
     */
    public DatabaseNode clone(Database database)
    {
        DatabaseNode newNode = new DatabaseNode(database, id, name, lastModified);

        // Perform same recursion on children
        DatabaseNode clonedChild;
        for (DatabaseNode child : children.values())
        {
            clonedChild = child.clone(database);
            newNode.children.put(clonedChild.id, clonedChild);
        }

        return newNode;
    }

    // TODO: same as above or...
    public void merge(DatabaseNode src)
    {
        // Check if this node was modified before/after
        if (src.lastModified > lastModified)
        {
            // Compare/clone first level props
            if (!name.equals(src.name))
            {
                name = src.name;
            }

            if (!value.equals(src.value))
            {
                value = new EncryptedAesValue(src.value.getIv(), src.value.getValue());
            }

            lastModified = src.lastModified;
        }

        // Compare our children against theirs
        {
            DatabaseNode child;
            DatabaseNode otherNode;

            Iterator<Map.Entry<UUID, DatabaseNode>> iterator = children.entrySet().iterator();
            Map.Entry<UUID, DatabaseNode> kv;

            while (iterator.hasNext())
            {
                kv = iterator.next();
                child = kv.getValue();

                otherNode = src.children.get(kv.getKey());

                if (otherNode != null)
                {
                    // Recursively merge child
                    kv.getValue().merge(otherNode);
                }
                else if (src.deletedChildren.contains(child.id))
                {
                    // Remove from our tree, this node has been deleted
                    iterator.remove();
                }
            }
        }

        // Compare their children against ours
        {
            DatabaseNode otherChild;
            DatabaseNode newNode;

            for (Map.Entry<UUID, DatabaseNode> kv : src.children.entrySet())
            {
                otherChild = kv.getValue();

                // Check if new node to add to our side (new node)...
                if (!children.containsKey(otherChild.id) && !deletedChildren.contains(otherChild.id))
                {
                    newNode = otherChild.clone(database);
                    children.put(newNode.id, newNode);
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseNode that = (DatabaseNode) o;

        if (lastModified != that.lastModified) return false;
        if (children != null ? !children.equals(that.children) : that.children != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        int result = children != null ? children.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int) (lastModified ^ (lastModified >>> 32));
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

}
