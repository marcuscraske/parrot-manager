package com.limpygnome.parrot.component.remote;

import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.database.EncryptedValueService;
import com.limpygnome.parrot.component.file.FileComponent;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.db.DatabaseMerger;
import com.limpygnome.parrot.library.db.DatabaseNode;
import com.limpygnome.parrot.library.dbaction.ActionLog;
import com.limpygnome.parrot.library.io.DatabaseReaderWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.InvalidCipherTextException;
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
        return new SshOptions(randomToken, name, host, port, user, remotePath, destinationPath);
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
    public String download(SshOptions options)
    {
        String result = null;
        SshSession session = null;

        try
        {
            // Check destination path
            result = checkDestinationPath(options);

            if (result == null)
            {
                // Connect
                session = sshComponent.connect(options);

                // Start download...
                sshComponent.download(session, options);
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
            if (session != null)
            {
                session.dispose();
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
    public String test(SshOptions options)
    {
        String result = null;
        SshSession session = null;

        try
        {
            // Check destination file first, fail fast...
            result = checkDestinationPath(options);

            if (result == null)
            {
                // Connect
                session = sshComponent.connect(options);

                // Check remote connection works and file exists
                if (!sshComponent.checkRemotePathExists(options, session))
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
            if (session != null)
            {
                session.dispose();
            }
        }

        return result;
    }

    public String sync(Database database, SshOptions options, String remotePassword)
    {
        if (database == null)
        {
            throw new IllegalArgumentException("Database is null");
        }
        else if (options == null)
        {
            throw new IllegalArgumentException("Options are null");
        }
        else if (remotePassword == null || remotePassword.length() == 0)
        {
            return "Remote password is required";
        }
        else if (options.getDestinationPath() == null)
        {
            throw new IllegalArgumentException("Internal error - destination path must be setup on options");
        }

        // Alter destination path for this host
        int fullHostNameHash = (options.getHost() + options.getPort()).hashCode();
        String syncPath = options.getDestinationPath();
        syncPath = syncPath + "." + fullHostNameHash + "." + System.currentTimeMillis() + ".sync";

        // Begin sync process...
        String result = null;
        SshSession session = null;

        try
        {
            // Check destination path
            result = checkDestinationPath(options);

            if (result == null)
            {
                // Connect
                LOG.info("sync - connecting");
                session = sshComponent.connect(options);

                // Start download...
                LOG.info("sync - downloading");
                boolean exists = sshComponent.download(session, options, syncPath);

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
                        sshComponent.upload(session, options, options.getDestinationPath());
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
                    sshComponent.upload(session, options, currentPath);

                    actionLog.add("uploaded successfully");
                }

                // Build result
                result = actionLog.getMessages();
            }
        }
        catch (InvalidCipherTextException e)
        {
            result = "Incorrect password or corrupted file";
            LOG.error("Failed to open remote database due to invalid crypto (wrong password / corrupted)", e);
        }
        catch (Exception e)
        {
            result = sshComponent.getExceptionMessage(e);
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
            if (session != null)
            {
                session.dispose();
            }
        }

        return result;
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

}
