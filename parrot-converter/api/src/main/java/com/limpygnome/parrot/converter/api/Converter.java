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

    void databaseImportText(Database database, Options options, String text) throws ConversionException, MalformedInputException;

    void databaseImport(Database database, Options options, InputStream inputStream) throws ConversionException, MalformedInputException, IOException;

    String databaseExportText(Database database, Options options) throws ConversionException;

    void databaseExport(Database database, Options options, OutputStream outputStream) throws ConversionException, IOException;

}
