package com.limpygnome.parrot.service;

import com.limpygnome.parrot.library.db.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by limpygnome on 20/02/17.
 */
@Service
public class BackupService
{
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private DatabaseService databaseService;

    public void create()
    {
        // Fetch the database currently open
        Database database = databaseService.getDatabase();

        // Check if max retained databases has been met
        checkRetainedDatabases();

        // Build file-name
        String fileName = databaseService.getFileName() + "." + System.currentTimeMillis();

        // Save as backup...
        //databaseIOService.save();
    }

    private void checkRetainedDatabases()
    {
        long maxRetained = settingsService.getSettings().automaticBackupsRetained().value();
    }

}
