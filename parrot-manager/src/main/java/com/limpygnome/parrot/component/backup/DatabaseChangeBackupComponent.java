package com.limpygnome.parrot.component.backup;

import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.event.DatabaseChangingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * When opening a database, this component will populate the list of available backups, or determine whether the
 * database its self is a backup.
 */
@Order(10)
@Component
public class DatabaseChangeBackupComponent implements DatabaseChangingEvent
{
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private BackupService backupService;
    @Autowired
    private BackupNameExtractorComponent nameExtractorComponent;

    @Override
    public void eventDatabaseChanged(boolean open)
    {
        if (open)
        {
            // update whether this is a backup file/database
            File fileDatabase = databaseService.getFile();
            File fileActualDatabase = nameExtractorComponent.extract(fileDatabase);
            backupService.setFileActualDatabase(fileActualDatabase);

            // populate backup files
            backupService.updateCache();
        }
        else
        {
            backupService.reset();
        }
    }

}
