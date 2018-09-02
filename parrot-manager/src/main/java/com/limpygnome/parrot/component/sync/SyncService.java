package com.limpygnome.parrot.component.sync;

import com.limpygnome.parrot.component.backup.BackupService;
import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.sync.ssh.SshSyncProfile;
import com.limpygnome.parrot.component.sync.ssh.SshSyncHandler;
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
    private BackupService backupService;
    @Autowired
    private SyncProfileService syncProfileService;
    @Autowired
    private SyncThreadService threadService;

    // State
    private long lastSync;
    private SyncOptions defaultSyncOptions;

    /**
     * Begins downloading a file from host.
     *
     * @param options options
     * @param profile sync profile
     * @return error message, otherwise null if successful
     */
    public synchronized String download(SyncOptions options, SyncProfile profile)
    {
        SyncHandler handler = syncProfileService.getHandlerForProfile(profile);
        String result = checkDestinationPath(options);

        if (result == null)
        {
            result = handler.download(options, profile);
        }

        return result;
    }

    /**
     * Tests the given host options.
     *
     * @param options options
     * @param profile sync profile
     * @return error message; or null if successful/no issues encountered
     */
    public synchronized String test(SyncOptions options, SyncProfile profile)
    {
        SyncHandler handler = syncProfileService.getHandlerForProfile(profile);
        String result = checkDestinationPath(options);

        if (result == null)
        {
            result = handler.test(options, profile);
        }

        return result;
    }

    /**
     * Overwrites the remote database with the current database.
     *
     * @param options options
     * @param profile sync profile
     */
    public synchronized void overwrite(SyncOptions options, SyncProfile profile)
    {
        SyncHandler handler = syncProfileService.getHandlerForProfile(profile);
        threadService.launchAsync(new SyncThread()
        {
            @Override
            public SyncResult execute(SyncOptions options, SyncProfile profile)
            {
                return handler.overwrite(options, profile);
            }
        }, options, profile);
    }

    /**
     * Unlocks the remote database, by removing the associated lock file.
     *
     * @param options options
     * @param profile sync profile
     */
    public synchronized void unlock(SyncOptions options, SyncProfile profile)
    {
        SyncHandler handler = syncProfileService.getHandlerForProfile(profile);
        threadService.launchAsync(new SyncThread()
        {
            @Override
            public SyncResult execute(SyncOptions options, SyncProfile profile)
            {
                return handler.unlock(options, profile);
            }
        }, options, profile);
    }

    /**
     * Synchronizes all the hosts.
     */
    public synchronized void syncAll()
    {
        LOG.info("syncing all hosts...");

        List<SyncProfile> profileList = new LinkedList<>();

        // Add all applicable hosts
        Database database = databaseService.getDatabase();
        DatabaseNode remoteSync = database.getRoot().getByName("remote-sync");

        if (remoteSync != null)
        {
            // Check and convert each node/host
            String currentHostName = getCurrentHostName();

            for (SyncProfile profile : syncProfileService.fetch())
            {
                if (canAutoSync(defaultSyncOptions, profile, currentHostName))
                {
                    profileList.add(profile);
                }
            }

            // Perform sync if we have anything
            if (!profileList.isEmpty())
            {
                LOG.debug("{} available hosts for sync", profileList.size());

                SyncProfile[] profileArray = profileList.toArray(new SshSyncProfile[profileList.size()]);
                launchAsyncSync(defaultSyncOptions, profileArray);
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
    public synchronized void sync(SyncOptions options, SyncProfile profile)
    {
        launchAsyncSync(options, profile);
    }

    private boolean canAutoSync(SyncOptions options, SyncProfile profile, String currentHostName)
    {
        SyncHandler handler = syncProfileService.getHandlerForProfile(profile);

        // check backup not open (should never happen)
        if (backupService.isBackupOpen())
        {
            LOG.info("excluded from sync as backup database is open - profile: {}", profile.getName());
            return false;
        }

        // check if auth is needed
        if (!handler.canAutoSync(options, profile))
        {
            LOG.info("excluded from auto-sync due to handler - profile: {}", profile.getName());
            return false;
        }

        // check machine filter
        String machineFilter = profile.getMachineFilter();

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
                    LOG.info("excluded from sync current host not matched in machine filter - profile: {}, hostName: {}", profile.getName(), currentHostName);
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

    private void launchAsyncSync(SyncOptions options, SyncProfile... profileArray)
    {
        threadService.launchAsync(new SyncThread()
        {
            @Override
            public SyncResult execute(SyncOptions options, SyncProfile profile)
            {
                return asyncSync(options, profile);
            }
        }, options, profileArray);
    }

    private SyncResult asyncSync(SyncOptions options, SyncProfile profile)
    {
        SyncResult syncResult;
        MergeLog mergeLog = new MergeLog();

        // check there isn't unsaved database changes
        if (databaseService.isDirty())
        {
            LOG.warn("skipped sync due to unsaved database changes");
            mergeLog.add(new LogItem(LogLevel.ERROR, "Skipped sync due to unsaved database changes"));
            syncResult = new SyncResult(profile.getName(), mergeLog, false, false);
        }
        else
        {
            // validate destination path
            String message = checkDestinationPath(options);
            if (message != null)
            {
                mergeLog.add(new LogItem(LogLevel.ERROR, message));
                syncResult = new SyncResult(profile.getName(), mergeLog, false, false);
            }
            else
            {
                // sync...
                SyncHandler handler = syncProfileService.getHandlerForProfile(profile);
                syncResult = handler.sync(options, profile);
            }
        }

        return syncResult;
    }

    private String checkDestinationPath(SyncOptions options)
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
        if (open)
        {
            // Setup default sync options for current database
            String databasePassword = databaseService.getPassword();
            String destinationPath = databaseService.getPath();
            defaultSyncOptions = new SyncOptions(databasePassword, destinationPath);
        }
        else
        {
            // Reset default sync options
            defaultSyncOptions = null;
        }

        abort();
    }

    /**
     * @return epoch ms timestamp of last sync all
     */
    public long getLastSync()
    {
        return lastSync;
    }

    /**
     * @return the default auto-sync options for the current database open
     */
    public SyncOptions getDefaultSyncOptions()
    {
        return defaultSyncOptions;
    }

    /**
     * Creates a temporary instance of sync options.
     *
     * Used primarily for when creating new databases, or manually syncing with a different password.
     *
     * Invoking this method another time will likely cause the original object ot be garbage collected.
     *
     * @param databasePassword database password
     * @param destinationPath destination path
     * @return an instance
     */
    public SyncOptions createTemproaryOptions(String databasePassword, String destinationPath)
    {
        SyncOptions options = new SyncOptions(databasePassword, destinationPath);
        sessionService.put("syncService.temproaryOptions", options);
        return options;
    }

}
