package com.limpygnome.parrot.service.rest;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.model.db.Database;
import com.limpygnome.parrot.service.AbstractService;

/**
 * REST service for database service.
 */
public class DatabaseService extends AbstractService
{
    // The current database open...
    private Database database;

    public DatabaseService(Controller controller) {
        super(controller);
    }

    /**
     * Creates a new database.
     *
     * @param location the file path of the new DB
     * @param password the desired password
     * @param rounds rounds for AES cryptography
     */
    public void create(String location, String password, int rounds)
    {
    }

}
