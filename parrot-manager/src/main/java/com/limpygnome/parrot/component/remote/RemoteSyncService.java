package com.limpygnome.parrot.component.remote;

import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.database.EncryptedValueService;
import com.limpygnome.parrot.component.remote.auth.RemoteSyncPasswordService;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.db.DatabaseNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Performs sync of databases.
 */
@Service
public class RemoteSyncService
{
    private static final Logger LOG = LogManager.getLogger(RemoteSyncService.class);

    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private RemoteSshFileService remoteSshFileService;
    @Autowired
    private EncryptedValueService encryptedValueService;

    public String createHost(
            String name,
            String host,
            int port,
            String user,
            String remotePath,
            String userPass,
            String privateKeyPath,
            String privateKeyPass,
            String proxyHost,
            int proxyPort,
            String proxyType,
            boolean strictHostChecking
    ) {
        try
        {
            // Build SSH options
            SshOptions options = new SshOptions();

            options.setName(name);
            options.setHost(host);
            options.setPort(port);
            options.setUser(user);
            options.setRemotePath(remotePath);
            options.setRemotePath(userPass);
            options.setPrivateKeyPath(privateKeyPath);
            options.setPrivateKeyPass(privateKeyPass);
            options.setProxyHost(proxyHost);
            options.setPort(proxyPort);
            options.setProxyType(proxyType);
            options.setStrictHostChecking(strictHostChecking);

            // Persist
            Database database = databaseService.getDatabase();
            options.persist(encryptedValueService, database);
        }
        catch (Exception e)
        {
            LOG.error("failed to persist options", e);
            return e.getMessage();
        }
    }

    public void syncAll()
    {
        DatabaseNode remoteSync = getRemoteSyncNode();
        SshOptions options;

        for (DatabaseNode host : remoteSync.getChildren())
        {
            try
            {
                options = remoteSyncPasswordService.createWithAuth(host);
                sync(options);
            }
            catch (Exception e)
            {
                LOG.error("failed to sync node", e);
            }
        }
    }

    public void sync(String nodeId)
    {
        try
        {
            DatabaseNode host;

            synchronized (databaseService)
            {
                Database database = databaseService.getDatabase();
                host = database.getNode(nodeId);
                SshOptions options = remoteSyncPasswordService.createWithAuth(host);
                sync(options);
            }
        }
        catch (Exception e)
        {
            LOG.error("failed to sync node - id: {}", nodeId, e);
        }
    }

    /**
     * Synchronizes a host.
     *
     * @param options host options
     */
    public void sync(SshOptions options)
    {
        Database database = databaseService.getDatabase();
        remoteSshFileService.sync(database, options);
    }

    private DatabaseNode getRemoteSyncNode()
    {
        DatabaseNode remoteSync;

        synchronized (databaseService)
        {
            if (!databaseService.isOpen())
            {
                throw new IllegalStateException("attempted to sync whilst database was closed");
            }

            Database database = databaseService.getDatabase();
            DatabaseNode root = database.getRoot();
            remoteSync = root.getByName("remote-sync");
        }

        return remoteSync;
    }

    private SshOptions getSshOptions(DatabaseNode node) throws Exception
    {
        SshOptions options = SshOptions.read(encryptedValueService, node);
        return options;
    }

}
