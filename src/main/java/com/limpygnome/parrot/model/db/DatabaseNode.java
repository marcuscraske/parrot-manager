package com.limpygnome.parrot.model.db;

import com.limpygnome.parrot.model.dbaction.Action;
import com.limpygnome.parrot.model.dbaction.ActionsLog;
import com.limpygnome.parrot.model.dbaction.MergeInfo;
import com.limpygnome.parrot.model.params.CryptoParams;

import java.util.*;

/**
 * Represents a db in a database.
 *
 * Each db can then have children, which can have more nodes.
 *
 * TODO: database/UI should have option to purge old delete history on a DB...
 *
 * Thread safe.
 */
public class DatabaseNode
{
    // The database to which this belongs
    private Database database;

    // A unique ID for this db
    private UUID id;

    // The name of the db
    private String name;

    // The epoch time of when the db was last changed
    private long lastModified;

    // The value stored at this db
    private EncryptedAesValue value;

    // Any sub-nodes which belong to this db
    private Map<UUID, DatabaseNode> children;

    // A list of previously deleted children; used for merging
    private Set<UUID> deletedChildren;

    private DatabaseNode(Database database, UUID id, String name, long lastModified)
    {
        this.database = database;
        this.id = id;
        this.name = name;
        this.lastModified = lastModified;

        this.children = new HashMap<>(0);
        this.deletedChildren = new HashSet<>();
    }

    /**
     * Creates a new db with already encrypted data.
     *
     * @param database the DB to which this belongs
     * @param id unique identifier
     * @param name the name of this db
     * @param lastModified the epoch time at which this db was last modified
     * @param value the encrypted value
     */
    public DatabaseNode(Database database, UUID id, String name, long lastModified, EncryptedAesValue value)
    {
        this(database, id, name, lastModified);
        this.value = value;
    }

    /**
     * Creates a new db for unecrypted data, which is encrypted by this constructor.
     *
     * @param database the DB to whcih this belongs
     * @param id unique identifier
     * @param name the name of this db
     * @param lastModified the epoch time at which this db was last modified
     * @param unecryptedData unencrypted data
     * @throws Exception thrown if the data cannot be encrypted
     */
    public DatabaseNode(Database database, UUID id, String name, long lastModified, byte[] unecryptedData) throws Exception
    {
        this(database, id, name, lastModified);

        // Encrypt the data
        this.value = database.encrypt(unecryptedData);
    }

    /**
     * @return unique identifier for this db
     */
    public UUID getId()
    {
        return id;
    }

    /**
     * @param id the unique identifier to be assigned to this db
     */
    public void setId(UUID id)
    {
        this.id = id;
    }

    /**
     * @return the name of the db; purely for presentation purposes
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
     * Decrypts the value stored at this db and returns the data.
     *
     * This can be an empty array if the db does not store a value i.e. acts as a directory/label for a set of child
     * nodes.
     *
     * @return the decrypted value stored at this db
     * @throws Exception
     */
    public synchronized byte[] getDecryptedValue() throws Exception
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
    public Set<UUID> getDeletedChildren()
    {
        return deletedChildren;
    }

    /*
        Rebuilds the encrypted in-memory value of this node
     */
    protected synchronized void rebuildCrypto(CryptoParams oldMemoryCryptoParams) throws Exception
    {
        // De-crypt current value
        if (value != null)
        {
            byte[] decrypted = database.decrypt(value, oldMemoryCryptoParams);

            // Re-encrypt
            value = database.encrypt(decrypted);
        }

        // Perform on child nodes
        for (DatabaseNode child : children.values())
        {
            child.rebuildCrypto(oldMemoryCryptoParams);
        }
    }

    /**
     * @param database the database to contain the new cloned db
     * @return a cloned instance of this db
     */
    protected synchronized DatabaseNode clone(Database database)
    {
        DatabaseNode newNode = new DatabaseNode(database, id, name, lastModified);
        newNode.value = new EncryptedAesValue(value.getIv().clone(), value.getValue().clone());

        // Perform same recursion on children
        DatabaseNode clonedChild;
        for (DatabaseNode child : children.values())
        {
            clonedChild = child.clone(database);
            newNode.children.put(clonedChild.id, clonedChild);
        }

        return newNode;
    }

    /*
        Merges the passed node with this node.

        Both nodes should be at the same level in their respected databases.
     */
    protected synchronized void merge(MergeInfo mergeInfo, DatabaseNode src)
    {
        String currentNodePath = mergeInfo.getNodePath(this);

        // Check if this db was modified before/after
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

            mergeInfo.actionsLog.add(new Action(currentNodePath + " - updated node properties"));
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
                    kv.getValue().merge(actionsLog, otherNode);
                }
                else if (src.deletedChildren.contains(child.id))
                {
                    // Remove from our tree, this db has been deleted
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

                // Check if new db to add to our side (new db)...
                if (!children.containsKey(otherChild.id) && !deletedChildren.contains(otherChild.id))
                {
                    newNode = otherChild.clone(database);
                    children.put(newNode.id, newNode);
                }
            }
        }

        // Merge any deleted items
        deletedChildren.addAll(src.deletedChildren);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseNode that = (DatabaseNode) o;

        if (lastModified != that.lastModified) return false;
        if (children != null ? !children.equals(that.children) : that.children != null) return false;
        if (deletedChildren != null ? !deletedChildren.equals(that.deletedChildren) : that.deletedChildren != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode()
    {
        int result = children != null ? children.hashCode() : 0;
        result = 31 * result + (deletedChildren != null ? deletedChildren.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int) (lastModified ^ (lastModified >>> 32));
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

}
