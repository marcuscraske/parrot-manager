package com.limpygnome.parrot.component;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.limpygnome.parrot.model.remote.FileStatus;
import com.limpygnome.parrot.model.remote.SshOptions;
import com.limpygnome.parrot.model.remote.SshSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Properties;

/**
 * Common functionality and wrapper around using SSH.
 *
 * TODO: unit test
 */
public class SshComponent
{
    private static final Logger LOG = LogManager.getLogger(SshComponent.class);

    private FileComponent fileComponent;

    public SshComponent()
    {
        this.fileComponent = new FileComponent();
    }

    public SshSession connect(SshOptions options) throws JSchException
    {
        LOG.info("transfer - {} - opening connection - options: {}", options.getRandomToken(), options);

        JSch jsch = new JSch();

        // Setup key
        if (options.isPrivateKey())
        {
            try
            {
                jsch.addIdentity(options.getPrivateKeyPath(), options.getPrivateKeyPass());
            }
            catch (JSchException e)
            {
                if (e.getCause() instanceof FileNotFoundException)
                {
                    throw new RuntimeException("Private key not found");
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

        session.connect();

        // Start sftp in session
        Channel channel = session.openChannel("sftp");
        channel.connect();

        ChannelSftp channelSftp = (ChannelSftp) channel;

        // Create agnostic wrapper
        SshSession sshSession = new SshSession(session, channelSftp);
        return sshSession;
    }

    public void download(SshSession sshSession, Map<String, FileStatus> fileStatusMap, SshOptions options) throws SftpException
    {
        ChannelSftp channelSftp = sshSession.getChannelSftp();

        final String randomToken = options.getRandomToken();
        String destinationPath = options.getDestinationPath();
        String remotePath = options.getRemotePath();

        try
        {
            // Ensure paths are fully resolved
            destinationPath = fileComponent.resolvePath(destinationPath);
            remotePath = resolveRemotePath(channelSftp, randomToken, remotePath);

            // Move to containing directory
            changeRemoteDirectoryIfNeeded(channelSftp, randomToken, remotePath);

            // Start the transfer...
            String remoteFileName = getFileNameFromRemotePath(remotePath);
            LOG.info("transfer - {} - initiating transfer - fileName: {}", randomToken, remoteFileName);

            channelSftp.get(remoteFileName, destinationPath, new SftpProgressMonitor()
            {
                @Override
                public void init(int op, String src, String dest, long maxBytes)
                {
                    LOG.info("transfer - {} - init - max bytes: {}", randomToken, maxBytes);

                    synchronized (fileStatusMap)
                    {
                        FileStatus status = fileStatusMap.get(randomToken);
                        status.setMax(maxBytes);
                    }
                }

                @Override
                public boolean count(long bytes)
                {
                    LOG.info("transfer - {} - progress - bytes: {}", randomToken, bytes);

                    synchronized (fileStatusMap)
                    {
                        FileStatus status = fileStatusMap.get(randomToken);
                        status.setCurrent(bytes);
                    }

                    return true;
                }

                @Override
                public void end()
                {
                    LOG.info("transfer - finished");
                }
            });
        }
        finally
        {
            // Remove progress
            if (randomToken != null)
            {
                synchronized (fileStatusMap)
                {
                    fileStatusMap.remove(randomToken);
                }
            }
        }
    }

    public boolean checkRemotePathExists(SshOptions options, SshSession session) throws SftpException
    {
        ChannelSftp channelSftp = session.getChannelSftp();

        // Resolve path
        String remotePath = options.getRemotePath();
        remotePath = resolveRemotePath(channelSftp, null, remotePath);

        // Change to the remote path
        changeRemoteDirectoryIfNeeded(channelSftp, null, remotePath);

        // Check file exists
        String remoteFileName = getFileNameFromRemotePath(remotePath);

        boolean exists = !channelSftp.ls(remotePath).isEmpty();
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
                } else if ("No such file".equals(message))
                {
                    message = "Remote file could not be found";
                }
            }
        }
        else
        {
            message = null;
        }

        return message;
    }

    /* Common path short-hands are translated */
    private String resolveRemotePath(ChannelSftp channelSftp, String randomToken, String remotePath) throws SftpException
    {
        // Replace ~/ with home directory
        if (remotePath.startsWith("~/") && remotePath.length() > 2)
        {
            String home = channelSftp.getHome();
            remotePath = home + "/" + remotePath.substring(2);
            LOG.info("transfer - {} - replacing ~/ with home directory - new remote path: {}", randomToken, remotePath);
        }

        return remotePath;
    }

    /* Changes the current remote directory to the parent of the remote path (if it has a parent) */
    private void changeRemoteDirectoryIfNeeded(ChannelSftp channelSftp, String randomToken, String remotePath) throws SftpException
    {
        // Move to containing directory
        File remoteFile = new File(remotePath);
        File remoteFileParent = remoteFile.getParentFile();

        if (remoteFileParent != null)
        {
            String remoteFileParentPath = remoteFileParent.getAbsolutePath();

            LOG.info("transfer - {} - changing directory - path: {}", randomToken, remoteFileParentPath);
            channelSftp.cd(remoteFileParentPath);
        }
    }

    private String getFileNameFromRemotePath(String remotePath)
    {
        File remoteFile = new File(remotePath);
        return remoteFile.getName();
    }

}
