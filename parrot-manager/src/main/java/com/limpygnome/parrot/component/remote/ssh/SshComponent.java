package com.limpygnome.parrot.component.remote.ssh;

import com.jcraft.jsch.*;
import com.limpygnome.parrot.component.file.FileComponent;
import com.limpygnome.parrot.component.remote.SyncFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOG = LoggerFactory.getLogger(SshComponent.class);

    private static final int CONNECTION_TIMEOUT = 10000;

    @Autowired
    private FileComponent fileComponent;

    public SshSession connect(SshOptions options) throws JSchException
    {
        LOG.info("opening connection - options: {}", options);

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

    public String download(SshSession sshSession, SshFile source, String destination) throws SftpException, SyncFailureException
    {
        String result = null;

        ChannelSftp channelSftp = sshSession.getChannelSftp();

        // Ensure path fully resolved
        destination = fileComponent.resolvePath(destination);

        // Check file exists
        if (checkRemotePathExists(sshSession, source))
        {
            LOG.info("downloading file - full path: {}", source.getFullPath());

            // Start the download...
            changeRemoteDirectory(channelSftp, source);
            channelSftp.get(source.getFullPath(), destination);
        }
        else
        {
            result = "Remote file does not exist - path: " + source.getFullPath();
        }

        return result;
    }

    public void upload(SshSession sshSession, String source, SshFile destination) throws SftpException
    {
        ChannelSftp channelSftp = sshSession.getChannelSftp();

        // Resolve full path
        source = fileComponent.resolvePath(source);

        // Upload file
        channelSftp.put(source, destination.getFullPath());
    }

    public void rename(SshSession sshSession, SshFile source, SshFile destination) throws SftpException
    {
        ChannelSftp channelSftp = sshSession.getChannelSftp();
        channelSftp.rename(source.getFullPath(), destination.getFullPath());

    }

    public void remove(SshSession sshSession, SshFile file) throws SftpException, SyncFailureException
    {
        ChannelSftp channelSftp = sshSession.getChannelSftp();

        // change directory
        changeRemoteDirectory(channelSftp, file);

        // remove file
        channelSftp.rm(file.getFullPath());
    }

    public boolean checkRemotePathExists(SshSession session, SshFile sshFile) throws SftpException, SyncFailureException
    {
        ChannelSftp channelSftp = session.getChannelSftp();

        // Change to the remote path
        changeRemoteDirectory(channelSftp, sshFile);

        boolean exists;

        try
        {
            exists = !channelSftp.ls(sshFile.getFullPath()).isEmpty();
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
    // TODO remove
    private void changeRemoteDirectory(ChannelSftp channelSftp, SshFile sshFile) throws SyncFailureException
    {
//        String directory = sshFile.getDirectory();
//
//        if (directory != null && !directory.isEmpty())
//        {
//            try
//            {
//                channelSftp.cd(directory);
//            }
//            catch (SftpException e)
//            {
//                throw new SyncFailureException("Remote path does not exist - " + directory);
//            }
//        }
    }

}
