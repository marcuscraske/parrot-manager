package com.limpygnome.parrot.component.backup;

import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.session.SessionService;
import com.limpygnome.parrot.component.settings.Settings;
import com.limpygnome.parrot.component.settings.SettingsService;
import com.limpygnome.parrot.component.ui.WebStageInitService;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.io.DatabaseReaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * Used to create backups of a database.
 */
@Service
public class BackupService
{
    private static final Logger LOG = LoggerFactory.getLogger(BackupService.class);

    @Autowired
    private SettingsService settingsService;
    @Autowired
    private DatabaseService databaseService;
    @Autowired
    private DatabaseReaderWriter databaseReaderWriter;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private WebStageInitService webStageInitService;

    // When a backup is opened, the path to the actual database is stored here; presence used to indicate database is a backup
    private File fileActualDatabase;

    // Cache of available backup files
    private BackupFile[] cachedBackupFiles;

    /**
     * Creates a new backup.
     *
     * @return error, null if successful
     */
    public String create()
    {
        String errorMessage = createBackup(true);
        return errorMessage;
    }

    private String createBackup(boolean deleteOldFiles)
    {
        String errorMessage = null;

        // Check database open and backups are enabled
        Settings settings = settingsService.getSettings();
        boolean isEnabled = settings.getAutomaticBackupsOnSave().getSafeBoolean(false);
        File currentFile = databaseService.getFile();

        if (currentFile != null && isEnabled)
        {
            LOG.info("creating backup...");

            try
            {
                // Fetch the database currently open
                Database database = databaseService.getDatabase();

                // Check if max retained databases has been met
                if (deleteOldFiles)
                {
                    checkRetainedDatabases();
                }

                // Build path
                File currentParentFile = currentFile.getParentFile();
                File backupFile = new File(currentParentFile, "." + databaseService.getFileName() + "." + System.currentTimeMillis());

                // Save as backup...
                databaseReaderWriter.save(database, backupFile);

                LOG.info("backup created - name: {}", backupFile.getAbsolutePath());
            }
            catch (Exception e)
            {
                errorMessage = "Failed to create backup - " + e.getMessage();
                LOG.error("failed to create backup", e);
            }
        }

        // Refresh files
        updateCache();

        return errorMessage;
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
        return cachedBackupFiles;
    }

    /**
     * Restores a backup.
     *
     * @param backupFile the instance to be restored
     * @return error message; null if successful
     */
    public String restore(BackupFile backupFile)
    {
        // create backup of current database first
        String errorMessage = createBackup(false);

        if (errorMessage == null)
        {
            String pathSrc = backupFile.getPath();
            File fileSrc = new File(pathSrc);

            String pathDest = databaseService.getPath();
            File fileDest = new File(pathDest);

            if (!fileSrc.exists())
            {
                errorMessage = "Backup no longer exists?";
            }
            else if (!fileDest.exists())
            {
                errorMessage = "Current database no longer exists?";
            }
            else
            {
                // overwrite file
                try
                {
                    Files.copy(fileSrc.toPath(), fileDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                catch (Exception e)
                {
                    errorMessage = e.getMessage();
                }
            }
        }

        return errorMessage;
    }

    /**
     * Deletes a backup.
     *
     * @param backupFile the instance to be deleted
     * @return error message; null if successful
     */
    public String delete(BackupFile backupFile)
    {
        String result = null;
        File file = new File(backupFile.getPath());

        if (!file.delete())
        {
            result = "Unable to delete file (unknown reason)";
        }

        // Refresh files
        updateCache();

        return result;
    }

    /**
     * @return indicates whether current database is a backup file
     */
    public boolean isBackupOpen()
    {
        return fileActualDatabase != null;
    }

    void setFileActualDatabase(File file)
    {
        this.fileActualDatabase = file;
    }

    /**
     * @return file for the actual database; null if backup not open
     */
    public File getFileActualDatabase()
    {
        return fileActualDatabase;
    }

    /**
     * @return path for the actual database; null if backup not open
     */
    public String getActualDatabasePath()
    {
        return fileActualDatabase.getAbsolutePath();
    }

    void updateCache()
    {
        File[] files = fetchFiles();
        BackupFile[] backupFiles = Arrays.stream(files).map(file -> new BackupFile(file)).toArray(size -> new BackupFile[size]);

        // Store copy internally to prevent gc
        this.cachedBackupFiles = backupFiles;

        // Raise change event
        webStageInitService.triggerEvent("document", "backupChange", backupFiles);

        LOG.debug("updated cache and triggered change event");
    }

    private File[] fetchFiles()
    {
        File[] files;

        // Fetch backup files
        File currentFile = databaseService.getFile();

        if (currentFile != null)
        {
            File parentFile = currentFile.getParentFile();
            String currentName = currentFile.getName();
            files = parentFile.listFiles((dir, name) -> name.startsWith("." + currentName));
        }
        else
        {
            files = new File[0];
            LOG.warn("attempted to fetch backup files when database closed");
        }

        return files;
    }

    private void checkRetainedDatabases()
    {
        long maxRetained = settingsService.getSettings().getAutomaticBackupsRetained().getSafeLong(0);

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

    void reset()
    {
        fileActualDatabase = null;
        cachedBackupFiles = null;
        LOG.debug("wiped cache of files");
    }

}
