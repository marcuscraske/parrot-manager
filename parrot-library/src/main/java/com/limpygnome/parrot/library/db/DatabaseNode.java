package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.crypto.CryptoParams;
import com.limpygnome.parrot.library.crypto.EncryptedAesValue;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
public final class DatabaseNode
{
    // The database to which this belongs
    Database database;

    // The parent of this node
    DatabaseNode parent;

    // A unique ID for this db
    UUID id;

    // The name of the db
    String name;

    // The epoch time of when the db was last changed
    long lastModified;

    // The value stored at this db
    EncryptedAesValue value;

    // Any sub-nodes which belong to this db
    Map<UUID, DatabaseNode> children;

    // Cached array of children retrieved; this is because to provide an array, we need to keep a permanent reference
    // to avoid garbage collection
    // TODO: update this on init and as child nodes are added/removed, current imp doesnt save anything
    DatabaseNode[] childrenCached;

    // A list of previously deleted children; used for merging
    Set<UUID> deletedChildren;

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
     * @return json object, or null
     * @throws Exception when crypto exception or cannot parse as JSON
     */
    public synchronized JSONObject getDecryptedValueJson() throws Exception
    {
        JSONObject result = null;
        String text = getDecryptedValueString();

        if (text != null)
        {
            JSONParser parser = new JSONParser();
            result = (JSONObject) parser.parse(text);
        }

        return result;
    }

    /**
     * TODO: unit test
     * @param json the desired value
     * @throws Exception when crypto exception
     */
    public synchronized void setValueJson(JSONObject json) throws Exception
    {
        // Convert to string and store
        String text = json.toJSONString();
        setValueString(text);
    }

    /**
     * TODO: unit test
     * @param value the desired value
     * @throws Exception when crypto exception
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

        // Update modified time
        lastModified = System.currentTimeMillis();

        // Set dirty flag...
        database.setDirty(true);
    }

    /**
     * @return child nodes (cached array)
     */
    public synchronized DatabaseNode[] getChildren()
    {
        // TODO: consider a more elegant solution...
        // Build array and assign locally to avoid garbage collection
        Collection<DatabaseNode> childNodes = children.values();
        childrenCached = childNodes.toArray(new DatabaseNode[childNodes.size()]);

        return childrenCached;
    }

    /**
     * TODO: test
     * @param name the name of the child node
     * @return the instance, if found, or null
     */
    public synchronized DatabaseNode getByName(String name)
    {
        DatabaseNode result = null;

        if (name != null)
        {
            DatabaseNode node;
            Iterator<DatabaseNode> iterator = children.values().iterator();

            while (result == null && iterator.hasNext())
            {
                node = iterator.next();

                if (name.equals(node.getName()))
                {
                    result = node;
                }
            }
        }

        return result;
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
     * TODO: review unit test, bug found here...
     *
     * @param database the database to contain the new cloned node
     * @return a cloned instance of this db
     */
    protected synchronized DatabaseNode clone(Database database)
    {
        DatabaseNode newNode = new DatabaseNode(database, id, name, lastModified);

        // TODO: unit test null value beiing cloned
        if (value != null)
        {
            newNode.value = new EncryptedAesValue(value.getIv().clone(), value.getValue().clone());
        }

        // Perform same recursion on children
        DatabaseNode clonedChild;
        for (DatabaseNode child : children.values())
        {
            clonedChild = child.clone(database);
            newNode.add(clonedChild);
        }

        return newNode;
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
     * @return creates a new instance and adds it as a child of this current node
     *
     * TODO: add tests
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
     *
     * TODO: add tests
     */
    public synchronized DatabaseNode remove()
    {
        // Only remove if parent i.e. not root element (cant remove root element)
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

    /**
     * TODO: unit test
     * @return the full path of this node
     */
    public String getPath()
    {
        String path = name != null ? name : "[" + id + "]";

        if (parent != null)
        {
            path = parent.getPath() + "/" + path;
        }

        return path;
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
