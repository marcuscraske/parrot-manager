package com.limpygnome.parrot.component.recentFile;

import com.limpygnome.parrot.component.backup.BackupService;
import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.event.DatabaseChangingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Upon opening a database, this component will add the newly opened file to the list of recent files.
 *
 * Notes:
 * - The priority of this component should be lower than backup component, so that the backup component can determine
 *   whether the new file is a backup.
 */
@Order(20)
@Component
public class RecentFileChangeComponent implements DatabaseChangingEvent
{
    private static final Logger LOG = LoggerFactory.getLogger(RecentFileChangeComponent.class);

    @Autowired
    private RecentFileService recentFileService;
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private BackupService backupService;

    @Override
    public void eventDatabaseChanged(boolean open)
    {
        if (open && !backupService.isBackupOpen())
        {
            try
            {
                File fileDatabase = databaseService.getFile();
                recentFileService.add(new RecentFile(fileDatabase));
            }
            catch (IOException e)
            {
                LOG.error("failed to update recent files", e);
            }
        }
    }

}
