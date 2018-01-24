package com.limpygnome.parrot.converter.api;

import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.db.DatabaseMerger;
import com.limpygnome.parrot.library.db.DatabaseNode;
import com.limpygnome.parrot.library.db.log.MergeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Used to convert between input-stream/text and database.
 */
public abstract class Converter
{
    private static final Logger LOG = LoggerFactory.getLogger(Converter.class);

    /**
     *
     * @param database
     * @param options
     * @param text
     * @return result of import
     * @throws ConversionException
     * @throws MalformedInputException
     */
    public abstract MergeLog databaseImportText(Database database, Options options, String text) throws ConversionException, MalformedInputException;

    /**
     *
     * @param database
     * @param options
     * @param inputStream
     * @return result of import
     * @throws ConversionException
     * @throws MalformedInputException
     * @throws IOException
     */
    public abstract MergeLog databaseImport(Database database, Options options, InputStream inputStream) throws ConversionException, MalformedInputException, IOException;

    public abstract String databaseExportText(Database database, Options options) throws ConversionException;

    public abstract void databaseExport(Database database, Options options, OutputStream outputStream) throws ConversionException, IOException;



    protected MergeLog merge(Database database, Database databaseParsed) throws ConversionException
    {
        // merge with current database
        try
        {
            DatabaseMerger merger = new DatabaseMerger();
            MergeLog mergeLog = merger.merge(databaseParsed, database, null);
            return mergeLog;
        }
        catch (Exception e)
        {
            LOG.error("failed to merge database", e);
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
