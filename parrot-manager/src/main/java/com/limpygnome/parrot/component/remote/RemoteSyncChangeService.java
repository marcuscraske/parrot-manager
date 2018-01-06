package com.limpygnome.parrot.component.remote;

import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.settings.SettingsService;
import com.limpygnome.parrot.event.DatabaseChangingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Performs remote sync at timed intervals, the database opens or changes occur.
 */
@Service
public class RemoteSyncChangeService implements DatabaseChangingEvent
{
    private static final Logger LOG = LoggerFactory.getLogger(RemoteSyncChangeService.class);

    @Autowired
    private SettingsService settingsService;
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private RemoteSyncService remoteSyncService;

    // Thread data
    private boolean continueToExecute;
    private Thread thread;

    public RemoteSyncChangeService()
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
            boolean syncOnDatabaseOpened = settingsService.getSettings().getRemoteSyncOnOpeningDatabase().getSafeBoolean(false);

            if (syncOnDatabaseOpened)
            {
                forceSync();
            }
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
    public void refreshContext()
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

                // Join execution...
                LOG.debug("joining thread");

                try
                {
                    thread.join();
                }
                catch (InterruptedException e)
                {
                    LOG.warn("interrupted when waiting for interval service to end");
                }
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
        boolean intervalSyncEnabled = settingsService.getSettings().getRemoteSyncIntervalEnabled().getSafeBoolean(false);
        Long intervalMs = settingsService.getSettings().getRemoteSyncInterval().getSafeLong(0L);

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
                synchronized (thread)
                {
                    thread.notify();
                }
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
        boolean intervalEnabled = settingsService.getSettings().getRemoteSyncIntervalEnabled().getSafeBoolean(false);
        long intervalPeriod = settingsService.getSettings().getRemoteSyncInterval().getSafeLong(0L);
        boolean interval = intervalEnabled && intervalPeriod > 0;

        boolean syncOnDatabaseOpened = settingsService.getSettings().getRemoteSyncOnOpeningDatabase().getSafeBoolean(false);
        boolean syncOnChange = settingsService.getSettings().getRemoteSyncOnChange().getSafeBoolean(false);

        boolean result = interval || syncOnDatabaseOpened || syncOnChange;
        return result;
    }

}
