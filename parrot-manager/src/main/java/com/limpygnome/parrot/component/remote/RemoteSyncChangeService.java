package com.limpygnome.parrot.component.remote;

import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.settings.SettingsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Performs remote sync at timed intervals, the database oprns or changes occur.
 */
@Service
public class RemoteSyncChangeService
{
    private static final Logger LOG = LogManager.getLogger(SettingsService.class);

    @Autowired
    private SettingsService settingsService;
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private RemoteSshFileService remoteSshFileService;

    // Thread data
    private boolean continueToExecute;
    private Thread thread;

    public RemoteSyncChangeService()
    {
        this.continueToExecute = false;
        this.thread = null;
    }

    /**
     * To be invoked when database is opened.
     */
    public synchronized void eventDatabaseOpened()
    {
        boolean syncOnDatabaseOpened = settingsService.getSettings().getRemoteSyncOnOpeningDatabase().getSafeBoolean(false);

        if (syncOnDatabaseOpened)
        {
            forceSync();
        }
    }

    /**
     * To be invoked when database is saved.
     */
    public synchronized void eventDatabaseSaved()
    {
        boolean syncOnChange = settingsService.getSettings().getRemoteSyncOnChange().getSafeBoolean(false);

        if (syncOnChange)
        {
            forceSync();
        }
    }

    /**
     * Should be invoked when the state of the current database changes.
     */
    public synchronized void refresh()
    {
        // Wait for existing thread to terminate
        if (thread != null)
        {
            synchronized (thread)
            {
                if (thread.isAlive())
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
            }
        }

        // Check database is open...
        if (databaseService.isOpen())
        {
            // Start another thread (safely)...
            if (thread == null || !thread.isAlive())
            {
                LOG.debug("starting new thread");

                continueToExecute = true;
                thread = new Thread(() -> execute());
                thread.start();
            }
        }
    }

    private void execute()
    {
        boolean intervalSyncEnabled = settingsService.getSettings().getRemoteSyncIntervalEnabled().getSafeBoolean(false);
        Long intervalMs = settingsService.getSettings().getRemoteSyncInterval().getSafeLong(0L);

        LOG.debug("thread started");

        while (continueToExecute)
        {
            try
            {
                if (intervalSyncEnabled && intervalMs > 0)
                {
                    // Sleep for interval period (milliseconds)...
                    Thread.sleep(intervalMs);
                }
                else
                {
                    // Wait to be forcibly woken to perform sync
                    synchronized (thread)
                    {
                        wait();
                    }
                }

                if (continueToExecute)
                {
                    // Sync all the hosts...
                    LOG.info("invoking sync all");
                    remoteSshFileService.syncAll();
                    LOG.info("finished sync");
                }
                else
                {
                    LOG.debug("skipping sync, thread is stopping...");
                }
            }
            catch(InterruptedException e)
            {
                LOG.debug("remote sync interval service was interrupted for sync");
            }
        }

        LOG.debug("thread has stopped");
    }

    private synchronized void forceSync()
    {
        if (thread != null)
        {
            synchronized (thread)
            {
                if (thread.isAlive())
                {
                    LOG.info("triggering forced sync");

                    thread.notify();
                    thread.interrupt();
                }
            }
        }
        else
        {
            LOG.debug("force sync skipped as no thread running");
        }
    }

}
