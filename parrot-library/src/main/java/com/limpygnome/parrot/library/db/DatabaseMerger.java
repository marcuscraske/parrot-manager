package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.crypto.CryptoParams;
import com.limpygnome.parrot.library.crypto.EncryptedValue;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Provides ability to merge databases.
 *
 * Node properties are local only.
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
    public synchronized MergeLog merge(Database source, Database destination, char[] password) throws Exception
    {
        MergeLog mergeLog = new MergeLog();

        synchronized (source)
        {
            synchronized (destination)
            {
                // check if databases are the same, skip if so...
                if (destination.equals(source))
                {
                    mergeLog.add("no changes detected");
                }
                else
                {
                    mergeLog.add("changes detected, merging...");

                    // merge crypto params
                    mergeDatabaseFileCryptoParams(mergeLog, source, destination, password);
                    mergeDatabaseMemoryCryptoParams(mergeLog, source, destination, password);

                    // merge nodes
                    boolean changed = mergeNode(mergeLog, destination, source.getRoot(), destination.getRoot());

                    if (changed)
                    {
                        destination.setDirty(true);
                    }

                    // set flag that remote needs syncing
                    // TODO make more efficient by only syncing with remote if there's actual remote changes
                    // TODO update unit tests
                    mergeLog.setRemoteOutOfDate(true);
                }
            }
        }

        return mergeLog;
    }

    void mergeDatabaseFileCryptoParams(MergeLog mergeLog, Database source, Database destination, char[] password) throws Exception
    {
        CryptoParams destFileCryptoParams = destination.getFileCryptoParams();
        CryptoParams srcFileCryptoParams = source.getFileCryptoParams();

        if (destFileCryptoParams.getLastModified() < srcFileCryptoParams.getLastModified())
        {
            destination.updateFileCryptoParams(srcFileCryptoParams, password);
            mergeLog.add("updated file crypto parameters");
        }
        else if (destFileCryptoParams.getLastModified() > srcFileCryptoParams.getLastModified())
        {
            destination.setDirty(true);
            mergeLog.add("local file params are newer");
        }
    }

    void mergeDatabaseMemoryCryptoParams(MergeLog mergeLog, Database source, Database destination, char[] password) throws Exception
    {
        CryptoParams destMemoryCryptoParams = destination.getMemoryCryptoParams();
        CryptoParams srcMemoryCryptoParams = source.getMemoryCryptoParams();

        if (destMemoryCryptoParams.getLastModified() < srcMemoryCryptoParams.getLastModified())
        {
            destination.updateMemoryCryptoParams(srcMemoryCryptoParams, password);
            mergeLog.add("updated memory crypto parameters");
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
    boolean mergeNode(MergeLog mergeLog, Database destination, DatabaseNode src, DatabaseNode dest)
    {
        boolean changed;

        changed  = mergeNodeProperties(mergeLog, src, dest);
        changed |= mergeDestNodeChildren(mergeLog, destination, src, dest);
        changed |= mergeSrcNodeChildren(mergeLog, destination, src, dest);

        return changed;
    }

    boolean mergeNodeProperties(MergeLog mergeLog, DatabaseNode src, DatabaseNode dest)
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
                mergeLog.add(dest, "changing name to '" + src.getName() + "'");
            }

            // -- Value
            if (isDifferent(dest.getValue(), src.getValue()))
            {
                EncryptedValue srcValue = src.getValue();
                dest.setValue(srcValue.clone());
                mergeLog.add(dest, "value updated");
            }

            // -- History
            if (isDifferent(dest.getHistory(), src.getHistory()))
            {
                dest.getHistory().merge(src.getHistory());
                mergeLog.add(dest, "history updated");
            }

            // Copy last modified
            dest.setLastModified(src.getLastModified());

            // Mark as dirty due to changes
            changed = true;

            mergeLog.add(dest, "updated node properties");
        }
        else if (src.getLastModified() < dest.getLastModified())
        {
            changed = true;
            mergeLog.add(dest, "node older on remote side");
        }

        // Merge any deleted items
        boolean change = dest.getDeletedChildren().addAll(src.getDeletedChildren());

        // Set dirty flag
        if (change)
        {
            mergeLog.add(dest, "updated list of deleted nodes");
            changed = true;
        }

        return changed;
    }

    boolean mergeDestNodeChildren(MergeLog mergeLog, Database destination, DatabaseNode src, DatabaseNode dest)
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
                changed |= mergeNode(mergeLog, destination, child, otherNode);
            }

            // src deleted this node
            else if (src.getDeletedChildren().contains(child.getUuid()))
            {
                // Remove from collection
                iterator.remove();

                // Remove node from our database as it has been removed remotely
                child.remove();

                mergeLog.add(dest, "removed child - " + child.getPath());
                changed = true;
            }

            // src is missing this node
            else
            {
                mergeLog.add(dest, "remote node missing our child - " + child.getPath());
                changed = true;
            }
        }

        return changed;
    }

    boolean mergeSrcNodeChildren(MergeLog mergeLog, Database destination, DatabaseNode src, DatabaseNode dest)
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
                mergeLog.add(dest, "added child - " + newNode.getPath());
                changed = true;
            }
            else if (isDeleted)
            {
                // Looks like we deleted the current node on our side...
                mergeLog.add(dest, "node already deleted in local database - " + otherChild.getPath());
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
