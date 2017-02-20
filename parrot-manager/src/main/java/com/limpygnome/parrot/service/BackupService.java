package com.limpygnome.parrot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by limpygnome on 20/02/17.
 */
@Service
public class BackupService
{
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private DatabaseIOService databaseIOService;

    public void create()
    {
        // Fetch the database currently open

        // Check if max retained databases has been met
        checkRetainedDatabases();

        // Build file-name
        String fileName = databaseService.getFileName() + "." + System.currentTimeMillis();

        // Save as backup...
        //databaseIOService.save();
    }

    private void checkRetainedDatabases()
    {
    }

}
