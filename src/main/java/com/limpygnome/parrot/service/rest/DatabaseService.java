package com.limpygnome.parrot.service.rest;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.model.db.Database;
import com.limpygnome.parrot.model.params.CryptoParams;
import com.limpygnome.parrot.service.AbstractService;
import com.limpygnome.parrot.service.server.DatabaseIOService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * REST service for database service.
 */
public class DatabaseService extends AbstractService
{
    private static final Logger LOG = LogManager.getLogger(DatabaseService.class);

    // The current database open...
    private Database database;
    private File currentFile;

    public DatabaseService(Controller controller) {
        super(controller);
    }

    /**
     * Creates a new database.
     *
     * @param location the file path of the new DB
     * @param password the desired password
     * @param rounds rounds for AES cryptography
     * @return true = success (new DB created and opened), false = failed/error...
     */
    public boolean create(String location, String password, int rounds)
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
    public String open(String path, String password)
    {
        String result;

        try
        {
            database = controller.getDatabaseIOService().open(controller, path, password.toCharArray());
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
     * @return the name of the file currently open
     */
    public String getFileName()
    {
        return currentFile != null ? currentFile.getName() : "";
    }

    /**
     * @return true = a database is open, false = not open
     */
    public boolean isOpen()
    {
        return database != null;
    }

}
