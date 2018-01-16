package com.limpygnome.parrot.converter.csv;

import com.limpygnome.parrot.converter.api.ConversionException;
import com.limpygnome.parrot.converter.api.Converter;
import com.limpygnome.parrot.converter.api.MalformedInputException;
import com.limpygnome.parrot.converter.api.Options;
import com.limpygnome.parrot.lib.database.EncryptedValueService;
import com.limpygnome.parrot.lib.io.StringStreamOperations;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.db.DatabaseNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component("csv")
public class CsvConverter extends Converter
{
    private static final Logger LOG = LoggerFactory.getLogger(CsvConverter.class);

    @Autowired
    private StringStreamOperations stringStreamOperations;
    @Autowired
    private EncryptedValueService encryptedValueService;

    @Override
    public String[] databaseImport(Database database, Options options, InputStream inputStream) throws ConversionException, MalformedInputException, IOException
    {
        String text = stringStreamOperations.readString(inputStream);
        return databaseImportText(database, options, text);
    }

    @Override
    public void databaseExport(Database database, Options options, OutputStream outputStream) throws ConversionException, IOException
    {
        String text = databaseExportText(database, options);
        stringStreamOperations.writeString(outputStream, text);
    }

    @Override
    public String[] databaseImportText(Database database, Options options, String text) throws ConversionException, MalformedInputException
    {
        if (text == null || text.isEmpty())
        {
            throw new ConversionException("Provided text is empty");
        }

        // parse as csx text
        List<String[]> rows = parse(text);

        if (rows.size() <= 1)
        {
            throw new ConversionException("Provided text has no data");
        }

        // convert each row into database node in database
        Database databaseParsed = createDatabase(database);

        String[] headers = rows.get(0);
        Map<String, DatabaseNode> parentNameToNode = new HashMap<>();

        // drop first row (headers)
        rows.remove(0);

        // scan for best column for determining parent
        int parentHeaderIndex = -1;
        boolean isParentId = false;

        for (int headerIndex = 0; headerIndex < headers.length && !isParentId; headerIndex++)
        {
            String header = headers[headerIndex].toLowerCase();
            switch (header)
            {
                case "parentid":
                    parentHeaderIndex = headerIndex;
                    isParentId = true;
                    break;
                case "parentname":
                case "group":
                    parentHeaderIndex = headerIndex;
                    break;
            }
        }

        if (parentHeaderIndex == -1)
        {
            throw new ConversionException("No header defines the parent/group, thus items cannot be mapped to a parent node/entry");
        }

        /*
            Since parents may be defined after children, we'll scan top to bottom and try to add the next available
            parent.

            This is not greatly efficient, but probably the most straight forward approach and expected data-set is
            small. In most cases this parents will be defined first, especially when exporting data from this same
            converter.
         */

        boolean anyImported;

        do
        {
            anyImported = false;

            for (int rowNum = 0; rowNum < rows.size(); rowNum++)
            {
                String[] row = rows.get(rowNum);
                if (importRow(databaseParsed, options, parentNameToNode, parentHeaderIndex, isParentId, headers, row, rowNum))
                {
                    rows.remove(rowNum);
                    anyImported = true;
                }
            }
        }
        while (!rows.isEmpty() && anyImported);

        // check whether there's orphaned data left over
        if (rows.size() > 1)
        {
            throw new ConversionException("Some data is orphaned / cannot be mapped to a parent - rows: " + rows.size());
        }

        // perform merge
        return merge(database, databaseParsed);
    }

    @Override
    public String databaseExportText(Database database, Options options) throws ConversionException
    {
        StringBuilder buffer = new StringBuilder();

        // append column names
        buffer.append("ParentId,ParentName,Id,Name,Value").append("\n");

        // append nodes
        DatabaseNode node = database.getRoot();
        exportAppend(database, node, buffer, options);

        String text = buffer.toString();
        return text;
    }

    private void exportAppend(Database database, DatabaseNode node, StringBuilder buffer, Options options) throws ConversionException
    {
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
                if (!isProhibitedNode(options, node))
                {
                    exportAppend(database, child, buffer, options);
                }
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

    private boolean importRow(Database databaseParsed, Options options, Map<String, DatabaseNode> parentNameToNode,
                              int parentHeaderIndex, boolean isParentId, String[] headers, String[] row, int rowNum) throws ConversionException
    {
        // check column count matches header
        if (row.length != headers.length)
        {
            throw new ConversionException("Number of columns does not match header - data row: " + (rowNum+1));
        }

        // create new node
        DatabaseNode node = new DatabaseNode(databaseParsed);

        // handle parent
        String parentValue = row[parentHeaderIndex];
        DatabaseNode parentNode;

        if ("root".equals(parentValue) || parentValue.isEmpty())
        {
            parentNode = databaseParsed.getRoot();
        }
        else if (isParentId)
        {
            try
            {
                UUID parentId = UUID.fromString(parentValue);
                parentNode = databaseParsed.getNodeByUuid(parentId);
            }
            catch (IllegalArgumentException e)
            {
                throw new ConversionException("Malformed parent identifier - value: " + parentValue, e);
            }
        }
        else
        {
            parentNode = parentNameToNode.get(parentValue);
        }

        // check we found parent, otherwise must be in future record
        if (parentNode == null)
        {
            return false;
        }

        // attach to parent
        parentNode.add(node);

        // parse properties
        for (int index = 0; index < headers.length; index++)
        {
            String header = headers[index].toLowerCase();
            String value = row[index];

            switch (header)
            {
                // Columns which natively map 1 to 1
                case "parentid":
                case "parentname":
                case "group":
                    // do nothing; parent already previously handled
                    break;
                case "id":
                    try
                    {
                        UUID nodeId = UUID.fromString(value);
                        node.setId(nodeId);
                    }
                    catch (IllegalArgumentException e)
                    {
                        throw new ConversionException("Invalid identifier for node - value: " + value, e);
                    }
                    break;
                case "name":
                case "title":
                    node.setName(value);
                    break;
                case "value":
                case "password":
                    try
                    {
                        EncryptedValue encryptedValue = encryptedValueService.fromString(databaseParsed, value);
                        node.setValue(encryptedValue);
                    }
                    catch (Exception e)
                    {
                        throw new ConversionException("Failed to encrypt value - " + e.getMessage(), e);
                    }
                    break;
                case "lastmodified":
                case "last modified":
                    try
                    {
                        long lastModified = Long.parseLong(value);
                        node.setLastModified(lastModified);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new ConversionException("Invalid value for last modified - parent: " + parentValue + ", value: " + value);
                    }
                    break;

                // Columns to be treated as sub-children (third-party password managers)
                case "user name":
                case "username":
                    importAddSubChild(databaseParsed, node, "User Name", value);
                    break;
                case "url":
                    importAddSubChild(databaseParsed, node, "URL", value);
                    break;
                case "notes":
                    importAddSubChild(databaseParsed, node, "Notes", value);
                    break;

                default:
                    LOG.warn("unhandled/ignored header - header: {}", header);
                    break;
            }
        }

        // drop and stop if prohibited node
        if (isProhibitedNode(options, node))
        {
            node.remove();
        }
        else if (!isParentId)
        {
            // add to map of names to nodes
            String nodeName = node.getName();

            if (nodeName != null)
            {
                if (parentNameToNode.containsKey(nodeName))
                {
                    throw new ConversionException("Cannot add node with identical name when parent nodes/identification is name based - name: " + nodeName);
                }
                parentNameToNode.put(nodeName, node);
            }
        }

        return true;
    }

    private void importAddSubChild(Database databaseParsed, DatabaseNode parent, String key, String value) throws ConversionException
    {
        if (value != null && value.length() > 0)
        {
            try
            {
                EncryptedValue encryptedValue = encryptedValueService.fromString(databaseParsed, value);

                DatabaseNode child = parent.addNew();
                child.setName(key);
                child.setValue(encryptedValue);
            }
            catch (Exception e)
            {
                throw new ConversionException("Failed to encrypt value for special child - parent: " + parent.getPath() + ", key: " + key, e);
            }
        }
    }

    /* matrix of lines/rows by columns */
    // TODO ignore empty or commented-out lines
    List<String[]> parse(String text)
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

            if ((c == '\n' && !insideQuote) || (pos + 1 == len))
            {
                // check if we captured an empty column
                if (colStart == pos)
                {
                    // add empty column
                    cols.add("");
                }
                else
                {
                    // include current char if end of text, otherwise exclude this char
                    String column = text.substring(colStart, pos + 1 == len ? pos + 1 : pos);
                    cols.add(column);
                }

                // looks like end of the row; reset col pos to next position
                colStart = pos + 1;

                // add columns as row
                String[] row = cols.toArray(new String[cols.size()]);
                rows.add(row);

                // reset columns
                cols.clear();
            }
        }

        return rows;
    }

}
