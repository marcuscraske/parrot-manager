package com.limpygnome.parrot.component.sync;

import com.limpygnome.parrot.component.backup.BackupService;
import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.sync.ssh.SshOptions;
import com.limpygnome.parrot.component.sync.ssh.SshRemoteSyncHandler;
import com.limpygnome.parrot.lib.database.EncryptedValueService;
import com.limpygnome.parrot.component.file.FileComponent;
import com.limpygnome.parrot.component.session.SessionService;
import com.limpygnome.parrot.event.DatabaseChangingEvent;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.db.DatabaseNode;
import com.limpygnome.parrot.library.db.log.LogItem;
import com.limpygnome.parrot.library.db.log.LogLevel;
import com.limpygnome.parrot.library.db.log.MergeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

/**
 * Service for synchronizing database files remotely.
 */
@Service
public class SyncService implements DatabaseChangingEvent
{
    private static final Logger LOG = LoggerFactory.getLogger(SyncService.class);

    private static final String SESSION_KEY_OPTIONS = "remoteSshOptions";

    // Components
    @Autowired
    private FileComponent fileComponent;
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private EncryptedValueService encryptedValueService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private SshRemoteSyncHandler sshRemoteSyncHandler;
    @Autowired
    private BackupService backupService;
    @Autowired
    private SyncThreadService threadService;

    // State
    private long lastSync;

    /**
     * Creates options from a set of mandatory values.
     *
     * @param name the name of the options, used later for persistence
     * @param host the remote host
     * @param port the remote port
     * @param user the remote logon user
     * @param remotePath the remote path of the database
     * @param destinationPath the local path of where to save a local copy of the database
     * @return a new instance
     */
    public SshOptions createOptions(String name, String host, int port, String user, String remotePath, String destinationPath)
    {
        // Create new instance
        SshOptions options = new SshOptions(name, host, port, user, remotePath, destinationPath);

        // Persist to session to avoid gc; it's possible multiple options could be made and this won't work, but it'll
        // do for now
        sessionService.put(SESSION_KEY_OPTIONS, options);

        return options;
    }

    /**
     * Creates options from a database node, which is under the standard remote-sync key and saved in the standard
     * JSON format.
     *
     * WARNING: do not remove, used by front-end.
     *
     * @param database database
     * @param node the node with remote-sync config saved as its value
     * @return the options
     * @throws Exception {@see SshOptions}
     */
    public SshOptions createOptionsFromNode(Database database, DatabaseNode node) throws Exception
    {
        SshOptions options = SshOptions.read(encryptedValueService, database, node);

        // Persist to session to avoid gc
        sessionService.put(SESSION_KEY_OPTIONS, options);

        return options;
    }










    /**
     * Begins downloading a file from host.
     *
     * @param options the config for a download
     * @return error message, otherwise null if successful
     */
    public synchronized String download(SshOptions options)
    {
        String result = checkDestinationPath(options);

        if (result == null)
        {
            result = sshRemoteSyncHandler.download(options);
        }

        return result;
    }

    /**
     * Tests the given host options.
     *
     * @param options SSH options to be tested
     * @return error message; or null if successful/no issues encountered
     */
    public synchronized String test(SshOptions options)
    {
        String result = checkDestinationPath(options);

        if (result == null)
        {
            result = sshRemoteSyncHandler.test(options);
        }

        return result;
    }

    /**
     * Overwrites the remote database with the current database.
     *
     * @param options options
     */
    public synchronized void overwrite(SshOptions options)
    {
        threadService.launchAsync(new SyncThread()
        {
            @Override
            public SyncResult execute(SshOptions options)
            {
                return sshRemoteSyncHandler.overwrite(options);
            }
        }, options);
    }

    /**
     * Unlocks the remote database, by removing the associated lock file.
     *
     * @param options options
     */
    public synchronized void unlock(SshOptions options)
    {
        threadService.launchAsync(new SyncThread()
        {
            @Override
            public SyncResult execute(SshOptions options)
            {
                return sshRemoteSyncHandler.unlock(options);
            }
        }, options);
    }

    /**
     * Synchronizes all the hosts.
     */
    public synchronized void syncAll()
    {
        LOG.info("syncing all hosts...");

        List<SshOptions> optionsList = new LinkedList<>();

        // Add all applicable hosts
        Database database = databaseService.getDatabase();
        DatabaseNode remoteSync = database.getRoot().getByName("remote-sync");

        if (remoteSync != null)
        {
            // Read destination path to be same as current database
            String destinationPath = databaseService.getPath();

            // Fetch remote password (current DB password)
            String databasePassword = databaseService.getPassword();

            // Check and convert each node/host
            String currentHostName = getCurrentHostName();

            SshOptions options;
            for (DatabaseNode node : remoteSync.getChildren())
            {
                try
                {
                    options = SshOptions.read(encryptedValueService, database, node);

                    if (canAutoSync(options, currentHostName))
                    {
                        options.setDestinationPath(destinationPath);
                        options.setDatabasePassword(databasePassword);
                        optionsList.add(options);
                    }
                }
                catch (Exception e)
                {
                    LOG.warn("failed to parse remote sync node - id: {}", node.getId(), e);
                }
            }

            // Perform sync if we have anything
            if (!optionsList.isEmpty())
            {
                LOG.debug("{} available hosts for sync", optionsList.size());

                SshOptions[] optionsArray = optionsList.toArray(new SshOptions[optionsList.size()]);
                launchAsyncSync(optionsArray);
            }
            else
            {
                LOG.debug("no hosts applicable for sync");
            }
        }

        // update last sync time
        lastSync = System.currentTimeMillis();
    }

    /**
     * Syncs a host.
     *
     * @param options host options
     */
    public synchronized void sync(SshOptions options)
    {
        String remotePassword = databaseService.getPassword();
        syncWithAuth(options, remotePassword);
    }

    /**
     * Invokes sync using password.
     *
     * @param options host options
     * @param remotePassword remote database's password
     */
    public synchronized void syncWithAuth(SshOptions options, String remotePassword)
    {
        options.setDatabasePassword(remotePassword);
        launchAsyncSync(options);
    }

    private boolean canAutoSync(SshOptions options, String currentHostName)
    {
        // check backup not open (should never happen)
        if (backupService.isBackupOpen())
        {
            LOG.info("excluded from sync as backup database is open - profile: {}", options.getName());
            return false;
        }

        // check if auth is needed
        if (options.isPromptKeyPass() || options.isPromptUserPass())
        {
            LOG.info("excluded from sync as auth is needed - profile: {}", options.getName());
            return false;
        }

        // check machine filter
        String machineFilter = options.getMachineFilter();

        if (machineFilter != null && currentHostName != null)
        {
            machineFilter = machineFilter.trim();

            if (machineFilter.length() > 0)
            {
                String[] hosts = machineFilter.replace(" ", ",").replace("\n", ",").split(",");

                // check current host name is in list of hosts
                boolean found = false;

                for (int i = 0; !found && i < hosts.length; i++)
                {
                    if (hosts[i].equals(currentHostName))
                    {
                        found = true;
                    }
                }

                if (!found)
                {
                    LOG.info("excluded from sync current host not matched in machine filter - profile: {}, hostName: {}", options.getName(), currentHostName);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * @return the current hostname of the machine
     */
    public String getCurrentHostName()
    {
        String result = null;

        try
        {
            result = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e)
        {
            LOG.warn("unable to determine hostname of current machine", e);
        }

        return result;
    }

    private void launchAsyncSync(SshOptions... optionsArray)
    {
        threadService.launchAsync(new SyncThread()
        {
            @Override
            public SyncResult execute(SshOptions options)
            {
                return asyncSync(options);
            }
        }, optionsArray);
    }

    private SyncResult asyncSync(SshOptions options)
    {
        SyncResult syncResult;
        MergeLog mergeLog = new MergeLog();

        // check there isn't unsaved database changes
        if (databaseService.isDirty())
        {
            LOG.warn("skipped sync due to unsaved database changes");
            mergeLog.add(new LogItem(LogLevel.ERROR, "Skipped sync due to unsaved database changes"));
            syncResult = new SyncResult(options.getName(), mergeLog, false, false);
        }
        else
        {
            // validate destination path
            String message = checkDestinationPath(options);
            if (message != null)
            {
                mergeLog.add(new LogItem(LogLevel.ERROR, message));
                syncResult = new SyncResult(options.getName(), mergeLog, false, false);
            }
            else
            {
                // sync...
                syncResult = sshRemoteSyncHandler.sync(options, options.getDatabasePassword());
            }
        }

        return syncResult;
    }

    private String checkDestinationPath(SshOptions options)
    {
        String result = null;

        // Check directory exists of local path
        String localPath = fileComponent.resolvePath(options.getDestinationPath());

        File localFile = new File(localPath);
        File parentLocalFile = localFile.getParentFile();

        if (parentLocalFile == null || !parentLocalFile.exists())
        {
            result = "Destination directory does not exist";
        }
        else if (localFile.exists() && (!localFile.canWrite() || !localFile.canRead()))
        {
            result = "Cannot read/write to existing destination path file";
        }

        return result;
    }

    /**
     * Aborts sync in progress.
     *
     * This can be safely invoked with uncertainty.
     */
    public synchronized void abort()
    {
        threadService.abort();
    }

    @Override
    public synchronized void eventDatabaseChanged(boolean open)
    {
        abort();
    }

    /**
     * @return epoch ms timestamp of last sync all
     */
    public long getLastSync()
    {
        return lastSync;
    }

}
