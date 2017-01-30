package com.limpygnome.parrot.service.rest;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;
import com.limpygnome.parrot.model.remote.FileStatus;
import java.io.File;
import java.util.Map;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A service for downloading and uploading remote files using SSH.
 *
 * TODO: unit test
 */
public class RemoteSshFileService
{
    private static final Logger LOG = LogManager.getLogger(RemoteSshFileService.class);

    private Map<String, Long>

    public static void main(String[] args)
    {
        RemoteSshFileService s = new RemoteSshFileService();
        s.download("xxx", "localhost", 22, "limpygnome", null, null, "~/test2.html", "~/dest-test.html");
    }

    public FileStatus getDownloadBytes(String randomToken)
    {

    }

    /**
     * Begins downloading a file from an SSH host.
     *
     * Invocation is synchronous, but the status of a file can be retrieved using {@link #getDownloadStatus}, using the
     * provided token.
     *
     * @param randomToken
     * @param host
     * @param port
     * @param user
     * @param pass
     * @param keyPath
     * @param destinationPath
     * @return error message, otherwise null if successful
     */
    public String download(String randomToken, String host, int port, String user, String pass, String keyPath, String remotePath, String destinationPath)
    {
        // Replace destination path ~/ with home directory
        if (destinationPath.startsWith("~/") && destinationPath.length() > 2)
        {
            // TODO: test on windows, could be flawed...
            String homeDirectory = System.getProperty("user.home");
            String pathSeparator = System.getProperty("file.separator");

            destinationPath = homeDirectory + pathSeparator + destinationPath.substring(2);
        }

        LOG.info("transfer - starting - user: {}, port: {}, remote path: {}, dest path: {}", user, port, remotePath, destinationPath);

        Session session = null;
        ChannelSftp channelSftp = null;

        try
        {
            JSch jsch = new JSch();
//            jsch.addIdentity("~/.ssh/id_rsa");

            Properties properties = new Properties();
            // TODO: consider if we should do this...
            properties.put("StrictHostKeyChecking", "no");

            session = jsch.getSession(user, host, port);
            session.setPassword("test123");
            session.setConfig(properties);
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();

            channelSftp = (ChannelSftp) channel;

            // Replace ~/ with home directory
            if (remotePath.startsWith("~/") && remotePath.length() > 2)
            {
                String home = channelSftp.getHome();
                remotePath = home + "/" + remotePath.substring(2);
                LOG.info("transfer - replacing ~/ with home directory - new remote path: {}", remotePath);
            }

            // Move to containing directory
            File remoteFile = new File(remotePath);
            File remoteFileParent = remoteFile.getParentFile();

            if (remoteFileParent != null)
            {
                String remoteFileParentPath = remoteFileParent.getAbsolutePath();

                LOG.info("transfer - changing directory - path: {}", remoteFileParentPath);
                channelSftp.cd(remoteFileParentPath);
            }

            String remoteFileName = remoteFile.getName();
            LOG.info("transfer - initiating transfer - fileName: {}", remoteFileName);

            channelSftp.get(remoteFileName, destinationPath, new SftpProgressMonitor()
            {
                @Override
                public void init(int op, String src, String dest, long maxBytes)
                {
                    LOG.info("transfer - start - max bytes: {}", maxBytes);
                }

                @Override
                public boolean count(long bytes)
                {
                    LOG.info("transfer - progress - bytes: {}", bytes);
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
            LOG.error("transfer - failed", e);
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
        }

        return null;
    }

    public String upload(String host, int port, String user, String pass, String keyPath, String destinationPath)
    {
        return null;
    }

}
