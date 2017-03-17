package com.limpygnome.parrot.library.io;

import com.limpygnome.parrot.library.db.Database;

import java.io.File;

/**
 * Generic API for reading and writing databases.
 */
public interface DatabaseReaderWriter
{

    /**
     * Opens a database from a file path.
     *
     * @param path path
     * @param password password to open database
     * @return an instance
     * @throws Exception when cannot load
     */
    Database open(String path, char[] password) throws Exception;

    /**
     * Saves a database to a file path.
     *
     * If something already exists, it will be overwritten.
     *
     * @param database database
     * @param path destination path
     * @throws Exception when cannot save
     */
    void save(Database database, String path) throws Exception;

    /**
     * Saves a database to the provided {@link File}.
     *
     * If something already exists, it will be overwritten.
     *
     * @param database database
     * @param file destination file
     * @throws Exception when cannot save
     */
    void save(Database database, File file) throws Exception;

}
