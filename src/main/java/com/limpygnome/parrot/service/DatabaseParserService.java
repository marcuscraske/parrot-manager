package com.limpygnome.parrot.service;

import com.limpygnome.parrot.model.Database;
import com.limpygnome.parrot.model.node.DatabaseNode;
import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Used for parsing instances of databases. This acts as the layer for reading and writing actual instances of
 * {@link com.limpygnome.parrot.model.Database}.
 *
 * The current database, being operated upon, is stored in {@link DatabaseService}.
 */
public class DatabaseParserService
{

    public Database create(char[] password, int rounds) throws Exception
    {
        Database database = new Database(password, rounds);
        return database;
    }

//    public synchronized Database open(String path, char[] password) throws Exception
//    {
//    }

    public Database open(byte[] rawData, char[] password) throws Exception
    {
        // Convert to text
        String text = new String(rawData, "UTF-8");

        // Parse as JSON object
        JSONParser jsonParser = new JSONParser();
        JSONObject json = (JSONObject) jsonParser.parse(text);

        // Read DB params
        String saltStr = (String) json.get("salt");
        byte[] salt = Base64.decode(saltStr);
        int rounds = (int) json.get("rounds");

        // Setup database
        Database database = new Database(salt, password, rounds);

        // Traverse and parse node structure
        DatabaseNode root = new DatabaseNode(database, null, null);
        JSONObject jsonRoot = (JSONObject) json.get("root");
        convertFromJson(root, jsonRoot);

        // Set root node of DB
        database.setRoot(root);

        return database;
    }

    private void convertFromJson(DatabaseNode node, JSONObject jsonNode)
    {

    }

    public byte[] save() throws Exception
    {
        // Convert to JSON object

        // Fetch as string and convert to bytes
    }

//    public void save(String path) throws Exception
//    {
//    }

    /**
     * Merges two databases together.
     *
     * @param source
     * @param destination
     */
    public void merge(Database source, Database destination)
    {
    }


}
