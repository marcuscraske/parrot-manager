package com.limpygnome.parrot.component.remote.ssh;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

import java.io.File;

public class SshFile
{
    private String directory;
    private String fileName;

    public SshFile(SshSession sshSession, String remotePath) throws SftpException
    {
        // fully resolve remote path
        ChannelSftp channelSftp = sshSession.getChannelSftp();

        // replace ~/ with home directory
        if (remotePath.startsWith("~/") && remotePath.length() > 2)
        {
            String home = channelSftp.getHome();
            remotePath = home + "/" + remotePath.substring(2);
        }

        // extract dir and filename
        File remoteFile = new File(remotePath);

        this.directory = remoteFile.getParent();
        this.fileName = remoteFile.getName();
    }

    private SshFile(String directory, String fileName)
    {
        this.directory = directory;
        this.fileName = fileName;
    }

    public String getDirectory()
    {
        return directory;
    }

    public void setDirectory(String directory)
    {
        this.directory = directory;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public SshFile postFixFileName(String postFix)
    {
        this.fileName += postFix;
        return this;
    }

    public SshFile clone()
    {
        return new SshFile(directory, fileName);
    }

    public String getFullPath()
    {
        return directory + "/" + fileName;
    }

}
