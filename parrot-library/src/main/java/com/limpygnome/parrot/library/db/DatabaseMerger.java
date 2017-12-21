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
     * @param remote the database being merged into the destination
     * @param local the database to have the final version of everything
     * @param password the password for the destination database
     * @return a log of actions performed on the database
     * @throws Exception if a crypto operation fails
     */
    public synchronized MergeLog merge(Database remote, Database local, char[] password) throws Exception
    {
        MergeLog mergeLog = new MergeLog();

        synchronized (remote)
        {
            synchronized (local)
            {
                // check if databases are the same, skip if so...
                if (local.equals(remote))
                {
                    mergeLog.add("no changes detected");
                }
                else
                {
                    mergeLog.add("changes detected, merging...");

                    // merge crypto params
                    mergeDatabaseFileCryptoParams(mergeLog, remote, local, password);
                    mergeDatabaseMemoryCryptoParams(mergeLog, remote, local, password);

                    // merge nodes
                    boolean changed = mergeNode(mergeLog, local, remote.getRoot(), local.getRoot());

                    if (changed)
                    {
                        local.setDirty(true);
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
    boolean mergeNode(MergeLog mergeLog, Database local, DatabaseNode remoteNode, DatabaseNode localNode)
    {
        boolean changed;

        changed  = mergeNodeProperties(mergeLog, remoteNode, localNode);
        changed |= mergeLocalNodeChildren(mergeLog, local, remoteNode, localNode);
        changed |= mergeRemoteNodeChildren(mergeLog, local, remoteNode, localNode);

        return changed;
    }

    boolean mergeNodeProperties(MergeLog mergeLog, DatabaseNode remoteNode, DatabaseNode localNode)
    {
        boolean changed = false;

        // Check if this db was modified before/after
        if (remoteNode.getLastModified() > localNode.getLastModified())
        {
            // Compare/clone first level props
            // -- Name
            if (isDifferent(localNode.getName(), remoteNode.getName()))
            {
                localNode.setName(remoteNode.getName());
                mergeLog.add(localNode, "changing name to '" + remoteNode.getName() + "'");
            }

            // -- Value
            if (isDifferent(localNode.getValue(), remoteNode.getValue()))
            {
                EncryptedValue srcValue = remoteNode.getValue();
                localNode.setValue(srcValue.clone());
                mergeLog.add(localNode, "value updated");
            }

            // -- History
            if (isDifferent(localNode.getHistory(), remoteNode.getHistory()))
            {
                localNode.getHistory().merge(remoteNode.getHistory());
                mergeLog.add(localNode, "history updated");
            }

            // Copy last modified
            localNode.setLastModified(remoteNode.getLastModified());

            // Mark as dirty due to changes
            changed = true;

            mergeLog.add(localNode, "updated node properties");
        }
        else if (remoteNode.getLastModified() < localNode.getLastModified())
        {
            changed = true;
            mergeLog.add(localNode, "node older on remote side");
        }

        // Merge any deleted items
        boolean change = localNode.getDeletedChildren().addAll(remoteNode.getDeletedChildren());

        // Set dirty flag
        if (change)
        {
            mergeLog.add(localNode, "updated list of deleted nodes");
            changed = true;
        }

        return changed;
    }

    /*
        Updates all nodes in the current database, so they're in sync with the remote database.
     */
    boolean mergeLocalNodeChildren(MergeLog mergeLog, Database local, DatabaseNode remoteNode, DatabaseNode localNode)
    {
        boolean changed = false;

        DatabaseNode localChild;
        DatabaseNode remoteChild;

        Map<UUID, DatabaseNode> localChildren = localNode.getChildrenMap();
        Iterator<Map.Entry<UUID, DatabaseNode>> iterator = localChildren.entrySet().iterator();
        Map.Entry<UUID, DatabaseNode> kv;

        Map<UUID, DatabaseNode> srcChildren = remoteNode.getChildrenMap();

        while (iterator.hasNext())
        {
            kv = iterator.next();
            localChild = kv.getValue();

            remoteChild = srcChildren.get(kv.getKey());

            // remote not missing node - recursively update src and dest nodes at same level
            if (remoteChild != null)
            {
                changed |= mergeNode(mergeLog, local, remoteChild, localChild);
            }

            // remote database deleted this local node
            else if (remoteNode.getDeletedChildren().contains(localChild.getUuid()))
            {
                // Remove from collection
                iterator.remove();

                // Remove node from our database as it has been removed remotely
                localChild.remove();

                mergeLog.add(localNode, "removed child - " + remoteChild.getPath());
                changed = true;
            }

            // remote database is missing this local node
            else
            {
                mergeLog.add(localNode, "remote node missing our child - " + remoteChild.getPath());
                changed = true;
            }
        }

        return changed;
    }

    /*
        Adds any new nodes in the remote database.
     */
    boolean mergeRemoteNodeChildren(MergeLog mergeLog, Database local, DatabaseNode remoteNode, DatabaseNode localNode)
    {
        boolean changed = false;

        DatabaseNode remoteChild;
        DatabaseNode newNode;
        boolean isDeleted;

        Map<UUID, DatabaseNode> remoteChildren = remoteNode.getChildrenMap();
        Map<UUID, DatabaseNode> localChildren = localNode.getChildrenMap();

        for (Map.Entry<UUID, DatabaseNode> kv : remoteChildren.entrySet())
        {
            remoteChild = kv.getValue();

            // New node on their side that we don't have...
            isDeleted = localNode.getDeletedChildren().contains(remoteChild.getUuid());

            if (!localChildren.containsKey(remoteChild.getUuid()) && !isDeleted)
            {
                newNode = remoteChild.clone(local);
                localNode.add(newNode);
                mergeLog.add(localNode, "added child - " + newNode.getPath());
                changed = true;
            }
            else if (isDeleted)
            {
                // Looks like we deleted the current node on our side...
                mergeLog.add(localNode, "node already deleted in local database - " + remoteChild.getPath());
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
