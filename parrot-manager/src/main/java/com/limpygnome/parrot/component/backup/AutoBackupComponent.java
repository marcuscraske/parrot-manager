package com.limpygnome.parrot.component.backup;

import com.limpygnome.parrot.event.DatabaseSavedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Responsible for making automatic backups, depending on the user's configuration.
 */
@Component
public class AutoBackupComponent implements DatabaseSavedEvent
{
    @Autowired
    private BackupService backupService;

    // Current thread sleeping to make backup after interval
    private Thread queuedThread;

    @Override
    public void eventDatabaseSaved()
    {
        String result = backupService.create();

        if (result != null)
        {
            throw new RuntimeException("Failed to create automatic backup - " + result);
        }
    }

    private synchronized void queueBackup()
    {
        queuedThread = new Thread(() -> {
           try
           {
               // Delay saving backup...
               Thread.sleep();

               // Make backup
               backupService.create();
           }
           catch (InterruptedException e)
           {
           }
        });
    }

}
