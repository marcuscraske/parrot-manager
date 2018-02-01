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
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * Common functionality and wrapper around using SSH.
 */
@Component
public class SshComponent
{
    private static final Logger LOG = LoggerFactory.getLogger(SshComponent.class);

    @Autowired
    private FileComponent fileComponent;

    public SshSession connect(SshOptions options) throws JSchException
    {
        LOG.info("opening connection - options: {}", options);

        // Setup session
        Session session = setupSession(options);

        // Create agnostic wrapper
        SshSession sshSession = new SshSession(session);
        return sshSession;
    }

    public String download(SshSession sshSession, SshFile source, String destination) throws JSchException, SftpException, SyncFailureException
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
            channelSftp.get(source.getFullPath(), destination);
        }
        else
        {
            result = "Remote file does not exist - path: " + source.getFullPath();
        }

        return result;
    }

    public void upload(SshSession sshSession, String source, SshFile destination) throws JSchException, SftpException
    {
        ChannelSftp channelSftp = sshSession.getChannelSftp();

        // Resolve full path
        source = fileComponent.resolvePath(source);

        // Upload file
        channelSftp.put(source, destination.getFullPath());
    }

    public void rename(SshSession sshSession, SshFile source, SshFile destination) throws JSchException, SftpException
    {
        ChannelSftp channelSftp = sshSession.getChannelSftp();
        channelSftp.rename(source.getFullPath(), destination.getFullPath());
    }

    public void remove(SshSession sshSession, SshFile file) throws JSchException, SftpException
    {
        ChannelSftp channelSftp = sshSession.getChannelSftp();
        channelSftp.rm(file.getFullPath());
    }

    public List<SshFile> list(SshSession sshSession, SshFile parent) throws JSchException, SftpException
    {
        ChannelSftp channelSftp = sshSession.getChannelSftp();

        String directory = parent.getFullPath();
        Vector<ChannelSftp.LsEntry> entries = channelSftp.ls(directory);

        List<SshFile> files = entries.stream()
                .map(lsEntry -> {
                    try
                    {
                        return new SshFile(sshSession, parent, lsEntry.getLongname());
                    }
                    catch (SftpException | JSchException e)
                    {
                        return null;
                    }

                })
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        return files;
    }

    public boolean checkRemotePathExists(SshSession session, SshFile sshFile) throws JSchException, SftpException, SyncFailureException
    {
        ChannelSftp channelSftp = session.getChannelSftp();

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

    private Session setupSession(SshOptions options) throws JSchException
    {
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
            session.connect(SshSession.CONNECTION_TIMEOUT);
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

        return session;
    }

}
