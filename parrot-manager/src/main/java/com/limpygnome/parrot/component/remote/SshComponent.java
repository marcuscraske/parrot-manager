package com.limpygnome.parrot.component.remote;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.limpygnome.parrot.component.file.FileComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

/**
 * Common functionality and wrapper around using SSH.
 */
@Component
public class SshComponent
{
    private static final Logger LOG = LogManager.getLogger(SshComponent.class);

    private static final int CONNECTION_TIMEOUT = 10000;

    @Autowired
    private FileComponent fileComponent;

    public SshSession connect(SshOptions options) throws JSchException
    {
        LOG.info("transfer - {} - opening connection - options: {}", options.getRandomToken(), options);

        JSch jsch = new JSch();

        // Setup key
        if (options.isPrivateKey())
        {
            try
            {
                // resolve ssh key
                String path = fileComponent.resolvePath(options.getPrivateKeyPath());
                File privateKeyFile = new File(path);

                if (!privateKeyFile.exists())
                {
                    throw new RuntimeException("Private key file does not exist - path: " + path);
                }
                else if (!privateKeyFile.canRead())
                {
                    throw new RuntimeException("Private key file cannot be read - path: " + path);
                }

                jsch.addIdentity(path, options.getPrivateKeyPass());
            }
            catch (JSchException e)
            {
                String message = e.getMessage();
                Throwable cause = e.getCause();

                if (message != null)
                {
                    if (message.toLowerCase().contains("invalid privatekey"))
                    {
                        throw new RuntimeException("Invalid private key - did you select the public key by accident?");
                    }
                }

                if (cause != null)
                {
                    if (cause instanceof FileNotFoundException)
                    {
                        throw new RuntimeException("Private key not found");
                    }
                }

                throw e;
            }
        }

        // Disable strict host checking (if not enabled)
        Properties properties = new Properties();

        if (!options.isStrictHostChecking())
        {
            properties.put("StrictHostKeyChecking", "no");
        }

        // Connect to host...
        Session session = jsch.getSession(options.getUser(), options.getHost(), options.getPort());
        session.setPassword(options.getUserPass());
        session.setConfig(properties);
        session.setProxy(options.getProxy());

        try
        {
            session.connect(CONNECTION_TIMEOUT);
        }
        catch (JSchException e)
        {
            String message = e.getMessage();

            if (message.toLowerCase().contains("socket is not established"))
            {
                throw new RuntimeException("Unable to connect to host '" + options.getName() + "'");
            }

            throw e;
        }

        // Start sftp in session
        Channel channel = session.openChannel("sftp");
        channel.connect(CONNECTION_TIMEOUT);

        ChannelSftp channelSftp = (ChannelSftp) channel;

        // Create agnostic wrapper
        SshSession sshSession = new SshSession(session, channelSftp);
        return sshSession;
    }

    public void download(SshSession sshSession, SshOptions options, String alternativeFileName) throws SftpException, SyncFailureException
    {
        download(sshSession, options, options.getDestinationPath(), alternativeFileName);
    }

    public boolean download(SshSession sshSession, SshOptions options, String destinationPath, String alternativeFileName) throws SftpException, SyncFailureException
    {
        boolean result;

        ChannelSftp channelSftp = sshSession.getChannelSftp();

        final String randomToken = options.getRandomToken();
        String remotePath = options.getRemotePath();

        // Ensure paths are fully resolved
        remotePath = resolveRemotePath(channelSftp, remotePath, alternativeFileName);
        destinationPath = fileComponent.resolvePath(destinationPath);

        // Move to containing directory
        changeRemoteDirectoryIfNeeded(channelSftp, remotePath);

        // Check file exists
        if (checkRemotePathExists(options, sshSession, alternativeFileName))
        {
            // Start the transfer...
            String remoteFileName = getFileNameFromRemotePath(remotePath);
            LOG.info("transfer - {} - initiating transfer - fileName: {}", randomToken, remoteFileName);

            channelSftp.get(remoteFileName, destinationPath);
            result = true;
        }
        else
        {
            result = false;
        }

        return result;
    }

    public void upload(SshSession sshSession, SshOptions options, String srcPath, String alternativeFileName) throws SftpException
    {
        ChannelSftp channelSftp = sshSession.getChannelSftp();

        // Resolve remote path
        String remotePath = resolveRemotePath(channelSftp, options.getRemotePath(), alternativeFileName);

        // Upload file
        channelSftp.put(srcPath, remotePath);
    }

    public void remove(SshSession sshSession, SshOptions options, String alternativeFileName) throws SftpException, SyncFailureException
    {
        ChannelSftp channelSftp = sshSession.getChannelSftp();

        // alter file name if provided
        String remotePath = resolveRemotePath(channelSftp, options.getRemotePath(), alternativeFileName);

        // change directory
        changeRemoteDirectoryIfNeeded(channelSftp, remotePath);

        // remove file
        channelSftp.rm(remotePath);
    }

    public boolean checkRemotePathExists(SshOptions options, SshSession session, String alternativeFileName) throws SftpException, SyncFailureException
    {
        ChannelSftp channelSftp = session.getChannelSftp();

        // Resolve path
        String remotePath = options.getRemotePath();
        remotePath = resolveRemotePath(channelSftp, remotePath, alternativeFileName);

        // Change to the remote path
        changeRemoteDirectoryIfNeeded(channelSftp, remotePath);

        boolean exists;

        try
        {
            exists = !channelSftp.ls(remotePath).isEmpty();
        }
        catch (SftpException e)
        {
            LOG.debug("Remote database file does not exist?", e);
            exists = false;
        }

        return exists;
    }

    /**
     * Translates an exception into a more friendly message.
     *
     * @param e the exception; can be null
     * @return the translate message; null if exception is null
     */
    public String getExceptionMessage(Exception e)
    {
        String message;

        if (e != null)
        {
            message = e.getMessage();

            // Improved error messages
            if (e instanceof JSchException)
            {
                if ("Auth fail".equals(message))
                {
                    message = "Auth failed - unable to connect using specified credentials";
                }
                else if ("No such file".equals(message))
                {
                    message = "Remote file could not be found";
                }
            }
        }
        else
        {
            message = e.getMessage();
        }

        return message;
    }


    /* Changes the current remote directory to the parent of the remote path (if it has a parent) */
    private void changeRemoteDirectoryIfNeeded(ChannelSftp channelSftp, String remotePath) throws SyncFailureException
    {
        // Move to containing directory
        int lastSlash = remotePath.lastIndexOf("/");

        if (lastSlash != -1 && remotePath.length() > lastSlash + 1);
        {
            String parentPath = remotePath.substring(0, lastSlash);

            try
            {
                channelSftp.cd(parentPath);
            }
            catch (SftpException e)
            {
                throw new SyncFailureException("Remote path does not exist - " + parentPath);
            }
        }
    }

    /* Common path short-hands are translated */
    private String resolveRemotePath(ChannelSftp channelSftp, String remotePath, String alternativeFileName) throws SftpException
    {
        // replace ~/ with home directory
        if (remotePath.startsWith("~/") && remotePath.length() > 2)
        {
            String home = channelSftp.getHome();
            remotePath = home + "/" + remotePath.substring(2);
            LOG.info("transfer - replacing ~/ with home directory - new remote path: {}", remotePath);
        }

        // replace file-name if alternative file-name provided
        if (alternativeFileName != null)
        {
            remotePath = rewriteRemotePathFileName(remotePath, alternativeFileName);
        }

        return remotePath;
    }

    /* Takes the remote path and changes the file-name; useful for placing files in the root of remote databases. */
    private String rewriteRemotePathFileName(String remotePath, String fileName)
    {
        String root = "";

        // extract parent/root path of remote path
        int lastIndex = remotePath.lastIndexOf("/");

        if (lastIndex == 0)
        {
            root = "/";
        }
        else if (lastIndex > 0 && lastIndex < remotePath.length() - 1)
        {
            root = remotePath.substring(0, lastIndex);
        }

        // add file name to end
        root += "/" + fileName;

        return root;
    }

    private String getFileNameFromRemotePath(String remotePath)
    {
        File remoteFile = new File(remotePath);
        return remoteFile.getName();
    }

}
