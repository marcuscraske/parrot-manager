package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.crypto.CryptoParams;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a node in a database.
 *
 * Each node can then have children, which can have more nodes.
 *
 * Thread safe.
 */
public class DatabaseNode
{
    // The database to which this belongs
    private Database database;

    // The parent of this node
    private DatabaseNode parent;

    // A unique ID for this node
    private UUID id;

    // The name of the node
    private String name;

    // The epoch time of when the node was last changed
    private long lastModified;

    // The value stored at this node
    private EncryptedValue value;

    // Non-secure local properties - these will not be remotely synchronized
    private Map<String, String> localProperties;

    // Any sub-nodes which belong to this node
    private List<DatabaseNode> children;

    // A list of previously deleted children; used for merging
    private Set<UUID> deletedChildren;

    // History of this node for remote-sync; tracks deleted elements
    private DatabaseNodeHistory history;

    // Cached array of children retrieved; this is because to provide an array, we need to keep a permanent reference
    // to avoid garbage collection
    DatabaseNode[] childrenCached;

    private DatabaseNode(Database database, UUID id, String name, long lastModified)
    {
        // TODO unit test
        if (id == null)
        {
            throw new IllegalArgumentException("Database node identifier cannot be null");
        }

        this.database = database;
        this.parent = null;
        this.id = id;
        this.name = name;
        this.lastModified = lastModified;

        this.children = new LinkedList<>();
        this.deletedChildren = new HashSet<>();
        this.history = new DatabaseNodeHistory(this);
        this.localProperties = new HashMap<>();
        this.childrenCached = new DatabaseNode[0];
    }

    /**
     * Creates a new database node.
     *
     * WARNING: use this very carefully.
     */
    public DatabaseNode(Database database)
    {
        this(database, UUID.randomUUID(), null, 0);
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
    public DatabaseNode(Database database, UUID id, String name, long lastModified, EncryptedValue value)
    {
        this(database, id, name, lastModified);
        this.value = value;
    }

    /**
     * Creates a new node, using the current system time for last modified time and generates a random UUID.
     *
     * @param database the DB to which this belongs
     * @param name the name of the node
     */
    public DatabaseNode(Database database, String name)
    {
        this(database, UUID.randomUUID(), name, System.currentTimeMillis());
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
     * This does not update last modified.
     *
     * @param id the unique identifier to be assigned to this node
     */
    public synchronized void setId(UUID id)
    {
        DatabaseLookup lookup = database.getLookup();

        // Remove from lookup
        lookup.remove(this);

        // Update ID
        this.id = id;

        // Add to lookup
        lookup.add(this);

        // Set dirty flag
        database.setDirty(true);
    }

    /**
     * @return the name of the node; purely for presentation purposes
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
        setDirty();
    }

    /**
     * @return last modified epoch ms time
     */
    public long getLastModified()
    {
        return lastModified;
    }

    /**
     * Sets the time at which this was last modified.
     *
     * This will set the dirty flag on the database.
     *
     * @param lastModified last modified epoch ms time
     */
    public void setLastModified(long lastModified)
    {
        this.lastModified = lastModified;
        database.setDirty(true);
    }

    /**
     * Updates value and marks database as dirty.
     *
     * @param value encrypted value, nullable; this will be cloned
     */
    public void setValue(EncryptedValue value)
    {
        // Create clone
        EncryptedValue clone = (value != null ? value.clone() : null);

        // Add existing value to history
        if (this.value != null)
        {
            history.add(this.value);
        }

        // Update current value
        this.value = clone;
        setDirty();
    }

    /**
     * @return the encrypted value, as stored in memory
     */
    public EncryptedValue getValue()
    {
        return value;
    }

    /**
     * Sets a non-secure local field/property.
     *
     * TODO unit test
     *
     * @param key the key/name of the local property
     * @param value the value
     * @param applyToChildren indicates whether to apply the same property to all the child nodes
     */
    public synchronized void setLocalProperty(String key, String value, boolean applyToChildren)
    {
        localProperties.put(key, value);

        if (applyToChildren)
        {
            for (DatabaseNode child : childrenCached)
            {
                child.setLocalProperty(key, value, true);
            }
        }
    }

    /**
     * TODO unit test
     *
     * @param name the key/name of the local property
     * @param defaultValue the value returned when the local property is not found; can be null, which returns empty string
     * @return either the value or default value
     */
    public String getLocalProperty(String name, String defaultValue)
    {
        String result = localProperties.get(name);
        if (result == null)
        {
            result = defaultValue != null ? defaultValue : "";
        }
        return result;
    }

    /**
     * @return read-only copy of properties
     */
    public Map<String, String> getLocalProperties()
    {
        return Collections.unmodifiableMap(localProperties);
    }

    /**
     * Sets the the instance of {@link DatabaseNodeHistory}, which holds
     * historic encrypted values for this node.
     *
     * @param history the instance
     */
    public void setHistory(DatabaseNodeHistory history)
    {
        this.history = history;

        // Mark as dirty
        setDirty();
    }

    /**
     * @return an instance to manage the history of old values
     */
    public DatabaseNodeHistory getHistory()
    {
        return history;
    }

    /**
     * @return child nodes (cached array)
     */
    public synchronized DatabaseNode[] getChildren()
    {
        return childrenCached;
    }

    /**
     * Retrieves child by name.
     *
     * @param name the name of the child node
     * @return the instance, if found, or null
     */
    public synchronized DatabaseNode getByName(String name)
    {
        DatabaseNode result = null;

        if (name != null)
        {
            result = children
                    .stream()
                    .filter(node -> name.equals(node.name))
                    .findFirst()
                    .orElse(null);
        }

        return result;
    }

    /**
     * Retrieves node by id.
     *
     * @param id the id of the node
     * @return the node, or null
     */
    public synchronized DatabaseNode getById(UUID id)
    {
        // TODO unit test
        DatabaseNode result = null;

        if (id != null)
        {
            result = children
                    .stream()
                    .filter(node -> id.equals(node.id))
                    .findFirst()
                    .orElse(null);
        }

        return result;
    }

    /*
        Use #add from parent node :)
     */
    void setParent(DatabaseNode node)
    {
        this.parent = node;
    }

    /**
     * @return the number of child nodes / entries
     */
    public synchronized int getChildCount()
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
        }

        // Perform on child nodes
        for (DatabaseNode child : children)
        {
            child.rebuildCrypto(oldMemoryCryptoParams);
        }
    }

    /**
     * Performs shallow clone of this node.
     *
     * This does not add it to the target database, nor does this operation set the dirty flag.
     *
     * @param newDatabase the database to contain the new cloned node
     * @return a cloned instance of this node
     */
    protected synchronized DatabaseNode clone(Database newDatabase)
    {
        DatabaseNode newNode = new DatabaseNode(
                newDatabase, id, name, lastModified
        );

        if (value != null)
        {
            newNode.value = value.clone();
        }


        // Perform same recursion on children
        DatabaseNode clonedChild;
        for (DatabaseNode child : children)
        {
            clonedChild = child.clone(newDatabase);
            newNode.add(clonedChild);
        }

        return newNode;
    }

    /**
     * Adds a child node.
     *
     * @param node the new child node
     * @return the node added
     */
    public synchronized DatabaseNode add(DatabaseNode node)
    {
        // Remove node if it already exists; this will become an update (thus don't use normal remove procedure)
        // TODO unit test
        DatabaseNode existing = node.getById(node.id);
        if (existing != null)
        {
            children.remove(existing);
        }

        // Add as child
        children.add(node);

        // Refresh cache
        refreshChildrenCache();

        // Update parent
        node.setParent(this);

        // Add to database lookup
        database.getLookup().add(node);

        // Set dirty flag
        database.setDirty(true);

        return node;
    }

    /**
     * @return creates a new node and adds it as a child of this current node
     */
    public synchronized DatabaseNode addNew()
    {
        // Add node
        UUID randomUuid = UUID.randomUUID();
        DatabaseNode node = new DatabaseNode(database, randomUuid, null, System.currentTimeMillis());
        add(node);

        return node;
    }

    /**
     * Removes this node from the database, unless this is a root node (cannot ever be removed).
     */
    public synchronized DatabaseNode remove()
    {
        // Only remove if parent i.e. not root element (cant remove root element)
        if (parent != null)
        {
            // Remove from parent
            parent.children.remove(this);
            parent.deletedChildren.add(id);

            // Refresh parent cache
            parent.refreshChildrenCache();

            // Remove from lookup
            database.getLookup().remove(this);

            // Set as orphan
            parent = null;

            // Set dirty flag
            database.setDirty(true);
        }

        return this;
    }

    /**
     * Moves this node to become the child of a different parent.
     *
     * @param newParent the destination parent node
     */
    public synchronized void moveTo(DatabaseNode newParent)
    {
        // Remove from previous parent
        remove();

        // Re-generate IDs recursively
        regenerateIds();

        // Attach to target node
        newParent.add(this);
    }

    private void regenerateIds()
    {
        id = UUID.randomUUID();

        for (DatabaseNode child : children)
        {
            child.regenerateIds();
        }
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

    /**
     * @return the full path of this node
     */
    public String getPath()
    {
        String path;

        if (parent == null)
        {
            // TODO unit test
            path = "/root";
        }
        else
        {
            path = name != null && name.length() > 0 ? name : "[" + id + "]";

            if (parent != null)
            {
                path = parent.getPath() + "/" + path;
            }
        }

        return path;
    }

    Database getDatabase()
    {
        return database;
    }

    private void setDirty()
    {
        // Update the last modified time
        lastModified = System.currentTimeMillis();

        // Set flag on database
        database.setDirty(true);
    }

    private void refreshChildrenCache()
    {
        childrenCached =  children.toArray(new DatabaseNode[children.size()]);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseNode that = (DatabaseNode) o;

        if (lastModified != that.lastModified) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (children != null ? !children.equals(that.children) : that.children != null) return false;
        if (deletedChildren != null ? !deletedChildren.equals(that.deletedChildren) : that.deletedChildren != null)
            return false;
        return history != null ? history.equals(that.history) : that.history == null;
    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int) (lastModified ^ (lastModified >>> 32));
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (children != null ? children.hashCode() : 0);
        result = 31 * result + (deletedChildren != null ? deletedChildren.hashCode() : 0);
        result = 31 * result + (history != null ? history.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "DatabaseNode{" +
                "  parent=" + (parent != null ? parent.getId() : "null") +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", lastModified="    + lastModified +
                '}';
    }

}
