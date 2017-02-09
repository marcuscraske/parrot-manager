package com.limpygnome.parrot.model.remote;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

/**
 * Created by limpygnome on 09/02/17.
 */
public class SshSession
{
    private Session session;
    private ChannelSftp channelSftp;

    public SshSession(Session session, ChannelSftp channelSftp)
    {
        this.session = session;
        this.channelSftp = channelSftp;
    }

    public Session getSession()
    {
        return session;
    }

    public ChannelSftp getChannelSftp()
    {
        return channelSftp;
    }

    public void dispose()
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

}
