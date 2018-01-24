package com.limpygnome.parrot.component.remote;

import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.settings.Settings;
import com.limpygnome.parrot.component.settings.event.SettingsRefreshedEvent;
import com.limpygnome.parrot.event.DatabaseChangingEvent;
import com.limpygnome.parrot.event.DatabaseSavedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Performs remote sync at timed intervals, the database opens or changes occur.
 */
@Service
public class RemoteSyncIntervalService implements DatabaseChangingEvent, DatabaseSavedEvent, SettingsRefreshedEvent
{
    private static final Logger LOG = LoggerFactory.getLogger(RemoteSyncIntervalService.class);

    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private RemoteSyncService remoteSyncService;

    // Settings
    private boolean intervalSyncEnabled;
    private long intervalMs;
    private boolean syncOnDatabaseOpened;
    private boolean syncOnChange;

    // Thread data
    private boolean continueToExecute;
    private Thread thread;

    public RemoteSyncIntervalService()
    {
        this.continueToExecute = false;
        this.thread = null;
    }

    @Override
    public synchronized void eventDatabaseChanged(boolean open)
    {
        refreshContext();

        if (open)
        {
            if (syncOnDatabaseOpened)
            {
                forceSync();
            }
        }
    }

    @Override
    public synchronized void eventDatabaseSaved()
    {
        if (syncOnChange)
        {
            forceSync();
        }
    }

    @Override
    public void eventSettingsRefreshed(Settings settings)
    {
        // Update settings
        intervalSyncEnabled = settings.getRemoteSyncIntervalEnabled().getSafeBoolean(false);
        intervalMs = settings.getRemoteSyncInterval().getSafeLong(0L);
        syncOnDatabaseOpened = settings.getRemoteSyncOnOpeningDatabase().getSafeBoolean(false);
        syncOnChange = settings.getRemoteSyncOnChange().getSafeBoolean(false);

        // Refresh context (automatic syncing)
        refreshContext();
    }

    /**
     * Should be invoked when the state of the current database changes.
     */
    public synchronized void refreshContext()
    {
        try
        {
            LOG.debug("refreshing...");

            // Wait for existing thread to terminate
            if (thread != null && thread.isAlive())
            {
                // Signal to thread to end...
                continueToExecute = false;

                // Force wake it...
                forceSync();
            }

            // Check database is open and we have settings enabling this feature...
            if (databaseService.isOpen() && canRunAtAll())
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
        catch (Exception e)
        {
            LOG.error("failed to refresh", e);
        }
    }

    private void execute()
    {
        LOG.debug("thread started");

        while (continueToExecute)
        {
            // Wait until we need to sync...
            try
            {
                if (intervalSyncEnabled && intervalMs > 0)
                {
                    LOG.debug("sleeping for interval - {} ms", intervalMs);

                    // Sleep for interval period (milliseconds)...
                    Thread.sleep(intervalMs);
                }
                else
                {
                    LOG.debug("waiting for change");

                    // Wait to be forcibly woken to perform sync
                    synchronized (thread)
                    {
                        thread.wait();
                    }
                }
            }
            catch(InterruptedException e)
            {
                LOG.debug("remote sync interval service was interrupted for sync");
            }

            // Sync...
            if (continueToExecute)
            {
                // Sync all the hosts...
                LOG.info("invoking sync all");
                remoteSyncService.syncAll();
                LOG.info("finished sync");
            }
            else
            {
                LOG.debug("skipping sync, thread is stopping...");
            }
        }

        LOG.debug("thread has stopped");
    }

    private void forceSync()
    {
        LOG.debug("forcing sync...");

        if (thread != null && thread.isAlive())
        {
            LOG.info("triggering forced sync");

            try
            {
                thread.interrupt();
            }
            catch (Exception e)
            {
                LOG.warn("failed to force sync", e);
            }
        }
        else
        {
            LOG.debug("force sync skipped as no thread running");
        }
    }

    /*
        Check all settings to see if this service can run at all.
     */
    private boolean canRunAtAll()
    {
        boolean interval = intervalSyncEnabled && intervalMs > 0;

        boolean result = interval || syncOnDatabaseOpened || syncOnChange;
        return result;
    }

}
