package com.limpygnome.parrot.component.remote.ssh;

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
        if (channelSftp != null)
        {
            channelSftp.disconnect();
        }

        if (session != null)
        {
            session.disconnect();
        }
    }

}
