package com.limpygnome.parrot.component.backup;

import com.limpygnome.parrot.component.settings.Settings;
import com.limpygnome.parrot.component.settings.event.SettingsRefreshedEvent;
import com.limpygnome.parrot.event.DatabaseSavedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Responsible for making automatic backups, depending on the user's configuration.
 */
@Component
public class AutomaticBackupComponent implements DatabaseSavedEvent, SettingsRefreshedEvent
{
    private static final Logger LOG = LoggerFactory.getLogger(AutomaticBackupComponent.class);

    @Autowired
    private BackupService backupService;

    // Current thread sleeping to make backup after interval
    private Thread queuedThread;

    // Delay between backups
    private long backupDelay;

    @Override
    public void eventDatabaseSaved()
    {
        if (backupDelay > 0)
        {
            queueBackup();
        }
        else
        {
            createBackup();
        }
    }

    @Override
    public void eventSettingsRefreshed(Settings settings)
    {
        backupDelay = settings.getAutomaticBackupDelay().getValue();

        if (backupDelay > 0)
        {
            // Convert from seconds to milliseconds
            backupDelay *= 1000;
        }
    }

    private synchronized void createBackup()
    {
        String result = backupService.create();

        if (result != null)
        {
            throw new RuntimeException("Failed to create automatic backup - " + result);
        }
    }

    private synchronized void queueBackup()
    {
        // Kill existing thread
        if (queuedThread != null)
        {
            queuedThread.interrupt();
            queuedThread = null;
        }

        // Start new thread
        queuedThread = new Thread(() ->
        {
           try
           {
               // Delay saving backup...
               Thread.sleep(backupDelay);

               synchronized (this)
               {
                   // Make backup
                   createBackup();

                   // Mark self as done
                   queuedThread = null;
               }
           }
           catch (InterruptedException e)
           {
               LOG.debug("aborted queued backup");
           }
        });
        queuedThread.start();
    }

}
