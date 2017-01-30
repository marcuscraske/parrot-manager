package com.limpygnome.parrot.service.rest;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;
import java.io.File;
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

    public static void main(String[] args)
    {
        RemoteSshFileService s = new RemoteSshFileService();
        s.download("localhost", 22, "limpygnome", null, null, "~/test.html", "~/dest-test.html");
    }

    /**
     * Begins downloading a file from an SSH host.
     *
     * @param host
     * @param port
     * @param user
     * @param pass
     * @param keyPath
     * @param destinationPath
     * @return error message, otherwise null if successful
     */
    public String download(String host, int port, String user, String pass, String keyPath, String remotePath, String destinationPath)
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

        try
        {
            JSch jsch = new JSch();
//            jsch.addIdentity("~/.ssh/id_rsa");

            Properties properties = new Properties();
            // TODO: consider if we should do this...
            properties.put("StrictHostKeyChecking", "no");

            Session session = jsch.getSession(user, host, port);
            session.setPassword("test123");
            session.setConfig(properties);
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();

            ChannelSftp channelSftp = (ChannelSftp) channel;

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

            channelSftp.disconnect();
            session.disconnect();
        }
        catch (Exception e)
        {
            LOG.error("transfer - failed", e);
        }

        return null;
    }

    public String upload(String host, int port, String user, String pass, String keyPath, String destinationPath)
    {
        return null;
    }

}
