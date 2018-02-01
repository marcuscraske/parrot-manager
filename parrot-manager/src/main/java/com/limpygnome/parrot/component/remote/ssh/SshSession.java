package com.limpygnome.parrot.component.remote.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Created by limpygnome on 09/02/17.
 */
public class SshSession
{
    public static final int CONNECTION_TIMEOUT = 10000;

    private Session session;
    private ChannelSftp channelSftp;
    private ChannelExec channelExec;

    public SshSession(Session session)
    {
        this.session = session;
    }

    public Session getSession()
    {
        return session;
    }

    public synchronized ChannelSftp getChannelSftp() throws JSchException
    {
        if (channelSftp == null)
        {
            // Start sftp in session
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect(CONNECTION_TIMEOUT);
        }
        return channelSftp;
    }

    public synchronized ChannelExec getChannelExec() throws JSchException
    {
        if (channelExec == null)
        {
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.connect(CONNECTION_TIMEOUT);
        }
        return channelExec;
    }

    public void dispose()
    {
        if (channelSftp != null)
        {
            channelSftp.disconnect();
        }

        if (channelExec != null)
        {
            channelExec.disconnect();
        }

        if (session != null)
        {
            session.disconnect();
        }
    }

}
