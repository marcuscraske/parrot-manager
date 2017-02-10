package com.limpygnome.parrot.service.rest;

import com.limpygnome.parrot.component.FileComponent;
import com.limpygnome.parrot.component.SshComponent;
import com.limpygnome.parrot.model.db.DatabaseNode;
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

    private FileComponent fileComponent;
    private SshComponent sshComponent;

    private Map<String, FileStatus> fileStatusMap;

    public RemoteSshFileService()
    {
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

    public String merge(SshOptions options)
    {
        return null;
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
