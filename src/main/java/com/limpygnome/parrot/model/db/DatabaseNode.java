package com.limpygnome.parrot.model.db;

import com.limpygnome.parrot.model.dbaction.MergeInfo;
import com.limpygnome.parrot.model.params.CryptoParams;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.joda.time.DateTime;

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

    // The parent of this node
    private DatabaseNode parent;

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
        this.parent = null;
        this.id = id;
        this.name = name;
        this.lastModified = lastModified;

        this.children = new HashMap<>(0);
        this.deletedChildren = new HashSet<>();

        // Add ref to database lookup
        database.lookup.put(id, this);
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
     * @return unique identifier for this node
     */
    public String getId()
    {
        return id.toString();
    }

    /**
     * @return unique identifier for this node
     */
    public UUID getUuid()
    {
        return id;
    }

    /**
     * @param id the unique identifier to be assigned to this db
     */
    public synchronized void setId(UUID id)
    {
        // Update lookup
        database.lookup.remove(this.id);
        database.lookup.put(id, this);

        // Update ID
        this.id = id;

        // Set dirty flag
        database.setDirty(true);
    }

    /**
     * @return the name of the db; purely for presentation purposes
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name for this node
     */
    public synchronized void setName(String name)
    {
        this.name = name;
        database.setDirty(true);
    }

    /**
     * @return the last modified date
     */
    public long getLastModified()
    {
        return lastModified;
    }


    /**
     * @return formatted date time
     */
    public String getFormattedLastModified()
    {
        DateTime dateTime = new DateTime(lastModified);
        return dateTime.toString("dd-MM-yyyy HH:mm:ss");
    }

    /**
     * @return the encrypted value, as stored in memory
     */
    public EncryptedAesValue getValue()
    {
        return value;
    }

    /**
     * TODO: unit test null value
     * Decrypts the value stored at this node and returns the data.
     *
     * This can be an empty array if the node does not store a value i.e. acts as a directory/label for a set of child
     * nodes.
     *
     * @return the decrypted value stored at this node
     * @throws Exception
     */
    public synchronized byte[] getDecryptedValue() throws Exception
    {
        byte[] result = null;

        if (value != null)
        {
            result = database.decrypt(value);;
        }

        return result;
    }

    /**
     * TODO: unit test null value
     * @return decrypted value stored at this node, as string
     * @throws Exception when the value cannot be decrypted
     */
    public synchronized String getDecryptedValueString() throws Exception
    {
        String result = null;
        byte[] decrypted = getDecryptedValue();

        if (decrypted != null)
        {
            // TODO: move as configurable property for database?
            result = new String(decrypted, "UTF-8");
        }

        return result;
    }

    /**
     * TODO: unit test
     * @param value the desired value
     * @throws Exception thrown if crypto exception
     */
    public synchronized void setValueString(String value) throws Exception
    {
        // TODO: move as configurable property for database?
        byte[] plainValue = value.length() > 0 ? value.getBytes("UTF-8") : null;
        setValue(plainValue);
    }

    /**
     * TODO: unit test
     * @param value the desired value
     * @throws Exception thrown if crypto exception
     */
    public synchronized void setValue(byte[] value) throws Exception
    {
        if (value != null)
        {
            this.value = database.encrypt(value);
        }
        else
        {
            this.value = null;
        }

        // Set dirty flag...
        database.setDirty(true);
    }

    /**
     * @return child nodes
     */
    public synchronized DatabaseNode[] getChildren()
    {
        Collection<DatabaseNode> childNodes = children.values();
        return childNodes.toArray(new DatabaseNode[childNodes.size()]);
    }

    /**
     * @return retrieves read-only underlying map of children
     */
    Map<UUID, DatabaseNode> getChildrenMap()
    {
        return Collections.unmodifiableMap(children);
    }

    /**
     * @return the number of child nodes / entries
     */
    public int getChildCount()
    {
        return children.size();
    }

    /**
     * This will return a reference to the set of deleted children IDs/UUIDs. Operations are permitted against the
     * returned instance.
     *
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

            // Set dirty flag
            database.setDirty(true);
        }

        // Perform on child nodes
        for (DatabaseNode child : children.values())
        {
            child.rebuildCrypto(oldMemoryCryptoParams);
        }
    }

    /**
     * Clones current node.
     *
     * This does not add it to the target database, nor does this operation set the dirty flag.
     *
     * @param database the database to contain the new cloned node
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
            newNode.add(clonedChild);
        }

        return newNode;
    }

    /*
        Merges the passed node with this node.

        Both nodes should be at the same level in their respected databases.
     */
    protected synchronized void merge(MergeInfo mergeInfo, DatabaseNode src)
    {
        // Check if this db was modified before/after
        if (src.lastModified > lastModified)
        {
            // Compare/clone first level props
            if (!name.equals(src.name))
            {
                name = src.name;
                mergeInfo.addMergeMessage("changing name to '" + src.name + "'");
            }

            if (!value.equals(src.value))
            {
                value = new EncryptedAesValue(src.value.getIv(), src.value.getValue());
            }

            lastModified = src.lastModified;
            // TODO: add test
            database.setDirty(true);

            mergeInfo.addMergeMessage("updated node properties");
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
                    child.merge(new MergeInfo(mergeInfo, child), otherNode);
                }
                else if (src.deletedChildren.contains(child.id))
                {
                    // Remove from our tree, this node has been deleted
                    iterator.remove();
                    database.lookup.remove(child.id);
                    // TODO: add test
                    database.setDirty(true);

                    mergeInfo.addMergeMessage("removed child - " + child.getName());
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

                // Check if new db to add to our side (new node)...
                if (!children.containsKey(otherChild.id) && !deletedChildren.contains(otherChild.id))
                {
                    newNode = otherChild.clone(database);
                    add(newNode);
                    mergeInfo.addMergeMessage("added child - " + newNode.name);
                }
            }
        }

        // Merge any deleted items
        boolean change = deletedChildren.addAll(src.deletedChildren);

        // Set dirty flag
        // TODO: add test
        if (change)
        {
            database.setDirty(true);
            mergeInfo.addMergeMessage("updated list of deleted nodes");
        }
    }

    /**
     * Adds a child node.
     *
     * TODO: add tests
     *
     * @param node the new child node
     * @return the node added
     */
    public synchronized DatabaseNode add(DatabaseNode node)
    {
        // Add as child
        children.put(node.id, node);

        // Update parent
        node.parent = this;

        // Set dirty flag
        database.setDirty(true);

        return node;
    }

    /**
     * @return creates and adds a new node as a child
     *
     * TODO: add tests
     */
    public synchronized DatabaseNode add()
    {
        // Add node
        UUID randomUuid = UUID.randomUUID();
        DatabaseNode node = new DatabaseNode(database, randomUuid, null, System.currentTimeMillis());
        add(node);

        return node;
    }

    /**
     * Removes this node from the database, unless this is a root node (cannot ever be removed).
     *
     * TODO: add tests
     */
    public synchronized DatabaseNode remove()
    {
        if (parent != null)
        {
            // Remove from parent
            parent.children.remove(id);
            parent.deletedChildren.add(id);

            // Remove from lookup
            database.lookup.remove(id);

            // Set as orphan
            parent = null;

            // Set dirty flag
            database.setDirty(true);
        }

        return this;
    }

    /**
     * @return true = this is hte highest level node, false = has a parent
     */
    public boolean isRoot()
    {
        return parent == null;
    }

    /**
     * @return retrieves the parent of this node; null if this is orphaned or the root of the tree
     */
    public DatabaseNode getParent()
    {
        return parent;
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
