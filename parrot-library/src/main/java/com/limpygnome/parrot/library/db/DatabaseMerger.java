package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.crypto.CryptoParams;
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
     * If there are any changes between the two databases, the dirty flag is set on the destination
     * database.
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
            actionsLog.add("no changes detected");
        }
        else
        {
            actionsLog.add("changes detected, merging...");

            // Merge crypto params
            mergeDatabaseFileCryptoParams(actionsLog, source, destination, password);
            mergeDatabaseMemoryCryptoParams(actionsLog, source, destination, password);

            // Merge nodes
            mergeNode(new MergeInfo(actionsLog, destination.root), source.root, destination.root);
        }

        return actionsLog;
    }

    void mergeDatabaseFileCryptoParams(ActionsLog actionsLog, Database source, Database destination, char[] password) throws Exception
    {
        CryptoParams destFileCryptoParams = destination.getFileCryptoParams();
        CryptoParams srcFileCryptoParams = source.getFileCryptoParams();

        if (destFileCryptoParams.getLastModified() < srcFileCryptoParams.getLastModified())
        {
            destination.updateFileCryptoParams(srcFileCryptoParams, password);
            actionsLog.add("updated file crypto parameters");
        }
        else if (destFileCryptoParams.getLastModified() > srcFileCryptoParams.getLastModified())
        {
            destination.setDirty(true);
            actionsLog.add("local file params are newer");
        }

        CryptoParams destMemoryCryptoParams = destination.getMemoryCryptoParams();
        CryptoParams srcMemoryCryptoParams = source.getMemoryCryptoParams();

        if (destMemoryCryptoParams.getLastModified() < srcMemoryCryptoParams.getLastModified())
        {
            destination.updateMemoryCryptoParams(srcMemoryCryptoParams, password);
            actionsLog.add("updated memory crypto parameters");
        }
        else if (destMemoryCryptoParams.getLastModified() > srcMemoryCryptoParams.getLastModified())
        {
            destination.setDirty(true);
            actionsLog.add("local memory params are newer");
        }
    }

    void mergeDatabaseMemoryCryptoParams(ActionsLog actionsLog, Database source, Database destination, char[] password) throws Exception
    {
        CryptoParams destMemoryCryptoParams = destination.getMemoryCryptoParams();
        CryptoParams srcMemoryCryptoParams = source.getMemoryCryptoParams();

        if (destMemoryCryptoParams.getLastModified() < srcMemoryCryptoParams.getLastModified())
        {
            destination.updateMemoryCryptoParams(srcMemoryCryptoParams, password);
            actionsLog.add("updated memory crypto parameters");
        }
        else if (destMemoryCryptoParams.getLastModified() > srcMemoryCryptoParams.getLastModified())
        {
            destination.setDirty(true);
        }
    }

    /*
        MERGE NODE

        Merges the passed node with this node.

        Both nodes should be at the same level in their respected databases.
     */
    void mergeNode(MergeInfo mergeInfo, DatabaseNode src, DatabaseNode dest)
    {
        mergeNodeDetails(mergeInfo, src, dest);
        mergeDestNodeChildren(mergeInfo, src, dest);
        mergeSrcNodeChildren(mergeInfo, src, dest);
    }

    void mergeNodeDetails(MergeInfo mergeInfo, DatabaseNode src, DatabaseNode dest)
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
                mergeInfo.addMergeMessage("value updated");
            }

            // -- History
            if (!dest.history.equals(src.history))
            {
                dest.history.cloneToNode(src);
                mergeInfo.addMergeMessage("history updated");
            }

            // Copy last modified
            dest.lastModified = src.lastModified;

            // Mark as dirty due to changes
            dest.database.setDirty(true);

            mergeInfo.addMergeMessage("updated node properties");
        }
        else if (src.lastModified < dest.lastModified)
        {
            dest.database.setDirty(true);
            mergeInfo.addMergeMessage("node older on remote side");
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

    void mergeDestNodeChildren(MergeInfo mergeInfo, DatabaseNode src, DatabaseNode dest)
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

            // src not missing node - recursively update src and dest nodes at same level
            if (otherNode != null)
            {
                mergeNode(mergeInfo, child, otherNode);
            }

            // src deleted this node
            else if (src.deletedChildren.contains(child.id))
            {
                // Remove from our tree, this node has been deleted
                iterator.remove();
                dest.database.lookup.remove(child.id);

                mergeInfo.addMergeMessage("removed child - " + child.getPath());
                dest.database.setDirty(true);
            }
            // src is missing this node
            else
            {
                mergeInfo.addMergeMessage("remote node missing our child - " + child.getPath());
                dest.database.setDirty(true);
            }
        }
    }

    void mergeSrcNodeChildren(MergeInfo mergeInfo, DatabaseNode src, DatabaseNode dest)
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
                dest.database.setDirty(true);
            }
        }
    }

}
