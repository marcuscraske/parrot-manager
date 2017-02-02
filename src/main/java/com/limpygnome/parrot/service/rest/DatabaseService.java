package com.limpygnome.parrot.service.rest;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.component.FileComponent;
import com.limpygnome.parrot.model.db.Database;
import com.limpygnome.parrot.model.params.CryptoParams;
import com.limpygnome.parrot.service.AbstractService;
import com.limpygnome.parrot.service.server.DatabaseIOService;
import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.InvalidCipherTextException;

/**
 * REST service for database service.
 */
public class DatabaseService extends AbstractService
{
    private static final Logger LOG = LogManager.getLogger(DatabaseService.class);

    private FileComponent fileComponent;

    // The current database open...
    private Database database;
    private File currentFile;

    public DatabaseService(Controller controller)
    {
        super(controller);

        this.fileComponent = new FileComponent();
    }

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
        DatabaseIOService databaseIOService = controller.getDatabaseIOService();

        try
        {
            LOG.info("creating new database - location: {}, rounds: {}", location, rounds);

            // Create DB
            CryptoParams memoryCryptoParams = new CryptoParams(controller, password.toCharArray(), rounds, System.currentTimeMillis());
            CryptoParams fileCryptoParams = new CryptoParams(controller, password.toCharArray(), rounds, System.currentTimeMillis());

            this.database = databaseIOService.create(memoryCryptoParams, fileCryptoParams);

            // Attempt to save...
            databaseIOService.save(controller, database, location);

            // Update internal state
            this.currentFile = new File(location);

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
        // TODO: unit test resolving?
        path = fileComponent.resolvePath(path);

        try
        {
            database = controller.getDatabaseIOService().open(controller, path, password.toCharArray());
            currentFile = new File(path);
            result = null;
        }
        catch (InvalidCipherTextException e)
        {
            result = "Incorrect password or corrupted file";
            LOG.error("Failed to open database due to invalid crypto (wrong password / corrupted)", e);
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
        String result = null;

        try
        {
            String path = currentFile.getCanonicalPath();
            LOG.info("saving database - path: {}", path);

            // Save the database
            controller.getDatabaseIOService().save(controller, database, path);

            // Reset dirty flag
            database.setDirty(false);

            LOG.info("successfully saved database");
        }
        catch (Exception e)
        {
            result = e.getMessage();
        }

        return result;
    }

    /**
     * Unloads the current database, closing it.
     *
     * WARNING: this does not save the database.
     */
    public synchronized void close()
    {
        database = null;
        currentFile = null;
    }

    /**
     * @return the name of the file currently open
     */
    public synchronized String getFileName()
    {
        return currentFile != null ? currentFile.getName() : "";
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

}
