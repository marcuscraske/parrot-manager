package com.limpygnome.parrot.model.node;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a node in a database.
 *
 * Each node can then have children, which can have more nodes or just values.
 */
public class DatabaseNode implements Serializable
{
    // Any sub-nodes which belong to this node
    public Map<String, DatabaseNode> children;
    // The name of the node
    private String name;
    // The type of value
    private DatabaseNodeValueType valueType;
    // The value
    private byte[] value;

    public DatabaseNode(String name)
    {
        // Default the type for now, not actually required to be different...
        this.valueType = DatabaseNodeValueType.STRING;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

}
