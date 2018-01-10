package com.limpygnome.parrot.converter.api;

import com.limpygnome.parrot.library.db.Database;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Used to convert between input-stream/text and database.
 */
public interface Converter
{

    /**
     *
     * @param database
     * @param options
     * @param text
     * @return list of merge messages / changes
     * @throws ConversionException
     * @throws MalformedInputException
     */
    String[] databaseImportText(Database database, Options options, String text) throws ConversionException, MalformedInputException;

    /**
     *
     * @param database
     * @param options
     * @param inputStream
     * @return list of merge messages / changes
     * @throws ConversionException
     * @throws MalformedInputException
     * @throws IOException
     */
    String[] databaseImport(Database database, Options options, InputStream inputStream) throws ConversionException, MalformedInputException, IOException;

    String databaseExportText(Database database, Options options) throws ConversionException;

    void databaseExport(Database database, Options options, OutputStream outputStream) throws ConversionException, IOException;

}
