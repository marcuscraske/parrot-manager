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

    void databaseImportText(Database database, String text) throws ConversionException, MalformedInputException;

    void databaseImport(Database database, InputStream inputStream) throws ConversionException, MalformedInputException, IOException;

    String databaseExportText(Database database) throws ConversionException;

    void databaseExport(Database database, OutputStream outputStream) throws ConversionException, IOException;

}
