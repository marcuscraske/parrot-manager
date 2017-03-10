package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.crypto.EncryptedValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages the collection of historic values for a node.
 */
public class DatabaseNodeHistory
{
    // The node to which this collection belongs
    DatabaseNode currentNode;

    // Cached array of historic values retrieved; same reason as children being cached
    EncryptedValue[] historyCached;

    // Previous values stored at this node
    List<EncryptedValue> history;

    DatabaseNodeHistory(DatabaseNode currentNode)
    {
        this.currentNode = currentNode;
        this.history = new LinkedList<>();
    }

    /**
     * @param encryptedValue value to be added
     */
    public void add(EncryptedValue encryptedValue)
    {
        history.add(encryptedValue);

        // Mark database as dirty
        currentNode.database.setDirty(true);
    }

    /**
     * @param values adds collection of values
     */
    public void addAll(Collection<? extends EncryptedValue> values)
    {
        history.addAll(values);
    }

    /**
     * @return cached history; result is safe against garbage collection
     */
    public EncryptedValue[] fetch()
    {
        historyCached = history.toArray(new EncryptedValue[history.size()]);
        return historyCached;
    }

    /**
     * Removes specified value from history.
     *
     * @param encryptedValue value to be removed
     */
    public void remove(EncryptedValue encryptedValue)
    {
        history.remove(encryptedValue);

        // Mark database as dirty
        currentNode.database.setDirty(true);
    }

    /**
     * @return total number of historic values
     */
    public int size()
    {
        return history.size();
    }

    public void cloneToNode(DatabaseNode targetNode)
    {
        // Build cloned instance
        DatabaseNodeHistory result = new DatabaseNodeHistory(currentNode);

        for (EncryptedValue encryptedValue : history)
        {
            result.add(encryptedValue.clone());
        }

        // Update target node with cloned history
        targetNode.history = result;
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
