package com.limpygnome.parrot.service;

import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.io.DatabaseReaderWriter;
import com.limpygnome.parrot.model.backup.BackupFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;

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
    @Autowired
    private DatabaseReaderWriter databaseReaderWriter;

    /**
     * Creates a new backup.
     *
     * @return error, null if successful
     */
    public String create()
    {
        String result = null;

        try
        {
            // Fetch the database currently open
            Database database = databaseService.getDatabase();

            // Check if max retained databases has been met
            checkRetainedDatabases();

            // Build file-name
            String fileName = databaseService.getFileName() + "." + System.currentTimeMillis();

            // Save as backup...
            databaseReaderWriter.save(database, fileName);
        }
        catch (Exception e)
        {
            result = "Failed to create backup - " + e.getMessage();
        }

        return result;
    }

    /**
     * Fetches a list of existing backups.
     *
     * This will look for files in the same path as the current database, starting with the same name.
     *
     * @return array of backup file-names
     */
    public BackupFile[] fetch()
    {
        File currentFile = databaseService.getFile();
        File parentFile = currentFile.getParentFile();
        String currentName = currentFile.getName();
        File[] files = parentFile.listFiles((dir, name) -> name.startsWith(currentName) && !name.equals(currentName));

        BackupFile[] backupFiles = (BackupFile[]) Arrays.stream(files).map(file -> new BackupFile(file)).toArray();
        return backupFiles;
    }

    private void checkRetainedDatabases()
    {
        long maxRetained = settingsService.getSettings().getAutomaticBackupsRetained().getValue();
    }

}
