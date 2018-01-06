package com.limpygnome.parrot.component.database;

import com.limpygnome.parrot.component.file.FileComponent;
import com.limpygnome.parrot.component.recentFile.RecentFile;
import com.limpygnome.parrot.component.recentFile.RecentFileService;
import com.limpygnome.parrot.event.DatabaseChangingEvent;
import com.limpygnome.parrot.event.DatabaseSavedEvent;
import com.limpygnome.parrot.library.crypto.CryptoParams;
import com.limpygnome.parrot.library.crypto.CryptoParamsFactory;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.io.DatabaseReaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A service for maintaining the current (primary) database open.
 */
@Service
public class DatabaseService
{
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseService.class);

    // Services
    @Autowired
    private RecentFileService recentFileService;

    // Components
    @Autowired
    private DatabaseReaderWriter databaseReaderWriter;
    @Autowired
    private FileComponent fileComponent;
    @Autowired
    private CryptoParamsFactory cryptoParamsFactory;
    @Autowired
    private DatabaseAutoSaveHandler autoSaveHandler;

    // Events
    @Autowired
    private List<DatabaseChangingEvent> databaseChangingEventList;
    @Autowired
    private List<DatabaseSavedEvent> databaseSavedEventList;

    // The current database open...
    private Database database;
    private File currentFile;
    private char[] password;

    /**
     * Creates a new database.
     *
     * @param path the file path of the new DB
     * @param password the desired password
     * @param rounds rounds for AES cryptography
     * @return true = success (new DB created and opened), false = failed/error...
     */
    public synchronized boolean create(String path, String password, int rounds)
    {
        try
        {
            // ensure database is closed
            enforceDatabaseOpen(false);

            // ensure path is resolved
            path = fileComponent.resolvePath(path);
            LOG.info("creating new database - file path: {}, rounds: {}", path, rounds);

            // create database
            CryptoParams memoryCryptoParams = cryptoParamsFactory.create(
                    password.toCharArray(), rounds, System.currentTimeMillis()
            );
            CryptoParams fileCryptoParams = cryptoParamsFactory.create(
                    password.toCharArray(), rounds, System.currentTimeMillis()
            );

            Database database = new Database(memoryCryptoParams, fileCryptoParams);

            // write to disk
            databaseReaderWriter.save(database, path);

            // change internal state
            setDatabase(database, password, path);

            LOG.info("created database successfully");

            return true;
        }
        catch (Exception e)
        {
            LOG.error("failed to create database - path: {}, rounds: {}, pass len: {}", path, rounds, (password != null ? password.length() : "null"), e);
            return false;
        }
    }

    /**
     * Opens a database from the file system.
     *
     * If an existing database is already open, it's closed.
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
            Database database = databaseReaderWriter.open(path, password.toCharArray());
            setDatabase(database, password, path);

            result = null;
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

        // ensure database is open
        enforceDatabaseOpen(true);

        try
        {
            String path = currentFile.getCanonicalPath();
            LOG.info("saving database - path: {}", path);

            // Save the database
            databaseReaderWriter.save(database, path);

            // Reset dirty flag
            database.setDirty(false);

            LOG.info("successfully saved database");

            // raise event
            raiseSavedEvent();

            result = null;
        }
        catch (Exception e)
        {
            result = e.getMessage();
        }

        return result;
    }

    /**
     * Changes the password of the database currently open.
     *
     * @param password the new password
     * @throws Exception if password change fails
     */
    public synchronized void changePassword(String password) throws Exception
    {
        enforceDatabaseOpen(true);

        // change password
        database.changePassword(password);

        // update internally stored copy of password
        this.password = password.toCharArray();
    }

    /**
     * Unloads the current database, closing it.
     *
     * WARNING: this does not save the database.
     */
    public synchronized void close()
    {
        enforceDatabaseOpen(true);

        // Update internal state
        database = null;
        updateCurrentFile(null);
        password = null;

        // Raise event with other components
        raiseChangeEvent(false);
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

    private void setDatabase(Database database, String password, String path)
    {
        // close existing database
        if (isOpen())
        {
            LOG.debug("closing existing database");
            close();
        }

        // ensure database is closed
        enforceDatabaseOpen(false);

        // update internal state
        this.database = database;

        File currentFile = new File(path);
        updateCurrentFile(currentFile);
        this.password = password.toCharArray();

        // hook database to auto-save changes
        database.getDirtyEventHandlers().add(autoSaveHandler);

        // raise event
        raiseChangeEvent(true);

        LOG.info("updated database - path: {}", path);
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

    private void enforceDatabaseOpen(boolean shouldBeOpen)
    {
        boolean open = isOpen();
        boolean result = (open == shouldBeOpen);

        if (!result)
        {
            if (open)
            {
                throw new IllegalStateException("database should be closed");
            }
            else
            {
                throw new IllegalStateException("database should be open");
            }
        }
    }

    private void raiseChangeEvent(boolean open)
    {
        for (DatabaseChangingEvent component : databaseChangingEventList)
        {
            component.eventDatabaseChanged(open);
        }
    }

    private void raiseSavedEvent()
    {
        for (DatabaseSavedEvent component : databaseSavedEventList)
        {
            component.eventDatabaseSaved();
        }
    }

}
