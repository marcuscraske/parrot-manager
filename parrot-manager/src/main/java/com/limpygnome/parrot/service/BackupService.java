package com.limpygnome.parrot.service;

import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.io.DatabaseReaderWriter;
import com.limpygnome.parrot.model.backup.BackupFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;

/**
 * Used to create backups of a database.
 */
@Service
public class BackupService
{
    private static final Logger LOG = LogManager.getLogger(BackupService.class);

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

        LOG.info("creating backup...");

        try
        {
            // Fetch the database currently open
            Database database = databaseService.getDatabase();

            // Check if max retained databases has been met
            checkRetainedDatabases();

            // Build path
            File currentFile = databaseService.getFile();
            File currentParentFile = currentFile.getParentFile();
            File backupFile = new File(currentParentFile, "." + databaseService.getFileName() + "." + System.currentTimeMillis());

            // Save as backup...
            databaseReaderWriter.save(database, backupFile);

            LOG.info("backup created - name: {}", backupFile.getAbsolutePath());
        }
        catch (Exception e)
        {
            result = "Failed to create backup - " + e.getMessage();
            LOG.error("failed to create backup", e);
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
        File[] files = parentFile.listFiles((dir, name) -> name.startsWith("." + currentName));

        BackupFile[] backupFiles = Arrays.stream(files).map(file -> new BackupFile(file)).toArray(size -> new BackupFile[size]);
        return backupFiles;
    }

    // TODO: finish...
    private void checkRetainedDatabases()
    {
        long maxRetained = settingsService.getSettings().getAutomaticBackupsRetained().getValue();
    }

}
