package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.crypto.CryptoParams;
import com.limpygnome.parrot.library.dbaction.ActionLog;

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
    public synchronized ActionLog merge(Database source, Database destination, char[] password) throws Exception
    {
        ActionLog actionLog = new ActionLog();

        synchronized (source)
        {
            synchronized (destination)
            {
                // Check if databases are the same, skip if so...
                if (destination.equals(source))
                {
                    actionLog.add("no changes detected");
                }
                else
                {
                    actionLog.add("changes detected, merging...");

                    // Merge crypto params
                    mergeDatabaseFileCryptoParams(actionLog, source, destination, password);
                    mergeDatabaseMemoryCryptoParams(actionLog, source, destination, password);

                    // Merge nodes
                    boolean changed = mergeNode(actionLog, destination, source.getRoot(), destination.getRoot());

                    if (changed)
                    {
                        destination.setDirty(true);
                    }
                }
            }
        }

        return actionLog;
    }

    void mergeDatabaseFileCryptoParams(ActionLog actionLog, Database source, Database destination, char[] password) throws Exception
    {
        CryptoParams destFileCryptoParams = destination.getFileCryptoParams();
        CryptoParams srcFileCryptoParams = source.getFileCryptoParams();

        if (destFileCryptoParams.getLastModified() < srcFileCryptoParams.getLastModified())
        {
            destination.updateFileCryptoParams(srcFileCryptoParams, password);
            actionLog.add("updated file crypto parameters");
        }
        else if (destFileCryptoParams.getLastModified() > srcFileCryptoParams.getLastModified())
        {
            destination.setDirty(true);
            actionLog.add("local file params are newer");
        }

        CryptoParams destMemoryCryptoParams = destination.getMemoryCryptoParams();
        CryptoParams srcMemoryCryptoParams = source.getMemoryCryptoParams();

        if (destMemoryCryptoParams.getLastModified() < srcMemoryCryptoParams.getLastModified())
        {
            destination.updateMemoryCryptoParams(srcMemoryCryptoParams, password);
            actionLog.add("updated memory crypto parameters");
        }
        else if (destMemoryCryptoParams.getLastModified() > srcMemoryCryptoParams.getLastModified())
        {
            destination.setDirty(true);
            actionLog.add("local memory params are newer");
        }
    }

    void mergeDatabaseMemoryCryptoParams(ActionLog actionLog, Database source, Database destination, char[] password) throws Exception
    {
        CryptoParams destMemoryCryptoParams = destination.getMemoryCryptoParams();
        CryptoParams srcMemoryCryptoParams = source.getMemoryCryptoParams();

        if (destMemoryCryptoParams.getLastModified() < srcMemoryCryptoParams.getLastModified())
        {
            destination.updateMemoryCryptoParams(srcMemoryCryptoParams, password);
            actionLog.add("updated memory crypto parameters");
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
    boolean mergeNode(ActionLog actionLog, Database destination, DatabaseNode src, DatabaseNode dest)
    {
        boolean changed;

        changed  = mergeNodeDetails(actionLog, src, dest);
        changed |= mergeDestNodeChildren(actionLog, destination, src, dest);
        changed |= mergeSrcNodeChildren(actionLog, destination, src, dest);

        return changed;
    }

    boolean mergeNodeDetails(ActionLog actionLog, DatabaseNode src, DatabaseNode dest)
    {
        boolean changed = false;

        // Check if this db was modified before/after
        if (src.getLastModified() > dest.getLastModified())
        {
            // Compare/clone first level props
            // -- Name
            if (isDifferent(dest.getName(), src.getName()))
            {
                dest.setName(src.getName());
                actionLog.add(dest, "changing name to '" + src.getName() + "'");
            }

            // -- Value
            if (isDifferent(dest.getValue(), src.getValue()))
            {
                dest.setValue(src.getValue().clone());
                actionLog.add(dest, "value updated");
            }

            // -- History
            if (isDifferent(dest.getHistory(), src.getHistory()))
            {
                dest.getHistory().cloneToNode(src);
                actionLog.add(dest, "history updated");
            }

            // Copy last modified
            dest.setLastModified(src.getLastModified());

            // Mark as dirty due to changes
            changed = true;

            actionLog.add(dest, "updated node properties");
        }
        else if (src.getLastModified() < dest.getLastModified())
        {
            changed = true;
            actionLog.add(dest, "node older on remote side");
        }

        // Merge any deleted items
        boolean change = dest.getDeletedChildren().addAll(src.getDeletedChildren());

        // Set dirty flag
        if (change)
        {
            actionLog.add(dest, "updated list of deleted nodes");
            changed = true;
        }

        return changed;
    }

    boolean mergeDestNodeChildren(ActionLog actionLog, Database destination, DatabaseNode src, DatabaseNode dest)
    {
        boolean changed = false;

        DatabaseNode child;
        DatabaseNode otherNode;

        Map<UUID, DatabaseNode> children = dest.getChildrenMap();
        Iterator<Map.Entry<UUID, DatabaseNode>> iterator = children.entrySet().iterator();
        Map.Entry<UUID, DatabaseNode> kv;

        Map<UUID, DatabaseNode> srcChildren = src.getChildrenMap();

        while (iterator.hasNext())
        {
            kv = iterator.next();
            child = kv.getValue();

            otherNode = srcChildren.get(kv.getKey());

            // src not missing node - recursively update src and dest nodes at same level
            if (otherNode != null)
            {
                mergeNode(actionLog, destination, child, otherNode);
            }

            // src deleted this node
            else if (src.getDeletedChildren().contains(child.getUuid()))
            {
                // Remove from our tree, this node has been deleted
                iterator.remove();
                destination.getLookup().remove(child.getUuid());

                actionLog.add(dest, "removed child - " + child.getPath());
                changed = true;
            }
            // src is missing this node
            else
            {
                actionLog.add(dest, "remote node missing our child - " + child.getPath());
                changed = true;
            }
        }

        return changed;
    }

    boolean mergeSrcNodeChildren(ActionLog actionLog, Database destination, DatabaseNode src, DatabaseNode dest)
    {
        boolean changed = false;

        DatabaseNode otherChild;
        DatabaseNode newNode;
        boolean isDeleted;

        Map<UUID, DatabaseNode> children = src.getChildrenMap();
        Map<UUID, DatabaseNode> destChildren = dest.getChildrenMap();

        for (Map.Entry<UUID, DatabaseNode> kv : children.entrySet())
        {
            otherChild = kv.getValue();

            // New node on their side that we don't have...
            isDeleted = dest.getDeletedChildren().contains(otherChild.getUuid());

            if (!destChildren.containsKey(otherChild.getUuid()) && !isDeleted)
            {
                newNode = otherChild.clone(destination);
                dest.add(newNode);
                actionLog.add(dest, "added child - " + newNode.getPath());
                changed = true;
            }
            else if (isDeleted)
            {
                // Looks like we deleted the current node on our side...
                actionLog.add(dest, "node already deleted in local database - " + otherChild.getPath());
                changed = true;
            }
        }

        return changed;
    }

    boolean isDifferent(Object a, Object b)
    {
        boolean differ;

        if (a != null)
        {
            differ = !a.equals(b);
        }
        else if (b != null)
        {
            differ = !b.equals(a);
        }
        else
        {
            differ = false;
        }

        return differ;
    }

}
