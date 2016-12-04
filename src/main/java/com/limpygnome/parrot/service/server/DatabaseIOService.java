package com.limpygnome.parrot.service.server;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.model.db.Database;
import com.limpygnome.parrot.model.dbaction.Action;
import com.limpygnome.parrot.model.dbaction.ActionsLog;
import com.limpygnome.parrot.model.db.DatabaseNode;
import com.limpygnome.parrot.model.db.EncryptedAesValue;
import com.limpygnome.parrot.model.params.CryptoParams;
import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.crypto.SecretKey;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Used for parsing instances of databases. This acts as the layer for reading and writing actual instances of
 * {@link Database}.
 *
 * The current database, being operated upon, is stored in {@link DatabaseService}.
 *
 * File (Encrypted) JSON Structure
 * -----------------------------------
 * The database is initially stored as followed, which is referred to as "encrypted":
 * {
 *     cryptoParams.salt: base64 string of salt byte data used to encrypt 'data',
 *     cryptoParams.rounds: integer,
 *     cryptoParams.modified: long (epoch) (versioning of crypto params)
 *     iv: base64 string of byte-array
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
 *     cryptoParams.salt: base64 string of salt byte data,
 *     cryptoParams.rounds: integer,
 *     cryptoParams.modified: long (epoch) (versioning of crypto params)
 *     children: [ db, ... ]
 * }
 *
 * JSON structure of a db, which is recursive:
 *
 * {
 *     id: uuid (String) (unique identifier)
 *     name: string,
 *     modified: long (epoch) (used for versioning),
 *     iv: base64 string of byte-array,
 *     data: base64 string of (encrypted) byte-array,
 *     children: [ db, ... ],
 *     deleted: [ list of uuid (string), ... ]
 * }
 */
public class DatabaseIOService
{
    private Controller controller;

    public DatabaseIOService(Controller controller)
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

        // Traverse and parse db structure
        DatabaseNode root = database.getRoot();
        convertJsonToNode(database, root, json, true);

        // Set root db of DB
        database.setRoot(root);

        return database;
    }

    private void convertJsonToNode(Database database, DatabaseNode nodeParent, JSONObject jsonNode, boolean isRootNode) throws Exception
    {
        // Read current db - skip if not defined; expected on initial read
        DatabaseNode child;

        UUID id;

        // Parse list of deleted children
        Set<UUID> deletedChildren;

        if (jsonNode.containsKey("deleted"))
        {
            // Parse deleted children
            JSONArray jsonDeleted = (JSONArray) jsonNode.get("deleted");
            deletedChildren = new HashSet<>(jsonDeleted.size());

            for (Object rawId : jsonDeleted)
            {
                id = UUID.fromString((String) rawId);
                deletedChildren.add(id);;
            }
        }
        else
        {
            deletedChildren = new HashSet<>(0);
        }

        // Parse actual ID of new db
        id = UUID.fromString((String) jsonNode.get("id"));

        // Add new child to parent if not root
        if (!isRootNode)
        {
            String name = (String) jsonNode.get("name");
            long lastModified = (long) jsonNode.get("modified");
            byte[] iv = Base64.decode((String) jsonNode.get("iv"));
            byte[] data = Base64.decode((String) jsonNode.get("data"));

            // Create new DB db
            EncryptedAesValue encryptedData = new EncryptedAesValue(iv, data);
            child = new DatabaseNode(database, id, name, lastModified, encryptedData);
            child.getDeletedChildren().addAll(deletedChildren);

            // Append to current parent
            nodeParent.getChildren().put(id, child);
        }
        else
        {
            nodeParent.setId(id);
            nodeParent.getDeletedChildren().addAll(deletedChildren);
            child = nodeParent;
        }

        // Recurse children
        if (jsonNode.containsKey("children"))
        {
            JSONArray jsonChildren = (JSONArray) jsonNode.get("children");
            JSONObject jsonChild;

            for (Object rawChild : jsonChildren)
            {
                jsonChild = (JSONObject) rawChild;
                convertJsonToNode(database, child, jsonChild, false);
            }
        }
    }

    private void convertNodeToJson(DatabaseNode node, JSONObject jsonRoot, boolean isRootNode)
    {
        JSONObject jsonChild;

        // Build list of deleted IDs
        JSONArray jsonDeleted = new JSONArray();
        for (UUID id : node.getDeletedChildren())
        {
            jsonDeleted.add(id.toString());
        }

        // Build child to append to root (if not root)...
        if (!isRootNode)
        {
            // Create new JSON object
            jsonChild = new JSONObject();

            jsonChild.put("id", node.getId().toString());
            jsonChild.put("name", node.getName());
            jsonChild.put("modified", node.getLastModified());

            EncryptedAesValue encryptedValue = node.getValue();
            if (encryptedValue != null) {
                String ivStr = Base64.toBase64String(node.getValue().getIv());
                String dataStr = Base64.toBase64String(node.getValue().getValue());
                jsonChild.put("iv", ivStr);
                jsonChild.put("data", dataStr);
            }

            jsonChild.put("deleted", jsonDeleted);

            // Add to parent
            if (!jsonRoot.containsKey("children")) {
                jsonRoot.put("children", new JSONArray());
            }

            JSONArray rootChildren = (JSONArray) jsonRoot.get("children");
            rootChildren.add(jsonChild);
        }
        else
        {
            jsonRoot.put("id", node.getId().toString());
            jsonRoot.put("deleted", jsonDeleted);
            jsonChild = jsonRoot;
        }

        // Recurse child nodes
        for (DatabaseNode child : node.getChildren().values())
        {
            convertNodeToJson(child, jsonChild, false);
        }
    }

    public byte[] saveMemoryEncrypted(Controller controller, Database database) throws Exception
    {
        // Convert to JSON object
        DatabaseNode rootNode = database.getRoot();
        CryptoParams memoryCryptoParams = database.getMemoryCryptoParams();

        JSONObject jsonRoot = new JSONObject();
        memoryCryptoParams.write(jsonRoot);

        convertNodeToJson(rootNode, jsonRoot, true);

        // Convert JSON object to bytes
        String jsonMemoryEncrypted = jsonRoot.toJSONString();
        byte[] result = jsonMemoryEncrypted.getBytes("UTF-8");
        return result;
    }

    public byte[] saveFileEncrypted(Controller controller, Database database) throws Exception
    {
        // Save as memory encrypted
        byte[] memoryEncrypted = saveMemoryEncrypted(controller, database);

        // Apply file encryption
        CryptoParams fileCryptoParams = database.getFileCryptoParams();
        EncryptedAesValue fileEncrypted = controller.getCryptographyService().encrypt(fileCryptoParams.getSecretKey(), memoryEncrypted);

        // Build JSON wrapper
        JSONObject jsonFileEncrypted = new JSONObject();

        fileCryptoParams.write(jsonFileEncrypted);

        jsonFileEncrypted.put("iv", Base64.toBase64String(fileEncrypted.getIv()));
        jsonFileEncrypted.put("data", Base64.toBase64String(fileEncrypted.getValue()));

        // Convert JSON wrapper to bytes
        String fileEncryptedStr = jsonFileEncrypted.toJSONString();
        byte[] result = fileEncryptedStr.getBytes("UTF-8");
        return result;
    }

    public void save(Controller controller, Database database, String path) throws Exception
    {
        // Save as file encrypted
        byte[] fileEncrypted = saveFileEncrypted(controller, database);

        // Write to path
        Files.write(new File(path).toPath(), fileEncrypted, StandardOpenOption.CREATE);
    }

    /**
     * Merges source to destination.
     *
     * This will only update destination, hence two calls are needed for merging both ways. This is since the
     * destination database should be the final result for both.
     *
     * @param source the database being merged into the destination
     * @param destination the database to have the final version of everything
     * @param password the password for the destination database
     * @return a log of actions performed on the database
     * @throws Exception if a crypto operation fails
     */
    public ActionsLog merge(Database source, Database destination, char[] password) throws Exception
    {
        ActionsLog actionsLog = new ActionsLog();

        // Check if databases are the same, skip if so...
        if (destination.equals(source))
        {
            actionsLog.add(new Action("No changes detected"));
        }
        else
        {
            actionsLog.add(new Action("Changes detected, merging..."));

            destination.merge(actionsLog, source, password);
        }

        return actionsLog;
    }

}
