package com.limpygnome.parrot.service.rest;

import com.limpygnome.parrot.model.db.Database;

/**
 * REST service for database service.
 */
public class DatabaseService
{
    // The current database open...
    private Database database;

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
