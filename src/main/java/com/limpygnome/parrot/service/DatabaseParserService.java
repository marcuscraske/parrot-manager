package com.limpygnome.parrot.service;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.model.Database;
import com.limpygnome.parrot.model.node.DatabaseNode;
import com.limpygnome.parrot.model.node.EncryptedAesValue;
import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;

/**
 * Used for parsing instances of databases. This acts as the layer for reading and writing actual instances of
 * {@link com.limpygnome.parrot.model.Database}.
 *
 * The current database, being operated upon, is stored in {@link DatabaseService}.
 *
 * File (Encrypted) JSON Structure
 * -----------------------------------
 * The database is initially stored as followed, which is referred to as "encrypted":
 * {
 *     salt: base64 string of salt byte data used to encrypt 'data',
 *     rounds: integer,
 *     data: base64 string of memory (encrypted) JSON structure (see below for structure),
 * }
 *
 * This is only for when the database is persisted to disk. The attribute 'data' must be decrypted before it can be
 * used.
 *
 *
 * Memory (Encrypted) JSON Structure
 * -----------------------------------
 * The database stored in memory, once loaded from a persistent store, uses the following JSON structure:
 * {
 *     salt: base64 string of salt byte data,-
 *     rounds: integer,
 *     children: [ node, ... ]
 * }
 *
 * JSON structure of a node, which is recursive:
 *
 * {
 *     name: string,
 *     iv: base64 string of byte-array,
 *     data: base64 string of (encrypted) byte-array,
 *     children: [ node, ... ]
 * }
 */
public class DatabaseParserService
{
    private Controller controller;

    public DatabaseParserService(Controller controller)
    {
        this.controller = controller;
    }

    public Database create(char[] password, int rounds) throws Exception
    {
        Database database = new Database(controller, password, rounds);
        return database;
    }

    public synchronized Database open(String path, char[] password) throws Exception
    {
        byte[] encryptedData = Files.readAllBytes(new File(path).toPath());
        Database database = openFileEncrypted(encryptedData, password);
        return database;
    }

    public Database openFileEncrypted(Controller controller, byte[] encryptedData, char[] password) throws Exception
    {
        // Convert to JSON
        String encryptedText = new String(encryptedData, "UTF-8");

        JSONParser jsonParser = new JSONParser();
        JSONObject json = (JSONObject) jsonParser.parse(encryptedText);

        // Read params to decrypt database
        byte[] salt = Base64.decode((String) json.get("salt"));
        int rounds = (int) json.get("rounds");
        byte[] iv = Base64.decode((String) json.get("iv"));
        byte[] data = Base64.decode((String) json.get("data"));

        // Decrypt it...
        CryptographyService cryptographyService = controller.getCryptographyService();
        SecretKey secretKey = cryptographyService.createSecretKey(password, salt, rounds);
        byte[] decryptedData = cryptographyService.decrypt(secretKey, new EncryptedAesValue(iv, data));

        // Now open as memory ecnrypted database
        Database database = openMemoryEncrypted(decryptedData, password);
        return database;
    }

    public Database openMemoryEncrypted(byte[] rawData, char[] password) throws Exception
    {
        // Convert to text
        String text = new String(rawData, "UTF-8");

        // Parse as JSON object
        JSONParser jsonParser = new JSONParser();
        JSONObject json = (JSONObject) jsonParser.parse(text);

        // Read DB params
        byte[] salt = Base64.decode((String) json.get("salt"));
        int rounds = (int) json.get("rounds");

        // Setup database
        Database database = new Database(controller, salt, password, rounds);

        // Traverse and parse node structure
        DatabaseNode root = new DatabaseNode(database, null, (EncryptedAesValue) null);
        JSONObject jsonRoot = (JSONObject) json.get("root");
        convertJsonToNode(database, root, jsonRoot);

        // Set root node of DB
        database.setRoot(root);

        return database;
    }

    private void convertJsonToNode(Database database, DatabaseNode nodeParent, JSONObject jsonNode)
    {
        // Read current node - skip if not defined; expected on initial read
        DatabaseNode child;

        if (jsonNode.containsKey("name") && jsonNode.containsKey("iv") && jsonNode.containsKey("data"))
        {
            String name = (String) jsonNode.get("name");
            byte[] iv = Base64.decode((String) jsonNode.get("iv"));
            byte[] data = Base64.decode((String) jsonNode.get("data"));

            // Create new DB node
            EncryptedAesValue encryptedData = new EncryptedAesValue(iv, data);
            child = new DatabaseNode(database, name, encryptedData);

            // Append to current parent
            nodeParent.children.add(child);
        }
        else
        {
            child = nodeParent;
        }

        // Recurse children
        if (jsonNode.containsKey("children"))
        {
            JSONArray jsonChildren = (JSONArray) jsonNode.get("children");
            JSONObject jsonChild;

            for (Object rawChild : jsonChildren) {
                jsonChild = (JSONObject) rawChild;
                convertJsonToNode(database, child, jsonChild);
            }
        }
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
