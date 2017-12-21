package com.limpygnome.parrot.component.remote;

import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.database.EncryptedValueService;
import com.limpygnome.parrot.component.file.FileComponent;
import com.limpygnome.parrot.component.session.SessionService;
import com.limpygnome.parrot.component.ui.WebStageInitService;
import com.limpygnome.parrot.component.ui.WebViewStage;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.db.DatabaseMerger;
import com.limpygnome.parrot.library.db.DatabaseNode;
import com.limpygnome.parrot.library.db.MergeLog;
import com.limpygnome.parrot.library.io.DatabaseReaderWriter;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for synchronizing database files remotely.
 */
@Service
public class RemoteSyncService
{
    private static final Logger LOG = LogManager.getLogger(RemoteSyncService.class);

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
    private WebStageInitService webStageInitService;
    @Autowired
    private SshSyncService sshSyncService;

    // State
    private Thread thread;
    private long lastSync;

    /**
     * Creates options from a set of mandatory values.
     *
     * @param randomToken a random token for retrieving the download/upload status, equivalent to e.g. a ticket or tx id
     * @param name the name of the options, used later for persistence
     * @param host the remote host
     * @param port the remote port
     * @param user the remote logon user
     * @param remotePath the remote path of the database
     * @param destinationPath the local path of where to save a local copy of the database
     * @return a new instance
     */
    public SshOptions createOptions(String randomToken, String name, String host, int port, String user, String remotePath, String destinationPath)
    {
        // Create new instance
        SshOptions options = new SshOptions(randomToken, name, host, port, user, remotePath, destinationPath);

        // Persist to session to avoid gc; it's possible multiple options could be made and this won't work, but it'll
        // do for now
        sessionService.put(SESSION_KEY_OPTIONS, options);

        return options;
    }

    /**
     * Creates options from a database node, which is under the standard remote-sync key and saved in the standard
     * JSON format.
     *
     * @param node the node with remote-sync config saved as its value
     * @return the options
     * @throws Exception {@see SshOptions}
     */
    public SshOptions createOptionsFromNode(DatabaseNode node) throws Exception
    {
        SshOptions options = SshOptions.read(encryptedValueService, node);

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
            result = sshSyncService.download(options);
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
            result = sshSyncService.test(options);
        }

        return result;
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
            String remotePassword = databaseService.getPassword();

            // Check and convert each node/host
            String currentHostName = getCurrentHostName();

            SshOptions options;
            for (DatabaseNode node : remoteSync.getChildren())
            {
                try
                {
                    options = SshOptions.read(encryptedValueService, node);

                    if (canAutoSync(options, currentHostName))
                    {
                        options.setDestinationPath(destinationPath);
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
                syncLaunchAsyncThread(remotePassword, optionsArray);
            }
            else
            {
                LOG.debug("no hosts applicable for sync");
            }
        }

        // update last sync time
        lastSync = System.currentTimeMillis();
    }

    private boolean canAutoSync(SshOptions options, String currentHostName)
    {
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

    public synchronized void sync(SshOptions options)
    {
        String remotePassword = databaseService.getPassword();
        syncWithAuth(options, remotePassword);
    }

    public synchronized void syncWithAuth(SshOptions options, String remotePassword)
    {
        syncLaunchAsyncThread(remotePassword, options);
    }

    private void syncLaunchAsyncThread(String remotePassword, SshOptions... optionsArray)
    {
        if (thread == null)
        {
            LOG.info("launching separate thread for sync");

            // Start separate thread for sync to prevent blocking
            thread = new Thread(() -> {
                syncAsyncThreadList(remotePassword, optionsArray);
            });
            thread.start();
        }
        else
        {
            LOG.error("attempted to sync whilst sync already in progress");
        }
    }

    private void syncAsyncThreadList(String remotePassword, SshOptions... optionsArray)
    {
        for (SshOptions options : optionsArray)
        {
            syncAsyncThreadSync(options, remotePassword);
        }
    }

    private void syncAsyncThreadSync(SshOptions options, String remotePassword)
    {
        WebViewStage stage = webStageInitService.getStage();

        // trigger sync is starting...
        stage.triggerEvent("document", "remoteSyncStart", options);

        // validate destination path
        SyncResult syncResult;

        String messages = checkDestinationPath(options);
        if (messages != null)
        {
            syncResult = new SyncResult(messages, false, false, options.getName());
        }
        else
        {
            // sync...
            syncResult = sshSyncService.sync(options, remotePassword);
        }

        // trigger end event...
        stage.triggerEvent("document", "remoteSyncFinish", syncResult);

        thread = null;
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
     * Aborts any SSH connection currently in progress.
     */
    public synchronized void abort()
    {
        // Wake thread, just in case...
        thread.interrupt();

        // Dispose session as well
        if (thread != null)
        {
            sshSyncService.cleanup();
            thread = null;
        }
    }

    /**
     * @return epoch ms timestamp of last sync all
     */
    public long getLastSync()
    {
        return lastSync;
    }

}
