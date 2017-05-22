package com.limpygnome.parrot.component.remote;

import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.settings.SettingsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Performs remote sync at timed intervals.
 */
@Service
public class RemoteSyncIntervalService
{
    private static final Logger LOG = LogManager.getLogger(SettingsService.class);

    @Autowired
    private SettingsService settingsService;
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private RemoteSshFileService remoteSshFileService;

    private boolean continueToExecute;
    private Thread thread;

    public RemoteSyncIntervalService()
    {
        this.thread = null;
    }

    /**
     * Should be invoked when the state of the current database changes.
     */
    public void refresh()
    {
        // Wait for existing thread to terminate
        if (thread != null && thread.isAlive())
        {
            continueToExecute = false;
            thread.interrupt();

            try
            {
                thread.join();
            }
            catch (InterruptedException e)
            {
                LOG.warn("interrupted when waiting for interval service to end");
            }
        }

        // Start another thread...
        if (thread == null || !thread.isAlive())
        {
            thread = new Thread(() -> execute());
        }
    }

    private void execute()
    {
        boolean enabled = settingsService.getSettings().getRemoteSyncIntervalEnabled().getValue();
        Long interval = settingsService.getSettings().getRemoteSyncInterval().getValue();

        if (enabled && interval != null)
        {
            try
            {
                while (continueToExecute)
                {
                    // Sleep for interval period (milliseconds)...
                    Thread.sleep(interval);

                    // Sync all the hosts...
                    LOG.info("invoking sync all");
                    remoteSshFileService.syncAll();
                    LOG.info("finished sync");
                }
            }
            catch (InterruptedException e)
            {
                LOG.debug("remote sync interval service interrupted");
            }
        }
    }

}
