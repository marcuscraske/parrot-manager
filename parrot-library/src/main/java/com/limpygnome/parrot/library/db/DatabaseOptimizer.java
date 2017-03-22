package com.limpygnome.parrot.library.db;

import java.util.stream.Stream;

/**
 * Common operations for optimizing a database.
 */
public class DatabaseOptimizer
{

    /**
     * Deletes all history around deleted nodes, which would otherwise be used for merging.
     *
     * @param database target database
     */
    public void deleteAllDeletedNodeHistory(Database database)
    {
        DatabaseNode root = database.getRoot();
        deleteAllDeletedNodeHistoryIterate(root);

        database.setDirty(true);
    }

    private void deleteAllDeletedNodeHistoryIterate(DatabaseNode node)
    {
        // Clear deleted node history
        node.getDeletedChildren().clear();

        // Iterate children
        for (DatabaseNode childNode : node.getChildren())
        {
            deleteAllValueHistoryIterate(childNode);
        }
    }

    /**
     * Deletes all historic values from a node.
     *
     * @param database target database
     */
    public void deleteAllValueHistory(Database database)
    {
        DatabaseNode root = database.root;
        deleteAllValueHistoryIterate(root);

        database.setDirty(true);
    }

    private void deleteAllValueHistoryIterate(DatabaseNode node)
    {
        // Clear history
        node.history().clearAll();

        // Iterate children
        Stream.of(node.getChildren()).forEach(
            childNode -> deleteAllValueHistoryIterate(childNode)
        );
    }

}
