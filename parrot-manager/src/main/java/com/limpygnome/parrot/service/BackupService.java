package com.limpygnome.parrot.service;

import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.io.DatabaseReaderWriter;
import com.limpygnome.parrot.model.backup.BackupFile;
import com.limpygnome.parrot.model.setting.Settings;
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
    @Autowired
    private SessionService sessionService;

    public String createBackupBeforeSave()
    {
        String result = null;

        // Check if enabled
        Settings settings = settingsService.getSettings();
        boolean isEnabled = settings.getAutomaticBackupsOnSave().getValue();

        if (isEnabled)
        {
            result = create();
        }

        return result;
    }

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

    private File[] fetchFiles()
    {
        // Fetch backup files
        File currentFile = databaseService.getFile();
        File parentFile = currentFile.getParentFile();
        String currentName = currentFile.getName();
        File[] files = parentFile.listFiles((dir, name) -> name.startsWith("." + currentName));
        return files;
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
        File[] files = fetchFiles();

        // Translate to view model
        BackupFile[] backupFiles = Arrays.stream(files).map(file -> new BackupFile(file)).toArray(size -> new BackupFile[size]);

        // Store in session to prevent GC
        sessionService.put("backups", backupFiles);

        return backupFiles;
    }

    private void checkRetainedDatabases()
    {
        long maxRetained = settingsService.getSettings().getAutomaticBackupsRetained().getValue();

        if (maxRetained > 0)
        {
            File[] backupFiles = fetchFiles();

            if (backupFiles.length >= maxRetained)
            {
                // Subtract one as we're now making a new backup, hence we only want n backups afterwards
                deleteOldestRetainedBackups(backupFiles, maxRetained - 1);
            }
        }
    }

    private void deleteOldestRetainedBackups(File[] backupFiles, long maxRetained)
    {
        Arrays.stream(backupFiles)
                .sorted((o1, o2) -> (int) (o1.lastModified() - o2.lastModified()) )
                .limit(backupFiles.length - maxRetained)
                .forEach(file -> {
                    file.delete();
                    LOG.info("deleted old retained file (max reached) - name: {}", file.getName());
                });
    }

    public String delete(BackupFile backupFile)
    {
        String result = null;
        File file = new File(backupFile.getPath());

        if (!file.delete())
        {
            result = "Unable to delete file (unknown reason)";
        }

        return result;
    }

}
