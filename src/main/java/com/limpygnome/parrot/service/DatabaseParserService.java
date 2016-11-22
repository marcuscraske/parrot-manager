package com.limpygnome.parrot.service;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.model.Database;
import com.limpygnome.parrot.model.node.DatabaseNode;
import com.limpygnome.parrot.model.node.EncryptedAesValue;
import com.limpygnome.parrot.model.params.CryptoParams;
import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.crypto.SecretKey;
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
 *     salt: base64 string of salt byte data,
 *     rounds: integer,
 *     children: [ node, ... ]
 * }
 *
 * JSON structure of a node, which is recursive:
 *
 * {
 *     name: string,
 *     modified: long (epoch) (used for versioning),
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

    public Database create(CryptoParams memoryCryptoParams, CryptoParams fileCryptoParams) throws Exception
    {
        Database database = new Database(controller, memoryCryptoParams, fileCryptoParams);
        return database;
    }

    public synchronized Database open(Controller controller, String path, char[] password) throws Exception
    {
        byte[] encryptedData = Files.readAllBytes(new File(path).toPath());
        Database database = openFileEncrypted(controller, encryptedData, password);
        return database;
    }

    public Database openFileEncrypted(Controller controller, byte[] encryptedData, char[] password) throws Exception
    {
        // Convert to JSON
        String encryptedText = new String(encryptedData, "UTF-8");

        JSONParser jsonParser = new JSONParser();
        JSONObject json = (JSONObject) jsonParser.parse(encryptedText);

        // Read params to decrypt database
        CryptoParams fileCryptoParams = CryptoParams.parse(controller, json, password);

        byte[] iv = Base64.decode((String) json.get("iv"));
        byte[] data = Base64.decode((String) json.get("data"));

        // Decrypt it...
        CryptographyService cryptographyService = controller.getCryptographyService();
        SecretKey secretKey = cryptographyService.createSecretKey(password, fileCryptoParams.getSalt(), fileCryptoParams.getRounds());
        byte[] decryptedData = cryptographyService.decrypt(secretKey, new EncryptedAesValue(iv, data));

        // Now open as memory encrypted database
        Database database = openMemoryEncrypted(decryptedData, password, fileCryptoParams);
        return database;
    }

    public Database openMemoryEncrypted(byte[] rawData, char[] password, CryptoParams fileCryptoParams) throws Exception
    {
        // Convert to text
        String text = new String(rawData, "UTF-8");

        // Parse as JSON object
        JSONParser jsonParser = new JSONParser();
        JSONObject json = (JSONObject) jsonParser.parse(text);

        // Read params
        CryptoParams memoryCryptoParams = CryptoParams.parse(controller, json, password);

        // Setup database
        Database database = new Database(controller, memoryCryptoParams, fileCryptoParams);

        // Traverse and parse node structure
        DatabaseNode root = database.getRoot();
        JSONObject jsonRoot = (JSONObject) json.get("root");
        convertJsonToNode(database, root, jsonRoot);

        // Set root node of DB
        database.setRoot(root);

        return database;
    }

    private void convertJsonToNode(Database database, DatabaseNode nodeParent, JSONObject jsonNode) throws Exception
    {
        // Read current node - skip if not defined; expected on initial read
        DatabaseNode child;

        if (jsonNode.containsKey("name") && jsonNode.containsKey("iv") && jsonNode.containsKey("data"))
        {
            String name = (String) jsonNode.get("name");
            long lastModified = (long) jsonNode.get("lastModified");
            byte[] iv = Base64.decode((String) jsonNode.get("iv"));
            byte[] data = Base64.decode((String) jsonNode.get("data"));

            // Create new DB node
            EncryptedAesValue encryptedData = new EncryptedAesValue(iv, data);
            child = new DatabaseNode(database, name, lastModified, encryptedData);

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

    private void convertNodeToJson(DatabaseNode node, JSONObject jsonRoot)
    {
        // Create new JSON object
        JSONObject jsonChild = new JSONObject();
        jsonChild.put("name", node.getName());
        jsonChild.put("lastModified", node.get)

        // Recurse child nodes
        for (DatabaseNode child : node.getChildren())
        {
            convertNodeToJson(node, jsonChild);
        }
    }

    public byte[] saveMemoryEncrypted(Controller controller, Database database) throws Exception
    {
        // Convert to JSON object
        JSONObject root = new JSONObject();

        // Convert JSON object to bytes
    }

    public byte[] saveFileEncrypted(Controller controller, Database database) throws Exception
    {
        // Save as memory encrypted

        // Apply file encryption
    }

    public void save(Controller controller, Database database, String path) throws Exception
    {
        // Save as file encrypted

        // Write to path
    }

    /**
     * Merges two databases together.
     *
     * The current strategy is to open both databases and copy anything missing from the source to the destination.
     * In the event the same item exists, the version with the latest modified timestamp is retained.
     *
     * The timestamp only affects an individual node and not its children.
     *
     * @param source the database being merged into the destination
     * @param destination the database to have the final version of everything
     */
    public void merge(Controller controller, Database source, Database destination)
    {
    }


}
