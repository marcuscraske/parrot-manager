package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.dbaction.Action;
import com.limpygnome.parrot.library.dbaction.ActionsLog;
import com.limpygnome.parrot.library.dbaction.MergeInfo;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Provides ability to merge databases.
 */
public class DatabaseMerger
{

    /**
     * Merges the source database into the destination database, so that the destination should have all changes
     * from both databases.
     *
     * At the end of this operation, it's expected that this current database instance will contain the result of the
     * merge, so that this database has the combined additions//deletions merged together.
     *
     * If there are any changes between the two databases, the dirty flag is set. If changes are made to this database,
     * the flag is set. If no changes are made, but required on the passed database instance, its dirty flag is set.
     *
     * @param source the database being merged into the destination
     * @param destination the database to have the final version of everything
     * @param password the password for the destination database
     * @return a log of actions performed on the database
     * @throws Exception if a crypto operation fails
     */
    public synchronized ActionsLog merge(Database source, Database destination, char[] password) throws Exception
    {
        ActionsLog actionsLog = new ActionsLog();

        // Check if databases are the same, skip if so...
        if (destination.equals(source))
        {
            actionsLog.add(new Action("no changes detected"));
        }
        else
        {
            actionsLog.add(new Action("changes detected, merging..."));

            // Merge crypto params
            mergeDatabaseCryptoParams(actionsLog, source, destination, password);

            // Merge nodes
            mergeNode(new MergeInfo(actionsLog, destination.root), source.root, destination.root);
        }

        return actionsLog;
    }

    private void mergeDatabaseCryptoParams(ActionsLog actionsLog, Database source, Database destination, char[] password) throws Exception
    {
        if (destination.fileCryptoParams.getLastModified() < source.fileCryptoParams.getLastModified())
        {
            destination.updateFileCryptoParams(source.fileCryptoParams, password);
            actionsLog.add(new Action("updated file crypto parameters"));
            destination.setDirty(true);
        }

        if (destination.memoryCryptoParams.getLastModified() < source.memoryCryptoParams.getLastModified())
        {
            destination.updateMemoryCryptoParams(source.memoryCryptoParams, password);
            actionsLog.add(new Action("updated memory crypto parameters"));
            destination.setDirty(true);
        }
    }

    /*
        MERGE NODE

        Merges the passed node with this node.

        Both nodes should be at the same level in their respected databases.
     */
    private void mergeNode(MergeInfo mergeInfo, DatabaseNode src, DatabaseNode dest)
    {
        // Check if this db was modified before/after
        if (src.lastModified > dest.lastModified)
        {
            // Compare/clone first level props
            // -- Name
            if (!dest.name.equals(src.name))
            {
                dest.name = src.name;
                mergeInfo.addMergeMessage("changing name to '" + src.name + "'");
            }

            // -- Value
            if (!dest.value.equals(src.value))
            {
                dest.value = src.value.clone();
            }

            // -- History
            if (!dest.history.equals(src.history))
            {
                dest.history.cloneToNode(src);
                mergeInfo.addMergeMessage("value history updated");
            }

            // Copy last modified
            dest.lastModified = src.lastModified;

            // Mark as dirty due to changes
            dest.database.setDirty(true);

            mergeInfo.addMergeMessage("updated node properties");
        }
        else if (src.lastModified < dest.lastModified)
        {
            src.database.setDirty(true);
            mergeInfo.addMergeMessage("node older on remote side");
        }

        // Compare our children against theirs
        {
            DatabaseNode child;
            DatabaseNode otherNode;

            Iterator<Map.Entry<UUID, DatabaseNode>> iterator = dest.children.entrySet().iterator();
            Map.Entry<UUID, DatabaseNode> kv;

            while (iterator.hasNext())
            {
                kv = iterator.next();
                child = kv.getValue();

                otherNode = src.children.get(kv.getKey());

                if (otherNode != null)
                {
                    // Recursively merge our child
                    mergeNode(mergeInfo, child, otherNode);
                }
                else if (src.deletedChildren.contains(child.id))
                {
                    // Remove from our tree, this node has been deleted
                    iterator.remove();
                    dest.database.lookup.remove(child.id);

                    mergeInfo.addMergeMessage("removed child - " + child.getPath());
                    dest.database.setDirty(true);
                }
                else
                {
                    // Our child is missing from the remote site
                    mergeInfo.addMergeMessage("remote node missing our child - " + child.getPath());
                    src.database.setDirty(true);
                }
            }
        }

        // Compare their children against ours
        {
            DatabaseNode otherChild;
            DatabaseNode newNode;
            boolean isDeleted;

            for (Map.Entry<UUID, DatabaseNode> kv : src.children.entrySet())
            {
                otherChild = kv.getValue();

                // New node on their side that we don't have...
                isDeleted = dest.deletedChildren.contains(otherChild.id);

                if (!dest.children.containsKey(otherChild.id) && !isDeleted)
                {
                    newNode = otherChild.clone(dest.database);
                    dest.add(newNode);
                    mergeInfo.addMergeMessage("added child - " + newNode.getPath());
                    dest.database.setDirty(true);
                }
                else if (isDeleted)
                {
                    // Looks like we deleted the current node on our side...
                    mergeInfo.addMergeMessage("node already deleted in local database - " + otherChild.getPath());
                    src.database.setDirty(true);
                }
            }
        }

        // Merge any deleted items
        boolean change = dest.deletedChildren.addAll(src.deletedChildren);

        // Set dirty flag
        if (change)
        {
            mergeInfo.addMergeMessage("updated list of deleted nodes");
            dest.database.setDirty(true);
        }
    }

}
