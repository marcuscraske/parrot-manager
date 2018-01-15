package com.limpygnome.parrot.converter.api;

import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.db.DatabaseMerger;
import com.limpygnome.parrot.library.db.DatabaseNode;
import com.limpygnome.parrot.library.db.MergeLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Used to convert between input-stream/text and database.
 */
public abstract class Converter
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
    public abstract String[] databaseImportText(Database database, Options options, String text) throws ConversionException, MalformedInputException;

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
    public abstract String[] databaseImport(Database database, Options options, InputStream inputStream) throws ConversionException, MalformedInputException, IOException;

    public abstract String databaseExportText(Database database, Options options) throws ConversionException;

    public abstract void databaseExport(Database database, Options options, OutputStream outputStream) throws ConversionException, IOException;



    protected String[] merge(Database database, Database databaseParsed) throws ConversionException
    {
        // merge with current database
        try
        {
            DatabaseMerger merger = new DatabaseMerger();
            MergeLog mergeLog = merger.merge(databaseParsed, database, null);
            List<String> listMessages = mergeLog.getMessages();
            String[] messages = listMessages.toArray(new String[listMessages.size()]);
            return messages;
        }
        catch (Exception e)
        {
            throw new ConversionException("Failed to merge database - " + e.getMessage(), e);
        }
    }

    protected boolean isProhibitedNode(Options options, DatabaseNode node)
    {
        if (!options.isRemoteSync() && "/root/remote-sync".equals(node.getPath()))
        {
            return true;
        }

        return false;
    }

    protected Database createDatabase(Database original)
    {
        Database database = new Database(original.getMemoryCryptoParams(), original.getFileCryptoParams());
        return database;
    }

}
