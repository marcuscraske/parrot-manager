package com.limpygnome.parrot.component.backup;

import com.limpygnome.parrot.component.database.DatabaseService;
import com.limpygnome.parrot.component.session.SessionService;
import com.limpygnome.parrot.component.settings.Settings;
import com.limpygnome.parrot.component.settings.event.SettingsRefreshedEvent;
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
public class BackupService implements SettingsRefreshedEvent
{
    private static final Logger LOG = LoggerFactory.getLogger(BackupService.class);

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

    // Settings
    private long automaticBackupsRetained;
    private boolean automaticBackupOnSave;

    @Override
    public void eventSettingsRefreshed(Settings settings)
    {
        automaticBackupsRetained = settings.getAutomaticBackupsRetained().getSafeLong(0);
        automaticBackupOnSave = settings.getAutomaticBackupsOnSave().getSafeBoolean(false);
    }

    /**
     * Creates a new backup.
     *
     * @return error, null if successful
     */
    public synchronized String create()
    {
        String errorMessage = createBackup(true);
        return errorMessage;
    }

    private synchronized String createBackup(boolean deleteOldFiles)
    {
        String errorMessage = null;

        // Check database open and backups are enabled
        File currentFile = databaseService.getFile();

        if (currentFile != null && automaticBackupOnSave)
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
    public synchronized BackupFile[] fetch()
    {
        return cachedBackupFiles;
    }

    /**
     * Restores a backup.
     *
     * This file will replace the current database.
     *
     * @param path the path of the file to be restored
     * @return error message; null if successful
     */
    public synchronized String restore(String path)
    {
        String errorMessage = null;

        // check file is a backup file
        if (!isBackupFile(path))
        {
            errorMessage = "Backup file is not valid";
        }
        else
        {
            // create backup of current database first
            errorMessage = createBackup(false);

            if (errorMessage == null)
            {
                File fileSrc = new File(path);

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
        }

        return errorMessage;
    }

    /**
     * Deletes a backup.
     *
     * TODO move this to use IDs, rather than paths - potentially dangerous in future otherwise
     *
     * @param path the path of the backup file to be deleted
     * @return error message; null if successful
     */
    public synchronized String delete(String path)
    {
        String result = null;

        // Check path is actually a backup (prevent any file being deleted)
        boolean found = isBackupFile(path);

        if (!found)
        {
            result = "Backup no longer exists";
        }
        else
        {
            File file = new File(path);

            if (!file.delete())
            {
                result = "Unable to delete file (unknown reason)";
            }
            else
            {
                LOG.info("wiped backup - path={}", file.getAbsolutePath());
            }

            // Refresh files
            updateCache();
        }

        return result;
    }

    public synchronized void wipeAll()
    {
        LOG.info("wiping all backups...");

        File[] files = fetchFiles();
        for (File file : files)
        {
            file.delete();
            LOG.info("wiped backup - path={}", file.getAbsolutePath());
        }

        LOG.info("wiped all backups.");

        updateCache();
    }

    // Layer of protection against front-end playing with non-backup files
    private boolean isBackupFile(String path)
    {
        boolean found = false;

        if (path != null && path.length() > 0)
        {
            for (int i = 0; !found && i < cachedBackupFiles.length; i++)
            {
                BackupFile backupFile = cachedBackupFiles[i];
                if (path.equals(backupFile.getPath()))
                {
                    found = true;
                }
            }
        }

        return found;
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

    /**
     * Used to delete a backup, when a backup is open as the current database.
     */
    public synchronized void deleteCurrentBackup()
    {
        if (fileActualDatabase == null)
        {
            throw new IllegalStateException("backup not open");
        }

        File current = databaseService.getFile();

        // Close current database
        databaseService.close();

        // Delete current database open
        current.delete();

        LOG.info("deleted current backup and closed database");
    }

    /**
     * Restores current backup file to become main.
     *
     * The old main database becomes a backup file, even if backups are disabled.
     */
    public synchronized void restoreCurrentBackup()
    {
        File current = databaseService.getFile();
        File fileActualDatabase = this.fileActualDatabase;

        // Close current database
        databaseService.close();

        // Move main as backup
        File fileActualParent = fileActualDatabase.getParentFile();
        String newName = "." + fileActualDatabase.getName() + "." + System.currentTimeMillis();
        File fileActualDatabaseRenamed = new File(fileActualParent, newName);
        fileActualDatabase.renameTo(fileActualDatabaseRenamed);

        // Move current to become old main
        current.renameTo(fileActualDatabase);

        LOG.info("restored backup to main database");
    }

    void updateCache()
    {
        File[] files = fetchFiles();
        BackupFile[] backupFiles = Arrays.stream(files).map(file -> new BackupFile(file)).toArray(size -> new BackupFile[size]);

        // Store copy internally to prevent gc
        this.cachedBackupFiles = backupFiles;

        // Raise change event
        webStageInitService.triggerEvent("document", "backupChange", null);

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
        if (automaticBackupsRetained > 0)
        {
            File[] backupFiles = fetchFiles();

            if (backupFiles.length >= automaticBackupsRetained)
            {
                // Subtract one as we're now making a new backup, hence we only want n backups afterwards
                deleteOldestRetainedBackups(backupFiles, automaticBackupsRetained - 1);
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
