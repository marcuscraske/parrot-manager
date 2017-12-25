package com.limpygnome.parrot.component.database;

import com.limpygnome.parrot.component.backup.BackupService;
import com.limpygnome.parrot.component.file.FileComponent;
import com.limpygnome.parrot.component.recentFile.RecentFile;
import com.limpygnome.parrot.component.recentFile.RecentFileService;
import com.limpygnome.parrot.component.remote.RemoteSyncChangeService;
import com.limpygnome.parrot.component.session.SessionService;
import com.limpygnome.parrot.library.crypto.CryptoParams;
import com.limpygnome.parrot.library.crypto.CryptoParamsFactory;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.io.DatabaseReaderWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * A service for maintaining the current (primary) database open.
 */
@Service
public class DatabaseService
{
    private static final Logger LOG = LogManager.getLogger(DatabaseService.class);

    // Services
    @Autowired
    private SessionService sessionService;
    @Autowired
    private BackupService backupService;
    @Autowired
    private RecentFileService recentFileService;
    @Autowired
    private RemoteSyncChangeService remoteSyncChangeService;

    // Components
    @Autowired
    private DatabaseReaderWriter databaseReaderWriter;
    @Autowired
    private FileComponent fileComponent;
    @Autowired
    private CryptoParamsFactory cryptoParamsFactory;

    // The current database open...
    private Database database;
    private File currentFile;
    private char[] password;

    /**
     * Creates a new database.
     *
     * @param location the file path of the new DB
     * @param password the desired password
     * @param rounds rounds for AES cryptography
     * @return true = success (new DB created and opened), false = failed/error...
     */
    public synchronized boolean create(String location, String password, int rounds)
    {
        try
        {
            // Ensure path is resolved
            location = fileComponent.resolvePath(location);
            LOG.info("creating new database - location: {}, rounds: {}", location, rounds);

            // Create DB
            CryptoParams memoryCryptoParams = cryptoParamsFactory.create(
                    password.toCharArray(), rounds, System.currentTimeMillis()
            );
            CryptoParams fileCryptoParams = cryptoParamsFactory.create(
                    password.toCharArray(), rounds, System.currentTimeMillis()
            );

            this.database = new Database(memoryCryptoParams, fileCryptoParams);

            // Attempt to save...
            databaseReaderWriter.save(database, location);

            // Update internal state
            File currentFile = new File(location);
            updateCurrentFile(currentFile);
            sessionService.reset();
            this.password = password.toCharArray();

            // Refresh interval syncing
            remoteSyncChangeService.refresh();

            LOG.info("created database successfully - location: {}", location);

            return true;
        }
        catch (Exception e)
        {
            LOG.error("failed to create database - location: {}, rounds: {}, pass len: {}", location, rounds, (password != null ? password.length() : "null"), e);
            return false;
        }
    }

    /**
     * Opens a database from the file system.
     *
     * @param path the path of the database
     * @param password the password to open the database
     * @return error message, or null if successfully opened
     */
    public synchronized String open(String path, String password)
    {
        String result;

        // Ensure path is fully resolved
        path = fileComponent.resolvePath(path);

        try
        {
            // Open file
            database = databaseReaderWriter.open(path, password.toCharArray());

            // Update internal state
            File currentFile = new File(path);
            updateCurrentFile(currentFile);
            sessionService.reset();
            result = null;
            this.password = password.toCharArray();

            // Refresh interval syncing
            remoteSyncChangeService.refresh();

            // Invoke event handlers
            remoteSyncChangeService.eventDatabaseOpened();
        }
        catch (Exception e)
        {
            result = "Failed to open file - " + e.getMessage();
            LOG.error("Failed to open database - path: {}, pass len: {}", path, (password != null ? password.length() : "null"), e);
        }

        return result;
    }

    /**
     * Saves the current database.
     *
     * This will also reset the dirty flag to false.
     *
     * @return error message if unsuccessful, otherwise null if successful
     */
    public synchronized String save()
    {
        String result;

        try
        {
            String path = currentFile.getCanonicalPath();
            LOG.info("saving database - path: {}", path);

            // Create backup
            result = backupService.create();

            if (result == null)
            {
                // Save the database
                databaseReaderWriter.save(database, path);

                // Reset dirty flag
                database.setDirty(false);

                LOG.info("successfully saved database");

                // Invoke event handlers
                remoteSyncChangeService.eventDatabaseSaved();
            }
        }
        catch (Exception e)
        {
            result = e.getMessage();
        }

        return result;
    }

    private void updateCurrentFile(File currentFile)
    {
        if (currentFile != null)
        {
            try
            {
                RecentFile recentFile = new RecentFile(currentFile);
                recentFileService.add(recentFile);
            }
            catch (IOException e)
            {
                LOG.error("failed to update recent files", e);
            }
        }

        this.currentFile = currentFile;
    }

    /**
     * Changes the password of the database currently open.
     *
     * @param password the new password
     * @throws Exception if password change fails
     */
    public synchronized void changePassword(String password) throws Exception
    {
        if (database != null)
        {
            // change password
            database.changePassword(password);

            // update internally stored copy of password
            this.password = password.toCharArray();
        }
    }

    /**
     * Unloads the current database, closing it.
     *
     * WARNING: this does not save the database.
     */
    public synchronized void close()
    {
        // Update internal state
        database = null;
        updateCurrentFile(null);
        sessionService.reset();
        password = null;

        // Refresh interval service
        remoteSyncChangeService.refresh();
    }

    /**
     * @return name of the file currently open
     */
    public synchronized String getFileName()
    {
        return currentFile != null ? currentFile.getName() : "";
    }

    /**
     * @return full path of the file currently open
     */
    public synchronized String getPath()
    {
        return currentFile != null ? currentFile.getAbsolutePath() : "";
    }

    public synchronized File getFile()
    {
        return currentFile;
    }

    /**
     * @return true = a database is open, false = not open
     */
    public synchronized boolean isOpen()
    {
        return database != null;
    }

    /**
     * @return true = database has been modified/dirty, false = unchanged database
     */
    public synchronized boolean isDirty()
    {
        return database != null && database.isDirty();
    }

    /**
     * @return retrieves current database; null if not open
     */
    public synchronized Database getDatabase()
    {
        return database;
    }

    /**
     * @return password for the current database
     */
    public synchronized String getPassword()
    {
        return new String(password);
    }

}
