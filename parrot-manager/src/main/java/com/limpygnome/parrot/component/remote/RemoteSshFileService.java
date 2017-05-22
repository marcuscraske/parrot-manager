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
import com.limpygnome.parrot.library.dbaction.ActionLog;
import com.limpygnome.parrot.library.io.DatabaseReaderWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * A archive for downloading and uploading remote files using SSH.
 */
@Service
public class RemoteSshFileService
{
    private static final Logger LOG = LogManager.getLogger(RemoteSshFileService.class);

    private static final String SESSION_KEY_OPTIONS = "remoteSshOptions";

    // Components
    @Autowired
    private DatabaseReaderWriter databaseReaderWriter;
    @Autowired
    private FileComponent fileComponent;
    @Autowired
    private SshComponent sshComponent;
    @Autowired
    private DatabaseMerger databaseMerger;
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private EncryptedValueService encryptedValueService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private WebStageInitService webStageInitService;

    // State
    private Thread thread;
    private SshSession sshSession;

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
     * Begins downloading a file from an SSH host.
     *
     * Invocation is synchronous.
     *
     * @param options the config for a download
     * @return error message, otherwise null if successful
     */
    public synchronized String download(SshOptions options)
    {
        String result = null;
        sshSession = null;

        try
        {
            // Check destination path
            result = checkDestinationPath(options);

            if (result == null)
            {
                // Connect
                sshSession = sshComponent.connect(options);

                // Start download...
                sshComponent.download(sshSession, options);
            }
        }
        catch (Exception e)
        {
            result = sshComponent.getExceptionMessage(e);
            LOG.error("transfer - {} - exception", options.getRandomToken(), e);
        }
        finally
        {
            // Disconnect
            if (sshSession != null)
            {
                sshSession.dispose();
                sshSession = null;
            }
        }

        return result;
    }

    /**
     * Tests the given SSH options without downloading/uploading any files.
     *
     * @param options SSH options to be tested
     * @return error message; or null if successful/no issues encountered
     */
    public synchronized String test(SshOptions options)
    {
        String result = null;
        sshSession = null;

        try
        {
            // Check destination file first, fail fast...
            result = checkDestinationPath(options);

            if (result == null)
            {
                // Connect
                sshSession = sshComponent.connect(options);

                // Check remote connection works and file exists
                if (!sshComponent.checkRemotePathExists(options, sshSession))
                {
                    result = "Remote file does not exist - ignore if expected";
                }
            }
        }
        catch (Exception e)
        {
            result = sshComponent.getExceptionMessage(e);
            LOG.error("transfer - {} - exception", options.getRandomToken(), e);
        }
        finally
        {
            // Disconnect
            if (sshSession != null)
            {
                sshSession.dispose();
                sshSession = null;
            }
        }

        return result;
    }

    public synchronized void syncAll()
    {
        // TODO... check each host can sync i.e. doesnt need prompt
    }

    public synchronized void sync(SshOptions options)
    {
        String remotePassword = databaseService.getPassword();
        syncWithAuth(options, remotePassword);
    }

    public synchronized void syncWithAuth(SshOptions options, String remotePassword)
    {
        if (thread == null)
        {
            LOG.info("launching separate thread for sync");

            // Start separate thread for sync to prevent blocking
            thread = new Thread(() -> {
                syncBlockingThread(options, remotePassword);
            });
            thread.start();
        }
        else
        {
            LOG.error("attempted to sync whilst sync already in progress");
        }
    }

    private void syncBlockingThread(SshOptions options, String remotePassword)
    {
        String messages;
        boolean success = true;

        WebViewStage stage = webStageInitService.getStage();
        Database database = databaseService.getDatabase();

        if (database == null)
        {
            LOG.error("database is null");
            messages = "Internal error - database is null?";
        }
        else if (options == null)
        {
            LOG.error("options are null");
            messages = "Internal error - options are null?";
        }
        else if (options.getDestinationPath() == null)
        {
            LOG.warn("destination path not setup");
            messages = "Internal error - destination path must be setup on options";
        }
        else if (remotePassword == null || remotePassword.length() == 0)
        {
            LOG.warn("remote password not specified");
            messages = "Remote password is required";
        }
        else
        {

            // Raise event with stage...
            stage.triggerEvent("document", "remoteSyncStart", options);

            // Alter destination path for this host
            int fullHostNameHash = (options.getHost() + options.getPort()).hashCode();
            String syncPath = options.getDestinationPath();
            syncPath = syncPath + "." + fullHostNameHash + "." + System.currentTimeMillis() + ".sync";

            // Begin sync process...
            try
            {
                // Check destination path
                messages = checkDestinationPath(options);

                if (messages == null)
                {
                    // Connect
                    LOG.info("sync - connecting");
                    sshSession = sshComponent.connect(options);

                    // Start download...
                    LOG.info("sync - downloading");
                    boolean exists = sshComponent.download(sshSession, options, syncPath);

                    ActionLog actionLog;

                    if (exists)
                    {
                        // Load remote database
                        LOG.info("sync - loading remote database");
                        Database remoteDatabase = databaseReaderWriter.open(syncPath, remotePassword.toCharArray());

                        // Perform merge and check if any change occurred...
                        LOG.info("sync - performing merge...");
                        actionLog = databaseMerger.merge(remoteDatabase, database, remotePassword.toCharArray());

                        // Check if we need to upload...
                        if (database.isDirty())
                        {
                            // Save current database
                            LOG.info("sync - database(s) dirty, saving...");
                            databaseReaderWriter.save(database, options.getDestinationPath());

                            // Upload to remote
                            LOG.info("sync - uploading to remote host...");
                            sshComponent.upload(sshSession, options, options.getDestinationPath());
                        }
                        else
                        {
                            LOG.info("sync - neither database is dirty");
                        }
                    }
                    else
                    {
                        LOG.info("sync - uploading current database");

                        actionLog = new ActionLog();
                        actionLog.add("uploading current database, as does not exist remotely");

                        String currentPath = databaseService.getPath();
                        sshComponent.upload(sshSession, options, currentPath);

                        actionLog.add("uploaded successfully");
                    }

                    // Build result
                    String hostName = options.getName();
                    messages = actionLog.getMessages(hostName);
                }
            }
            catch (Exception e)
            {
                messages = sshComponent.getExceptionMessage(e);
                success = false;
                LOG.error("sync - {} - exception", options.getRandomToken(), e);
            }
            finally
            {
                // Cleanup sync file
                File syncFile = new File(syncPath);

                if (syncFile.exists())
                {
                    syncFile.delete();
                }

                // Disconnect
                synchronized (this)
                {
                    if (sshSession != null)
                    {
                        sshSession.dispose();
                        sshSession = null;
                    }

                    // Wipe ref to this thread
                    thread = null;
                }
            }
        }

        // Raise event with result
        SyncResult syncResult = new SyncResult(
                messages, success, database.isDirty(), options.getName()
        );
        stage.triggerEvent("document", "remoteSyncFinish", syncResult);
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
        if (thread != null && sshSession != null)
        {
            sshSession.dispose();
            sshSession = null;
        }
    }

}
