package com.limpygnome.parrot.library.io.json;

import com.limpygnome.parrot.library.crypto.CryptoParams;
import com.limpygnome.parrot.library.crypto.CryptoParamsFactory;
import com.limpygnome.parrot.library.crypto.CryptoReaderWriter;
import com.limpygnome.parrot.library.crypto.EncryptedAesValue;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.db.DatabaseNode;
import com.limpygnome.parrot.library.io.DatabaseReaderWriter;
import org.bouncycastle.util.encoders.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

/**
 * Used for reading and writing instances of {@link Database}.
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
public class DatabaseJsonReaderWriter implements DatabaseReaderWriter
{
    private CryptoReaderWriter cryptoReaderWriter;
    private CryptoParamsFactory cryptoParamsFactory;
    private EncryptedValueJsonReaderWriter encryptedValueJsonReaderWriter;

    /**
     * Creates an instance.
     */
    public DatabaseJsonReaderWriter()
    {
        this.cryptoReaderWriter = new CryptoReaderWriter();
        this.cryptoParamsFactory = new CryptoParamsFactory();
        this.encryptedValueJsonReaderWriter = new EncryptedValueJsonReaderWriter();
    }

    @Override
    public synchronized Database open(String path, char[] password) throws Exception
    {
        byte[] encryptedData = Files.readAllBytes(new File(path).toPath());
        Database database = openFileEncrypted(encryptedData, password);
        return database;
    }

    private Database openFileEncrypted(byte[] encryptedData, char[] password) throws Exception
    {
        // Convert to JSON
        String encryptedText = new String(encryptedData, "UTF-8");

        JSONParser jsonParser = new JSONParser();
        JSONObject json = (JSONObject) jsonParser.parse(encryptedText);

        // Read params to decrypt database
        CryptoParams fileCryptoParams = cryptoParamsFactory.parse(json, password);

        byte[] iv = Base64.decode((String) json.get("iv"));
        byte[] data = Base64.decode((String) json.get("data"));

        // Decrypt it...
        byte[] decryptedData = cryptoReaderWriter.decrypt(fileCryptoParams, new EncryptedAesValue(0, iv, data));

        // Now open as memory encrypted database
        Database database = openMemoryEncrypted(decryptedData, password, fileCryptoParams);
        return database;
    }

    private Database openMemoryEncrypted(byte[] rawData, char[] password, CryptoParams fileCryptoParams) throws Exception
    {
        // Convert to text
        String text = new String(rawData, "UTF-8");

        // Parse as JSON object
        JSONParser jsonParser = new JSONParser();
        JSONObject json = (JSONObject) jsonParser.parse(text);

        // Read params
        CryptoParams memoryCryptoParams = cryptoParamsFactory.parse(json, password);

        // Setup database
        Database database = new Database(memoryCryptoParams, fileCryptoParams);

        // Traverse and parse db structure
        DatabaseNode root = database.getRoot();
        readDatabaseNode(database, root, json, true);

        // Set root db of DB
        database.setRoot(root);

        // Unset dirty flag, as we've just loaded it...
        // TODO: add test
        database.setDirty(false);

        return database;
    }

    private void readDatabaseNode(Database database, DatabaseNode nodeParent, JSONObject jsonNode, boolean isRootNode) throws Exception
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
            // Read values
            String name = (String) jsonNode.get("name");
            long lastModified = (long) jsonNode.get("modified");

            EncryptedValue encryptedValue = encryptedValueJsonReaderWriter.read(jsonNode);

            // -- History
            LinkedList<EncryptedValue> history = new LinkedList<>();
            JSONArray jsonHistory = (JSONArray) jsonNode.get("history");

            if (jsonHistory != null)
            {
                EncryptedValue historicValue;
                JSONObject jsonHistoricValue;
                for (Object rawHistory : jsonHistory)
                {
                    jsonHistoricValue = (JSONObject) rawHistory;
                    historicValue = encryptedValueJsonReaderWriter.read(jsonHistoricValue);
                    history.add(historicValue);
                }
            }

            // Create new DB db
            child = new DatabaseNode(database, id, name, lastModified, encryptedValue);
            child.getDeletedChildren().addAll(deletedChildren);
            child.history().addAll(history);

            // Append to current parent
            nodeParent.add(child);
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
                readDatabaseNode(database, child, jsonChild, false);
            }
        }
    }

    private void writeDatabaseNode(DatabaseNode node, JSONObject jsonRoot, boolean isRootNode)
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

            jsonChild.put("id", node.getUuid().toString());
            jsonChild.put("name", node.getName());
            jsonChild.put("modified", node.getLastModified());
            jsonChild.put("deleted", jsonDeleted);

            EncryptedValue encryptedValue = node.getValue();
            encryptedValueJsonReaderWriter.write(jsonChild, encryptedValue);

            JSONArray jsonHistory = new JSONArray();
            JSONObject jsonHistoryItem;
            for (EncryptedValue historicValue : node.history().fetch())
            {
                jsonHistoryItem = new JSONObject();
                encryptedValueJsonReaderWriter.write(jsonHistoryItem, historicValue);
                jsonHistory.add(jsonHistoryItem);
            }
            jsonChild.put("history", jsonHistory);

            // Add to parent
            if (!jsonRoot.containsKey("children"))
            {
                jsonRoot.put("children", new JSONArray());
            }

            JSONArray rootChildren = (JSONArray) jsonRoot.get("children");
            rootChildren.add(jsonChild);
        }
        else
        {
            jsonRoot.put("id", node.getUuid().toString());
            jsonRoot.put("deleted", jsonDeleted);
            jsonChild = jsonRoot;
        }

        // Recurse child nodes
        for (DatabaseNode child : node.getChildren())
        {
            writeDatabaseNode(child, jsonChild, false);
        }
    }

    private byte[] saveMemoryEncrypted(Database database) throws Exception
    {
        // Convert to JSON object
        DatabaseNode rootNode = database.getRoot();
        CryptoParams memoryCryptoParams = database.getMemoryCryptoParams();

        JSONObject jsonRoot = new JSONObject();
        writeCryptoParams(jsonRoot, memoryCryptoParams);

        writeDatabaseNode(rootNode, jsonRoot, true);

        // Convert JSON object to bytes
        String jsonMemoryEncrypted = jsonRoot.toJSONString();
        byte[] result = jsonMemoryEncrypted.getBytes("UTF-8");
        return result;
    }

    private byte[] saveFileEncrypted(Database database) throws Exception
    {
        // Save as memory encrypted
        byte[] memoryEncrypted = saveMemoryEncrypted(database);

        // Apply file encryption
        CryptoParams fileCryptoParams = database.getFileCryptoParams();
        EncryptedAesValue fileEncrypted = (EncryptedAesValue) cryptoReaderWriter.encrypt(fileCryptoParams, memoryEncrypted);

        // Build JSON wrapper
        JSONObject jsonFileEncrypted = new JSONObject();

        writeCryptoParams(jsonFileEncrypted, fileCryptoParams);

        jsonFileEncrypted.put("iv", Base64.toBase64String(fileEncrypted.getIv()));
        jsonFileEncrypted.put("data", Base64.toBase64String(fileEncrypted.getValue()));

        // Convert JSON wrapper to bytes
        String fileEncryptedStr = jsonFileEncrypted.toJSONString();
        byte[] result = fileEncryptedStr.getBytes("UTF-8");
        return result;
    }

    @Override
    public void save(Database database, String path) throws Exception
    {
        save(database, new File(path));
    }

    @Override
    public void save(Database database, File file) throws Exception
    {
        // Save as file encrypted
        byte[] fileEncrypted = saveFileEncrypted(database);

        // Write to path
        Files.write(file.toPath(), fileEncrypted);
    }

    private void writeCryptoParams(JSONObject object, CryptoParams cryptoParams)
    {
        object.put("cryptoParams.salt", Base64.toBase64String(cryptoParams.getSalt()));
        object.put("cryptoParams.rounds", cryptoParams.getRounds());
        object.put("cryptoParams.modified", cryptoParams.getLastModified());
    }

}
