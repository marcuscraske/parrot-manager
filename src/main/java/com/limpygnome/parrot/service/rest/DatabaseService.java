package com.limpygnome.parrot.service.rest;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.model.db.Database;
import com.limpygnome.parrot.model.params.CryptoParams;
import com.limpygnome.parrot.service.AbstractService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

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
        try
        {
            LOG.info("creating new database - location: {}, rounds: {}", location, rounds);

            // Create DB
            CryptoParams memoryCryptoParams = new CryptoParams(controller, password.toCharArray(), rounds, System.currentTimeMillis());
            CryptoParams fileCryptoParams = new CryptoParams(controller, password.toCharArray(), rounds, System.currentTimeMillis());

            // TODO: needs to pass back result...
            this.database = controller.getDatabaseIOService().create(memoryCryptoParams, fileCryptoParams);

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
