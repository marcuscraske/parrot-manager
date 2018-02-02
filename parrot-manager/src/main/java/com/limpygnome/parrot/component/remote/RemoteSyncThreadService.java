package com.limpygnome.parrot.component.remote;

import com.limpygnome.parrot.component.remote.ssh.SshOptions;
import com.limpygnome.parrot.component.ui.WebStageInitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages executing sync-related threads.
 */
@Service
public class RemoteSyncThreadService
{
    private static final Logger LOG = LoggerFactory.getLogger(RemoteSyncThreadService.class);

    @Autowired
    private SshSyncService sshSyncService;
    @Autowired
    private RemoteSyncResultService resultService;
    @Autowired
    private WebStageInitService webStageInitService;

    // State
    private boolean aborted;
    private Thread thread;

    public RemoteSyncThreadService()
    {
        aborted = false;
        thread = null;
    }

    /**
     * Launches new thread.
     *
     * If anything is running, the request is ignored.
     *
     * @param remoteSyncThread thread to be executed
     * @param optionsArray host options
     */
    public synchronized void launchAsync(RemoteSyncThread remoteSyncThread, SshOptions... optionsArray)
    {
        if (thread == null)
        {
            LOG.info("launching separate thread for sync");

            // Reset abort flag
            aborted = false;

            // Start separate thread for sync to prevent blocking
            thread = new Thread(() ->
            {
                try
                {
                    execute(remoteSyncThread, optionsArray);
                }
                finally
                {
                    thread = null;
                }
            });
            thread.start();
        }
        else
        {
            LOG.error("attempted to sync whilst sync already in progress");
        }
    }

    /**
     * Aborts current thread.
     */
    public synchronized void abort()
    {
        if (thread != null)
        {
            // set abort flag
            aborted = true;

            // interrupt thread
            thread.interrupt();
        }
    }

    private void execute(RemoteSyncThread remoteSyncThread, SshOptions... optionsArray)
    {
        // Reset results
        resultService.clear();

        for (int i = 0; !aborted && i < optionsArray.length; i++)
        {
            SshOptions options = optionsArray[i];
            executeForHost(remoteSyncThread, options);
        }
    }

    private void executeForHost(RemoteSyncThread remoteSyncThread, SshOptions options)
    {
        // raise event for work started
        webStageInitService.triggerEvent("document", "remoteSyncStart", options);

        SyncResult syncResult = null;

        try
        {
            // execute work
            syncResult = remoteSyncThread.execute(options);

            // add to results
            resultService.add(syncResult);
        }
        finally
        {
            // raise event for work finished
            webStageInitService.triggerEvent("document", "remoteSyncFinish", syncResult);
        }
    }

}
