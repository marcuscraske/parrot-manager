package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.crypto.CryptoParams;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import com.limpygnome.parrot.library.log.Log;
import com.limpygnome.parrot.library.log.LogItem;
import com.limpygnome.parrot.library.log.LogLevel;

import java.util.HashMap;
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
     * @param password the password for the destination database; if null, crypto params are not merged
     * @return a log of actions performed on the database
     * @throws Exception if a crypto operation fails
     */
    public synchronized Log merge(Database remote, Database local, char[] password) throws Exception
    {
        Log log = new Log();

        synchronized (remote)
        {
            synchronized (local)
            {
                Changed changed = new Changed();

                // Disable events
                remote.setEventsEnabled(false);
                local.setEventsEnabled(false);

                // check if databases are the same...
                if (!local.equals(remote))
                {
                    // merge crypto params
                    if (password != null && password.length > 0)
                    {
                        Changed changedFileCrypto = mergeDatabaseFileCryptoParams(log, remote, local, password);
                        Changed changedMemoryCrypto = mergeDatabaseMemoryCryptoParams(log, remote, local, password);
                        changed.merge(changedFileCrypto, changedMemoryCrypto);
                    }

                    // merge nodes
                    Changed changedNodes = mergeNode(log, local, remote.getRoot(), local.getRoot());
                    changed.merge(changedNodes);

                    if (changed.isLocal())
                    {
                        log.add(new LogItem(LogLevel.INFO, true, "Database has changed"));
                        local.setDirty(true);
                    }
                    if (changed.isRemote())
                    {
                        log.add(new LogItem(LogLevel.INFO, false, "Remote database has changes"));
                        remote.setDirty(true);

                        // set flag that remote needs syncing
                        log.setRemoteOutOfDate(true);
                    }
                }

                if (!changed.isAnyChange())
                {
                    log.add(new LogItem(LogLevel.INFO, true, "No changes"));
                }

                // Enable events
                local.setEventsEnabled(true);
                remote.setEventsEnabled(true);
            }
        }

        return log;
    }

    Changed mergeDatabaseFileCryptoParams(Log log, Database source, Database destination, char[] password) throws Exception
    {
        Changed changed = new Changed();

        CryptoParams destFileCryptoParams = destination.getFileCryptoParams();
        CryptoParams srcFileCryptoParams = source.getFileCryptoParams();

        if (destFileCryptoParams.getLastModified() < srcFileCryptoParams.getLastModified())
        {
            destination.updateFileCryptoParams(srcFileCryptoParams, password);
            log.add(new LogItem(LogLevel.DEBUG, true, "Updated file crypto parameters"));
            changed.localChanged();
        }
        else if (destFileCryptoParams.getLastModified() > srcFileCryptoParams.getLastModified())
        {
            log.add(new LogItem(LogLevel.DEBUG, false, "Local file params are newer"));
            changed.remoteChanged();
        }

        return changed;
    }

    Changed mergeDatabaseMemoryCryptoParams(Log log, Database source, Database destination, char[] password) throws Exception
    {
        Changed changed = new Changed();

        CryptoParams destMemoryCryptoParams = destination.getMemoryCryptoParams();
        CryptoParams srcMemoryCryptoParams = source.getMemoryCryptoParams();

        if (destMemoryCryptoParams.getLastModified() < srcMemoryCryptoParams.getLastModified())
        {
            destination.updateMemoryCryptoParams(srcMemoryCryptoParams, password);
            log.add(new LogItem(LogLevel.DEBUG, true, "Updated memory crypto parameters"));
            changed.localChanged();
        }
        else if (destMemoryCryptoParams.getLastModified() > srcMemoryCryptoParams.getLastModified())
        {
            log.add(new LogItem(LogLevel.DEBUG, false, "Local memory crypto parameters are newer"));
            changed.remoteChanged();
        }

        return changed;
    }

    /*
        MERGE NODE

        Merges the passed node with this node.

        Both nodes should be at the same level in their respected databases.
     */
    Changed mergeNode(Log log, Database local, DatabaseNode remoteNode, DatabaseNode localNode)
    {
        Changed changed = new Changed();

        Changed changedNodeProperties = mergeNodeProperties(log, remoteNode, localNode);
        Changed changedLocalAgainstRemote = checkLocalChildrenAgainstRemote(log, local, remoteNode, localNode);
        Changed changedRemote = mergeRemoteNodeChildren(log, local, remoteNode, localNode);

        changed.merge(changedNodeProperties, changedLocalAgainstRemote, changedRemote);
        return changed;
    }

    Changed mergeNodeProperties(Log log, DatabaseNode remoteNode, DatabaseNode localNode)
    {
        Changed changed = new Changed();

        // Check if this db was modified before/after
        if (remoteNode.getLastModified() > localNode.getLastModified())
        {
            // Compare/clone first level props
            // -- Name
            if (isDifferent(localNode.getName(), remoteNode.getName()))
            {
                localNode.setName(remoteNode.getName());
                log.add(new LogItem(LogLevel.DEBUG, true, localNode, "Updated name"));
            }

            // -- Value
            if (isDifferent(localNode.getValue(), remoteNode.getValue()))
            {
                EncryptedValue srcValue = remoteNode.getValue();
                if (srcValue != null)
                {
                    localNode.setValue(srcValue.clone());
                }
                else
                {
                    localNode.setValue(null);
                }

                log.add(new LogItem(LogLevel.DEBUG, true, localNode, "Updated value"));
            }

            // -- History
            if (isDifferent(localNode.getHistory(), remoteNode.getHistory()))
            {
                localNode.getHistory().merge(remoteNode.getHistory());
                log.add(new LogItem(LogLevel.DEBUG, true, localNode, "History updated"));
            }

            // Copy last modified
            localNode.setLastModified(remoteNode.getLastModified());

            // Mark as dirty due to changes
            changed.localChanged();
        }
        else if (remoteNode.getLastModified() < localNode.getLastModified())
        {
            log.add(new LogItem(LogLevel.DEBUG, true, localNode, "Remote version is older"));
            changed.remoteChanged();
        }

        // Merge any deleted items
        boolean localDeletedChildrenChanged = localNode.getDeletedChildren().addAll(remoteNode.getDeletedChildren());

        // Set dirty flag
        if (localDeletedChildrenChanged)
        {
            log.add(new LogItem(LogLevel.DEBUG, true, localNode, "Updated list of deleted nodes"));
            changed.localChanged();
        }

        return changed;
    }

    /*
        Updates all nodes in the current database, so they're in sync with the remote database.
     */
    Changed checkLocalChildrenAgainstRemote(Log log, Database local, DatabaseNode remoteNode, DatabaseNode localNode)
    {
        Changed changed = new Changed();

        DatabaseNode localChild;
        DatabaseNode remoteChild;

        Map<UUID, DatabaseNode> localChildren = createMap(localNode.getChildren());
        Iterator<Map.Entry<UUID, DatabaseNode>> iterator = localChildren.entrySet().iterator();
        Map.Entry<UUID, DatabaseNode> kv;

        Map<UUID, DatabaseNode> srcChildren = createMap(remoteNode.getChildren());

        while (iterator.hasNext())
        {
            kv = iterator.next();
            localChild = kv.getValue();

            remoteChild = srcChildren.get(kv.getKey());

            // remote not missing node - recursively update src and dest nodes at same level
            if (remoteChild != null)
            {
                Changed childChanged = mergeNode(log, local, remoteChild, localChild);
                changed.merge(childChanged);
            }

            // remote database deleted this local node
            else if (remoteNode.getDeletedChildren().contains(localChild.getUuid()))
            {
                // Remove from collection
                iterator.remove();

                // Remove node from our database as it has been removed remotely
                localChild.remove();

                log.add(new LogItem(LogLevel.REMOVED, true, localChild, "Removed"));
                changed.localChanged();
            }

            // remote database is missing this local node
            else
            {
                log.add(new LogItem(LogLevel.DEBUG, false, localChild, "Remote database missing node"));
                changed.remoteChanged();
            }
        }

        return changed;
    }

    /*
        Adds any new nodes in the remote database.
     */
    Changed mergeRemoteNodeChildren(Log log, Database local, DatabaseNode remoteNode, DatabaseNode localNode)
    {
        Changed changed = new Changed();

        DatabaseNode remoteChild;
        DatabaseNode newNode;
        boolean isDeleted;

        Map<UUID, DatabaseNode> remoteChildren = createMap(remoteNode.getChildren());
        Map<UUID, DatabaseNode> localChildren = createMap(localNode.getChildren());

        for (Map.Entry<UUID, DatabaseNode> kv : remoteChildren.entrySet())
        {
            remoteChild = kv.getValue();

            // New node on their side that we don't have...
            isDeleted = localNode.getDeletedChildren().contains(remoteChild.getUuid());

            if (!localChildren.containsKey(remoteChild.getUuid()) && !isDeleted)
            {
                newNode = remoteChild.clone(local);
                localNode.add(newNode);
                log.add(new LogItem(LogLevel.ADDED, true, newNode, "Added"));
                changed.localChanged();
            }
            else if (isDeleted)
            {
                // Looks like we deleted the current node on our side...
                log.add(new LogItem(LogLevel.DEBUG, false, remoteChild, "Node already deleted in local database"));
                changed.remoteChanged();
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

    Map<UUID, DatabaseNode> createMap(DatabaseNode[] children)
    {
        Map<UUID, DatabaseNode> result = new HashMap<>();
        for (DatabaseNode node : children)
        {
            result.put(node.getUuid(), node);
        }
        return result;
    }

}
