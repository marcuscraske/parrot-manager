package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.crypto.EncryptedValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Manages the collection of historic values for a node.
 */
public class DatabaseNodeHistory
{
    // The node to which this collection belongs
    private DatabaseNode currentNode;

    // Cached array of historic values retrieved; same reason as children being cached
    EncryptedValue[] historyCached;

    // Previous values stored at this node
    private Map<UUID, EncryptedValue> history;

    // The hashcode's of deleted values (used for syncing)
    private Set<UUID> deleted;

    DatabaseNodeHistory(DatabaseNode currentNode)
    {
        this.currentNode = currentNode;
        this.history = new HashMap<>();
        this.deleted = new HashSet<>();
    }

    /**
     * Adds the identifier of a deleted item.
     *
     * @param id ID from a deleted {@link EncryptedValue}
     */
    public synchronized void addDeleted(UUID id)
    {
        deleted.add(id);
    }

    /**
     * An array of identifiers of deleted values.
     *
     * @return an array, or empty
     */
    public synchronized UUID[] getDeleted()
    {
        return deleted.toArray(new UUID[deleted.size()]);
    }

    /**
     * @param encryptedValue value to be added
     */
    public synchronized void add(EncryptedValue encryptedValue)
    {
        history.put(encryptedValue.getId(), encryptedValue);
        setDirty();
    }

    /**
     * @param values adds collection of values
     */
    public synchronized void addAll(Collection<? extends EncryptedValue> values)
    {
        // Add each value
        values.stream().forEach(encryptedValue -> history.put(encryptedValue.getId(), encryptedValue));
        setDirty();
    }

    /**
     * @return cached history; result is safe against garbage collection
     */
    public synchronized EncryptedValue[] fetch()
    {
        historyCached = history.values().toArray(new EncryptedValue[history.size()]);
        return historyCached;
    }

    /**
     * Removes specified value from history.
     *
     * @param encryptedValue value to be removed
     */
    public void remove(EncryptedValue encryptedValue)
    {
        // Remove from collection
        if (history.remove(encryptedValue.getId()) != null)
        {
            // Add to list of values deleted
            deleted.add(encryptedValue.getId());
        }

        setDirty();
    }

    /**
     * clears all stored history.
     */
    public void clearAll()
    {
        // Clear history
        history.clear();

        // Clear deleted values
        deleted.clear();

        setDirty();
    }

    /**
     * @return total number of historic values
     */
    public int size()
    {
        return history.size();
    }

    /**
     * Merges items from another history into this history.
     *
     * @param otherHistory the other history
     */
    public void merge(DatabaseNodeHistory otherHistory)
    {
        boolean isDirty = false;

        // Add any missing values, unless deleted
        EncryptedValue clone;

        for (EncryptedValue encryptedValue : otherHistory.history.values())
        {
            if (!deleted.contains(encryptedValue.getId()) && !this.history.containsKey(encryptedValue.getId()))
            {
                clone = encryptedValue.clone();
                this.history.put(clone.getId(), clone);
                isDirty = true;
            }
        }

        // Add any missing deleted values
        if (deleted.addAll(otherHistory.deleted))
        {
            isDirty = true;
        }

        // Mark database as dirty
        if (isDirty)
        {
            setDirty();
        }
    }

    private void setDirty()
    {
        // Mark database as dirty
        currentNode.getDatabase().setDirty(true);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DatabaseNodeHistory that = (DatabaseNodeHistory) o;

        return history != null ? history.equals(that.history) : that.history == null;

    }

    @Override
    public int hashCode()
    {
        return history != null ? history.hashCode() : 0;
    }

}
