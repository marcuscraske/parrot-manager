package com.limpygnome.parrot.component.remote;

import com.limpygnome.parrot.component.database.DatabaseService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Synchronizes database at timed intervals.
 */
public class RemoteSyncIntervalService
{
    private static final Logger LOG = LogManager.getLogger(RemoteSyncIntervalService.class);

    @Autowired
    private DatabaseService databaseService;

    private void execute()
    {
        synchronized (databaseService)
        {
            if (databaseService.isOpen())
            {
            }
        }
    }

    private void syncAll()
    {
        remoteSshFileService.createOptions()
        remoteSshFileService.sync();
    }

}
