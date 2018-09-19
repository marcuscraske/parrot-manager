package com.limpygnome.parrot.component.sync;

import com.limpygnome.parrot.component.backup.BackupService;
import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.file.FileComponent;
import com.limpygnome.parrot.component.session.SessionService;
import com.limpygnome.parrot.component.sync.ssh.SshSyncProfile;
import com.limpygnome.parrot.component.sync.thread.AsyncSyncThread;
import com.limpygnome.parrot.component.sync.thread.DownloadSyncThread;
import com.limpygnome.parrot.component.sync.thread.OverwriteSyncThread;
import com.limpygnome.parrot.component.sync.thread.TestSyncThread;
import com.limpygnome.parrot.component.sync.thread.UnlockSyncThread;
import com.limpygnome.parrot.event.DatabaseChangingEvent;
import com.limpygnome.parrot.lib.database.EncryptedValueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    private AsyncSyncThread asyncSyncThread;
    @Autowired
    private DownloadSyncThread downloadSyncThread;
    @Autowired
    private OverwriteSyncThread overwriteSyncThread;
    @Autowired
    private TestSyncThread testSyncThread;
    @Autowired
    private UnlockSyncThread unlockSyncThread;

    // State
    private long lastSync;
    private SyncOptions defaultSyncOptions;

    /**
     * Downloads and opens a database for the first time.
     *
     * @param options options
     * @param profile sync profile
     */
    public synchronized void download(SyncOptions options, SyncProfile profile)
    {
        threadService.launchAsync(downloadSyncThread, options, profile);
    }

    /**
     * Tests the given host options.
     *
     * @param options options
     * @param profile sync profile
     * @return error message; or null if successful/no issues encountered
     */
    public synchronized void test(SyncOptions options, SyncProfile profile)
    {
        threadService.launchAsync(testSyncThread, options, profile);
    }

    /**
     * Overwrites the remote database with the current database.
     *
     * @param options options
     * @param profile sync profile
     */
    public synchronized void overwrite(SyncOptions options, SyncProfile profile)
    {
        threadService.launchAsync(overwriteSyncThread, options, profile);
    }

    /**
     * Unlocks the remote database, by removing the associated lock file.
     *
     * @param options options
     * @param profile sync profile
     */
    public synchronized void unlock(SyncOptions options, SyncProfile profile)
    {
        threadService.launchAsync(unlockSyncThread, options, profile);
    }

    /**
     * Synchronizes all the hosts.
     */
    public synchronized void syncAll()
    {
        LOG.info("syncing all hosts...");

        List<SyncProfile> profileList = new LinkedList<>();

        // Add all the profiles that can be synchronized for this host
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
            threadService.launchAsync(asyncSyncThread, defaultSyncOptions, profileArray);
        }
        else
        {
            LOG.debug("no hosts applicable for sync");
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
        threadService.launchAsync(asyncSyncThread, options, profile);
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
     * Creates a temporary instance of sync options. This will inherit the default options.
     *
     * Used primarily for when creating new databases, or manually syncing with a different password.
     *
     * Invoking this method another time will likely cause the original object ot be garbage collected.
     *
     * @return an instance
     */
    public SyncOptions createTemporaryOptions()
    {
        SyncOptions options = new SyncOptions(defaultSyncOptions);
        sessionService.put("syncService.temproaryOptions", options);
        return options;
    }

}
