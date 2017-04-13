package com.limpygnome.parrot.library.db;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fast lookup for database nodes based on their identifier.
 */
public class DatabaseLookup
{
    private Map<UUID, DatabaseNode> lookup;

    public DatabaseLookup()
    {
        lookup = new HashMap<>();
    }

    public synchronized DatabaseNode get(UUID id)
    {
        return lookup.get(id);
    }

    synchronized void add(DatabaseNode node)
    {
        lookup.put(node.getUuid(), node);

        // Add children recursively
        for (DatabaseNode child : node.getChildren())
        {
            add(child);
        }
    }

    synchronized void remove(DatabaseNode node)
    {
        lookup.remove(node.getUuid());

        // Remove children recursively
        for (DatabaseNode child : node.getChildren())
        {
            remove(child);
        }
    }

}
