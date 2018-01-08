package com.limpygnome.parrot.converter.json;

import com.google.gson.JsonObject;
import com.limpygnome.parrot.converter.api.Converter;
import com.limpygnome.parrot.converter.api.MalformedInputException;
import com.limpygnome.parrot.converter.api.ConversionException;
import com.limpygnome.parrot.lib.database.EncryptedValueService;
import com.limpygnome.parrot.lib.io.StringStreamOperations;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.db.DatabaseNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Qualifier("json")
@Component
public class JsonConverter implements Converter
{
    @Autowired
    private StringStreamOperations stringStreamOperations;
    @Autowired
    private EncryptedValueService encryptedValueService;

    @Override
    public void databaseImport(Database database, InputStream inputStream) throws ConversionException, MalformedInputException, IOException
    {
        try
        {
            String text = stringStreamOperations.readString(inputStream);
            databaseImportText(database, text);
        }
        catch (ConversionException e)
        {
            throw new RuntimeException("text conversion should always be supported", e);
        }
    }

    @Override
    public void databaseExport(Database database, OutputStream outputStream) throws ConversionException, IOException
    {
        try
        {
            String text = databaseExportText(database);
            stringStreamOperations.writeString(outputStream, text);
        }
        catch (ConversionException e)
        {
            throw new RuntimeException("text conversion should always be supported", e);
        }
    }

    @Override
    public void databaseImportText(Database database, String text) throws ConversionException, MalformedInputException
    {

    }

    @Override
    public String databaseExportText(Database database) throws ConversionException
    {
        DatabaseNode root = database.getRoot();
        JsonObject jsonRoot = new JsonObject();

        // iterate and add all DB children
        addChildren(database, root, jsonRoot);

        // convert to string
        String text = jsonRoot.toString();

        return text;
    }

    private void addChildren(Database database, DatabaseNode root, JsonObject jsonRoot) throws ConversionException
    {
        if (!root.isRoot())
        {
            String decryptedString;

            try
            {
                EncryptedValue encryptedValue = root.getValue();
                decryptedString = encryptedValueService.asString(database, encryptedValue);
            }
            catch (Exception e)
            {
                throw new ConversionException("Failed to decrypt node value - name: " + root.getName(), e);
            }

            jsonRoot.addProperty("id", root.getId());
            jsonRoot.addProperty("name", root.getName());
            jsonRoot.addProperty("value", decryptedString);
        }
    }

}
