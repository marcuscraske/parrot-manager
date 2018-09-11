package com.limpygnome.parrot.component.sync;

import com.limpygnome.parrot.component.sync.ssh.SshSyncHandler;
import com.limpygnome.parrot.component.ui.WebStageInitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages executing sync-related threads.
 */
@Service
public class SyncThreadService
{
    private static final Logger LOG = LoggerFactory.getLogger(SyncThreadService.class);

    @Autowired
    private SshSyncHandler sshSyncHandler;
    @Autowired
    private SyncResultService resultService;
    @Autowired
    private WebStageInitService webStageInitService;

    // State
    private boolean aborted;
    private Thread thread;

    public SyncThreadService()
    {
        aborted = false;
        thread = null;
    }

    /**
     * Launches new thread.
     *
     * If anything is running, the request is ignored.
     *
     * @param syncThread thread to be executed
     * @param profileArray sync profiles
     */
    public synchronized void launchAsync(SyncThread syncThread, SyncOptions options, SyncProfile... profileArray)
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
                    execute(syncThread, options, profileArray);
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

    private void execute(SyncThread syncThread, SyncOptions options, SyncProfile... profileArray)
    {
        // Reset results
        resultService.clear();

        for (int i = 0; !aborted && i < profileArray.length; i++)
        {
            SyncProfile profile = profileArray[i];
            executeForHost(syncThread, options, profile);
        }
    }

    private void executeForHost(SyncThread syncThread, SyncOptions options, SyncProfile profile)
    {
        // raise event for work started
        webStageInitService.triggerEvent("document", "syncStart", profile);

        SyncResult syncResult = null;

        try
        {
            // execute work
            syncResult = syncThread.execute(options, profile);

            // add to results
            resultService.add(syncResult);
        }
        finally
        {
            // raise event for work finished
            webStageInitService.triggerEvent("document", "syncFinish", syncResult);
        }
    }

}
