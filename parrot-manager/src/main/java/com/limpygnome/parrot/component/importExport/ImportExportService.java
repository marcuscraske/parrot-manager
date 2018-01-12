package com.limpygnome.parrot.component.importExport;

import com.limpygnome.parrot.converter.api.ConversionException;
import com.limpygnome.parrot.converter.api.Converter;
import com.limpygnome.parrot.converter.api.MalformedInputException;
import com.limpygnome.parrot.converter.api.Options;
import com.limpygnome.parrot.library.db.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
public class ImportExportService
{
    @Autowired
    private Map<String, Converter> converters;

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
        Converter converter = getConverter(options);

        if (converter == null)
        {
            return unsupported();
        }

        try
        {
            String[] messages = converter.databaseImportText(database, options, text);
            Result result = new Result(messages);
            return result;
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
            String[] messages = converter.databaseImport(database, options, fis);
            Result result = new Result(messages);
            return result;
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

}
