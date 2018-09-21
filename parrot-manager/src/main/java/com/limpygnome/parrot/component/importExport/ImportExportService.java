package com.limpygnome.parrot.component.importExport;

import com.limpygnome.parrot.converter.api.ConversionException;
import com.limpygnome.parrot.converter.api.Converter;
import com.limpygnome.parrot.converter.api.MalformedInputException;
import com.limpygnome.parrot.converter.api.Options;
import com.limpygnome.parrot.event.DatabaseChangingEvent;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class ImportExportService implements DatabaseChangingEvent
{
    @Autowired
    private Map<String, Converter> converters;

    // Keep the last result to prevent GC
    private Result result;

    /**
     * Creates options for use with importing and exporting operations.
     *
     * @param format check options
     * @param remoteSync check options
     * @return an instance
     */
    public Options createOptions(String format, boolean remoteSync)
    {
        Options options = new Options();

        options.setFormat(format);
        options.setRemoteSync(remoteSync);

        return options;
    }

    public Result databaseImportText(Database database, Options options, String text)
    {
        resetResult();

        Converter converter = getConverter(options);

        if (converter == null)
        {
            return unsupported();
        }

        try
        {
            Log log = converter.databaseImportText(database, options, text);
            this.result = new Result(log);
            return this.result;
        }
        catch (MalformedInputException e)
        {
            return new Result(null, "Text is malformed");
        }
        catch (ConversionException e)
        {
            return conversionException(e);
        }
    }

    public Result databaseImportFile(Database database, Options options, String path)
    {
        resetResult();

        Converter converter = getConverter(options);

        if (converter == null)
        {
            return unsupported();
        }

        File file = new File(path);

        if (!file.exists())
        {
            return new Result(null, "File not found");
        }
        else if (!file.canRead())
        {
            return new Result(null, "File cannot be read");
        }

        try
        {
            FileInputStream fis = new FileInputStream(path);
            Log log = converter.databaseImport(database, options, fis);
            this.result = new Result(log);
            return this.result;
        }
        catch (MalformedInputException e)
        {
            return new Result(null, "File has malformed data");
        }
        catch (ConversionException | IOException e)
        {
            return conversionException(e);
        }
    }

    public Result databaseExportText(Database database, Options options)
    {
        resetResult();

        Converter converter = getConverter(options);

        if (converter == null)
        {
            return unsupported();
        }

        try
        {
            String text = converter.databaseExportText(database, options);
            return new Result(text, null);
        }
        catch (ConversionException e)
        {
            return conversionException(e);
        }
    }

    public Result databaseExportFile(Database database, Options options, String path)
    {
        resetResult();

        Converter converter = getConverter(options);

        if (converter == null)
        {
            return unsupported();
        }

        try
        {
            FileOutputStream fos = new FileOutputStream(path);
            converter.databaseExport(database, options, fos);
            return new Result();
        }
        catch (ConversionException | IOException e)
        {
            return conversionException(e);
        }
    }

    @Override
    public void eventDatabaseChanged(boolean open)
    {
        // Reset result
        result = null;
    }

    private Converter getConverter(Options options)
    {
        Converter converter = null;
        String format = options.getFormat();

        if (format != null)
        {
            converter = converters.get(format);
        }

        return converter;
    }

    private Result unsupported()
    {
        return new Result(null, "Format is not supported");
    }

    private Result conversionException(Exception e)
    {
        return new Result(null, "Problem occurred - " + e.getMessage());
    }

    private void resetResult()
    {
        this.result = null;
    }

}
