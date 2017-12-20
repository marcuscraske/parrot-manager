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

        session.connect(CONNECTION_TIMEOUT);

        // Start sftp in session
        Channel channel = session.openChannel("sftp");
        channel.connect(CONNECTION_TIMEOUT);

        ChannelSftp channelSftp = (ChannelSftp) channel;

        // Create agnostic wrapper
        SshSession sshSession = new SshSession(session, channelSftp);
        return sshSession;
    }

    public void download(SshSession sshSession, SshOptions options) throws SftpException
    {
        download(sshSession, options, options.getDestinationPath());
    }

    public void upload(SshSession sshSession, SshOptions options, String srcPath) throws SftpException
    {
        ChannelSftp channelSftp = sshSession.getChannelSftp();

        // Resolve remote path
        String remotePath = resolveRemotePath(channelSftp, options.getRemotePath());

        // Upload file
        channelSftp.put(srcPath, remotePath);
    }

    /**
     * Downloads remote database.
     *
     * @param sshSession
     * @param options
     * @param destinationPath
     * @return true = success, false = file does not exist
     * @throws SftpException if connection or I/O issue
     */
    public boolean download(SshSession sshSession, SshOptions options, String destinationPath) throws SftpException
    {
        boolean result;

        ChannelSftp channelSftp = sshSession.getChannelSftp();

        final String randomToken = options.getRandomToken();
        String remotePath = options.getRemotePath();

        // Ensure paths are fully resolved
        remotePath = resolveRemotePath(channelSftp, remotePath);
        destinationPath = fileComponent.resolvePath(destinationPath);

        // Move to containing directory
        changeRemoteDirectoryIfNeeded(channelSftp, remotePath);

        // Check file exists
        if (checkRemotePathExists(options, sshSession))
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

    public boolean checkRemotePathExists(SshOptions options, SshSession session) throws SftpException
    {
        ChannelSftp channelSftp = session.getChannelSftp();

        // Resolve path
        String remotePath = options.getRemotePath();
        remotePath = resolveRemotePath(channelSftp, remotePath);

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

    /* Common path short-hands are translated */
    private String resolveRemotePath(ChannelSftp channelSftp, String remotePath) throws SftpException
    {
        // Replace ~/ with home directory
        if (remotePath.startsWith("~/") && remotePath.length() > 2)
        {
            String home = channelSftp.getHome();
            remotePath = home + "/" + remotePath.substring(2);
            LOG.info("transfer - replacing ~/ with home directory - new remote path: {}", remotePath);
        }

        return remotePath;
    }

    /* Changes the current remote directory to the parent of the remote path (if it has a parent) */
    private void changeRemoteDirectoryIfNeeded(ChannelSftp channelSftp, String remotePath)
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
                throw new RuntimeException("Remote path does not exist - " + parentPath);
            }
        }
    }

    private String getFileNameFromRemotePath(String remotePath)
    {
        File remoteFile = new File(remotePath);
        return remoteFile.getName();
    }

}
