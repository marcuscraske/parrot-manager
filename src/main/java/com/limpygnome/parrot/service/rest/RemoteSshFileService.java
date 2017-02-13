package com.limpygnome.parrot.service.rest;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.component.FileComponent;
import com.limpygnome.parrot.component.SshComponent;
import com.limpygnome.parrot.model.db.Database;
import com.limpygnome.parrot.model.db.DatabaseNode;
import com.limpygnome.parrot.model.dbaction.ActionsLog;
import com.limpygnome.parrot.model.remote.FileStatus;
import com.limpygnome.parrot.model.remote.SshOptions;
import com.limpygnome.parrot.model.remote.SshSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A service for downloading and uploading remote files using SSH.
 *
 * TODO: unit test
 */
public class RemoteSshFileService
{
    private static final Logger LOG = LogManager.getLogger(RemoteSshFileService.class);

    private Controller controller;

    private FileComponent fileComponent;
    private SshComponent sshComponent;

    private Map<String, FileStatus> fileStatusMap;

    public RemoteSshFileService(Controller controller)
    {
        this.controller = controller;

        this.fileComponent = new FileComponent();
        this.sshComponent = new SshComponent();
        this.fileStatusMap = new HashMap<>();
    }

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
        SshOptions options = SshOptions.read(node);
        return options;
    }

    /**
     * Retrieves the status of a file download/upload.
     *
     * @param randomToken the same token used to initiate a download/upload
     * @return the status, or null if not found / transfer has ended
     */
    public FileStatus getStatus(String randomToken)
    {
        FileStatus fileStatus;

        synchronized (fileStatusMap)
        {
            fileStatus = fileStatusMap.get(randomToken);
        }

        return fileStatus;
    }

    /**
     * Begins downloading a file from an SSH host.
     *
     * Invocation is synchronous, but the status of a file can be retrieved using {@link #getStatus}, using the
     * provided token.
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
                sshComponent.download(session, fileStatusMap, options);
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
                    result = "Remote file does not exist";
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
        // TODO: perhaps we should use DB service instead...
        if (options.getDestinationPath() == null)
        {
            throw new IllegalArgumentException("Internal error - destination path must be setup on options");
        }

        // Alter destination path for this host
        // TODO: will need to fix this for async merging...
        String syncPath = options.getDestinationPath();
        syncPath = syncPath + ".sync";

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
                sshComponent.download(session, fileStatusMap, options, syncPath);

                // Load remote database
                LOG.info("sync - loading remote database");
                Database remoteDatabase = controller.getDatabaseIOService().open(controller, syncPath, remotePassword.toCharArray());

                // Perform merge and check if any change occurred...
                LOG.info("sync - performing merge...");
                ActionsLog actionsLog = controller.getDatabaseIOService().merge(remoteDatabase, database, remotePassword.toCharArray());

                // Check if we need to upload...
                if (database.isDirty() || remoteDatabase.isDirty())
                {
                    // Save current database
                    LOG.info("sync - database(s) dirty, saving... - local: {}, remote: {}", database.isDirty(), remoteDatabase.isDirty());
                    controller.getDatabaseIOService().save(controller, database, options.getDestinationPath());

                    // Upload to remote
                    LOG.info("sync - uploading to remote host...");
                    sshComponent.upload(session, options, options.getDestinationPath());
                }
                else
                {
                    LOG.info("sync - neither database is dirty");
                }

                // TODO: return actual log, this is just to get things going for now; prolly want actual async callbacks
                result = "merged";
            }
        }
        catch (Exception e)
        {
            result = sshComponent.getExceptionMessage(e);
            LOG.error("sync - {} - exception", options.getRandomToken(), e);
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
