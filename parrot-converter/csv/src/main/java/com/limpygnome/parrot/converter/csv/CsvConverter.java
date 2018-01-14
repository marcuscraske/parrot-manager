package com.limpygnome.parrot.converter.csv;

import com.limpygnome.parrot.converter.api.ConversionException;
import com.limpygnome.parrot.converter.api.Converter;
import com.limpygnome.parrot.converter.api.MalformedInputException;
import com.limpygnome.parrot.converter.api.Options;
import com.limpygnome.parrot.lib.database.EncryptedValueService;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.db.DatabaseNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component("csv")
public class CsvConverter extends Converter
{
    @Autowired
    private EncryptedValueService encryptedValueService;

    @Override
    public String[] databaseImport(Database database, Options options, InputStream inputStream) throws ConversionException, MalformedInputException, IOException
    {
        return new String[0];
    }

    @Override
    public void databaseExport(Database database, Options options, OutputStream outputStream) throws ConversionException, IOException
    {
    }

    @Override
    public String[] databaseImportText(Database database, Options options, String text) throws ConversionException, MalformedInputException
    {
        return new String[0];
    }

    @Override
    public String databaseExportText(Database database, Options options) throws ConversionException
    {
        StringBuilder buffer = new StringBuilder();

        // append column names
        buffer.append("ParentId,ParentName,Id,Name,Value").append("\n");

        // append nodes
        DatabaseNode node = database.getRoot();
        exportAppend(database, node, buffer);

        String text = buffer.toString();
        return text;
    }

    private void exportAppend(Database database, DatabaseNode node, StringBuilder buffer) throws ConversionException
    {
        // TODO ignore remote sync
        if (!node.isRoot())
        {
            // collect data
            // -- parent
            DatabaseNode parent = node.getParent();

            String parentId;
            String parentName;

            if (!parent.isRoot())
            {
                parentId = parent.getId();
                parentName = parent.getName();

                if (parentName == null)
                {
                    parentName = "";
                }
            }
            else
            {
                parentId = "root";
                parentName = "root";
            }

            // node
            String id = node.getId();
            String name = node.getName();

            if (name == null || name.isEmpty())
            {
                name = "(unnamed)";
            }

            String value;
            try
            {
                value = encryptedValueService.asString(database, node.getValue());
            }
            catch (Exception e)
            {
                throw new ConversionException("Failed to encrypt password - name: " + name, e);
            }

            // escape values
            parentId = escape(parentId);
            parentName = escape(parentName);
            id = escape(id);
            name = escape(name);
            value = escape(value);

            // append data
            buffer.append(parentId).append(",")
                    .append(parentName).append(",")
                    .append(id).append(",")
                    .append(name).append(",")
                    .append(value).append("\n");
        }

        // iterate children
        DatabaseNode[] children = node.getChildren();

        if (children != null)
        {
            for (DatabaseNode child : children)
            {
                exportAppend(database, child, buffer);
            }
        }
    }

    /* https://tools.ietf.org/html/rfc4180 */
    private String escape(String value)
    {
        if (value != null)
        {
            // determine if we have naughty chars
            if (value.contains(",") || value.contains("\n") || value.contains("\r") || value.contains("\""))
            {
                // replace quotes with double quotes
                value = value.replace("\"", "\"\"");

                // wrap value in quotes
                value = "\"" + value + "\"";
            }
        }
        else
        {
            value = "";
        }

        return value;
    }

    /* matrix of lines/rows by columns */
    String[][] parse(String text)
    {
        int colStart = 0;

        int pos = -1;
        int len = text.length();
        boolean insideQuote = false;

        List<String[]> rows = new LinkedList<>();
        List<String> cols = new LinkedList<>();

        // scan until we find end of row
        while (++pos < len)
        {
            char c = text.charAt(pos);

            if (c == '\"')
            {
                if (pos + 2 < len && text.charAt(pos+1) == '\"')
                {
                    // double quote, skip past it
                    pos++;
                }
                else
                {
                    // flip being inside a quote
                    insideQuote = !insideQuote;
                }

            }
            else if (c == ',' && !insideQuote)
            {
                String column;

                // check whether column is quoted
                if (text.charAt(colStart) == '\"')
                {
                    if (pos - colStart < 3)
                    {
                        // empty column
                        column = "";
                    }
                    else
                    {
                        // quoted value
                        column = text.substring(colStart + 1, pos - 1);

                        // -- replace quote escaping
                        column = column.replace("\"\"", "\"");
                    }
                }
                else if (pos - 1 == colStart)
                {
                    // empty value i.e. two commas next to each other
                    column = "";
                }
                else
                {
                    // unquoted value
                    column = text.substring(colStart, pos);
                }

                // add column
                cols.add(column);

                // reset col position to next position
                colStart = pos + 1;
            }
            else if ((c == '\n' && !insideQuote) || pos + 1 == len)
            {
                // check if we captured an empty column
                if (colStart == pos)
                {
                    // add empty column
                    cols.add("");
                }

                // looks like end of the row; reset col pos to next position
                colStart = pos + 1;

                // add columns as row
                String[] row = cols.toArray(new String[cols.size()]);
                rows.add(row);
            }
        }

        // build final matrix
        String[][] matrix = rows.toArray(new String[rows.size()][]);
        return matrix;
    }

}
