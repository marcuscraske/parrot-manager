package com.limpygnome.parrot.component.backup;

import com.limpygnome.parrot.component.settings.Settings;
import com.limpygnome.parrot.component.settings.event.SettingsRefreshedEvent;
import com.limpygnome.parrot.event.DatabaseSavedEvent;
import com.limpygnome.parrot.lib.threading.DelayedThread;
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

    // Manager for queued thread
    DelayedThread queuedThread;

    // Delay between backups
    private long backupDelay;

    public AutomaticBackupComponent()
    {
        queuedThread = new DelayedThread();
    }

    @Override
    public void eventDatabaseSaved()
    {
        if (!backupService.isBackupOpen())
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
        else
        {
            LOG.debug("skipped backup, as backup is open");
        }
    }

    @Override
    public void eventSettingsRefreshed(Settings settings)
    {
        backupDelay = settings.getAutomaticBackupDelay().getSafeLong(0L) * 1000L;
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
        // Start new thread
        queuedThread.start(() -> createBackup(), backupDelay);
    }

}
