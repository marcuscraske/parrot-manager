package com.limpygnome.parrot.service.rest;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;
import com.limpygnome.parrot.model.remote.DownloadSshOptions;
import com.limpygnome.parrot.model.remote.FileStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A service for downloading and uploading remote files using SSH.
 *
 * TODO: unit test
 */
public class RemoteSshFileService
{
    private static final Logger LOG = LogManager.getLogger(RemoteSshFileService.class);

    private Map<String, FileStatus> fileStatusMap;

    public RemoteSshFileService()
    {
        this.fileStatusMap = new HashMap<>();
    }

    public DownloadSshOptions createDownloadOptions(String randomToken, String host, int port, String user, String remotePath, String destinationPath)
    {
        return new DownloadSshOptions(randomToken, host, port, user, remotePath, destinationPath);
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
    public String download(DownloadSshOptions options)
    {
        String result = null;

        final String randomToken = options.getRandomToken();
        String destinationPath = options.getDestinationPath();
        String remotePath = options.getRemotePath();

        // Add status for transfer
        synchronized (fileStatusMap)
        {
            fileStatusMap.put(randomToken, new FileStatus());
        }

        // Replace destination path ~/ with home directory
        if (destinationPath.startsWith("~/") && destinationPath.length() > 2)
        {
            // TODO: test on windows, could be flawed...
            String homeDirectory = System.getProperty("user.home");
            String pathSeparator = System.getProperty("file.separator");

            destinationPath = homeDirectory + pathSeparator + destinationPath.substring(2);
        }

        LOG.info("transfer - {} - starting - options: {}", randomToken, options);

        Session session = null;
        ChannelSftp channelSftp = null;

        try
        {
            JSch jsch = new JSch();

            // Setup key
            if (options.isPrivateKey())
            {
                jsch.addIdentity(options.getPrivateKeyPath(), options.getPrivateKeyPass());
            }

            // Disable strict host checking (if not enabled)
            Properties properties = new Properties();

            if (!options.isStrictHostKeyChecking())
            {
                properties.put("StrictHostKeyChecking", "no");
            }

            // Connect to host...
            session = jsch.getSession(options.getUser(), options.getHost(), options.getPort());
            session.setPassword(options.getPass());
            session.setConfig(properties);
            session.setProxy(options.getProxy());

            session.connect();

            // Start sftp in session
            Channel channel = session.openChannel("sftp");
            channel.connect();

            channelSftp = (ChannelSftp) channel;

            // Replace ~/ with home directory
            if (remotePath.startsWith("~/") && remotePath.length() > 2)
            {
                String home = channelSftp.getHome();
                remotePath = home + "/" + remotePath.substring(2);
                LOG.info("transfer - {} - replacing ~/ with home directory - new remote path: {}", randomToken, remotePath);
            }

            // Move to containing directory
            File remoteFile = new File(remotePath);
            File remoteFileParent = remoteFile.getParentFile();

            if (remoteFileParent != null)
            {
                String remoteFileParentPath = remoteFileParent.getAbsolutePath();

                LOG.info("transfer - {} - changing directory - path: {}", randomToken, remoteFileParentPath);
                channelSftp.cd(remoteFileParentPath);
            }

            // Start the transfer...
            String remoteFileName = remoteFile.getName();
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
        catch (Exception e)
        {
            LOG.error("transfer - {} - failed", randomToken, e);
            result = e.getMessage();
        }
        finally
        {
            // Dispose session and sftp channel
            if (session != null)
            {
                session.disconnect();
            }

            if (channelSftp != null)
            {
                channelSftp.exit();
            }

            // Remove progress
            if (randomToken != null)
            {
                synchronized (fileStatusMap)
                {
                    fileStatusMap.remove(randomToken);
                }
            }
        }

        return result;
    }

}
