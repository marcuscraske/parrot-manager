package com.limpygnome.parrot.library.io.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.limpygnome.parrot.library.crypto.CryptoParams;
import com.limpygnome.parrot.library.crypto.CryptoParamsFactory;
import com.limpygnome.parrot.library.crypto.CryptoReaderWriter;
import com.limpygnome.parrot.library.crypto.EncryptedAesValue;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import com.limpygnome.parrot.library.db.Database;
import com.limpygnome.parrot.library.db.DatabaseNode;
import com.limpygnome.parrot.library.io.DatabaseReaderWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;

import java.io.File;
import java.nio.file.Files;
import java.util.HashSet;
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
    private static final Logger LOG = LogManager.getLogger(DatabaseJsonReaderWriter.class);

    private CryptoReaderWriter cryptoReaderWriter;
    private CryptoParamsFactory cryptoParamsFactory;

    // JSON readers/writers
    private CryptoParamsJsonReaderWriter cryptoParamsJsonReaderWriter;
    private EncryptedValueJsonReaderWriter encryptedValueJsonReaderWriter;
    private DatabaseNodeHistoryReaderWriter databaseNodeHistoryReaderWriter;

    /**
     * Creates an instance.
     */
    public DatabaseJsonReaderWriter()
    {
        this.cryptoReaderWriter = new CryptoReaderWriter();
        this.cryptoParamsFactory = new CryptoParamsFactory();
        this.cryptoParamsJsonReaderWriter = new CryptoParamsJsonReaderWriter(cryptoParamsFactory);
        this.encryptedValueJsonReaderWriter = new EncryptedValueJsonReaderWriter();
        this.databaseNodeHistoryReaderWriter = new DatabaseNodeHistoryReaderWriter(encryptedValueJsonReaderWriter);
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

        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(encryptedText).getAsJsonObject();

        // Read params to decrypt database
        CryptoParams fileCryptoParams = cryptoParamsJsonReaderWriter.parse(json, password);

        byte[] iv = Base64.decode(json.get("iv").getAsString());
        byte[] data = Base64.decode(json.get("data").getAsString());

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
        JsonParser jsonParser = new JsonParser();
        JsonObject json;

        try
        {
            json = jsonParser.parse(text).getAsJsonObject();
        }
        catch (Exception e)
        {
            // Add some more useful debugging logging
            LOG.debug("JSON text being parsed: " + text);

            // Re-throw exception...
            throw e;
        }

        // Read params
        CryptoParams memoryCryptoParams = cryptoParamsJsonReaderWriter.parse(json, password);

        // Setup database
        Database database = new Database(memoryCryptoParams, fileCryptoParams);

        // Traverse and parse db structure
        DatabaseNode root = database.getRoot();
        readDatabaseNode(database, root, json, true);

        // Set root db of DB
        database.setRoot(root);

        // Unset dirty flag, as we've just loaded it...
        database.setDirty(false);

        return database;
    }

    private void readDatabaseNode(Database database, DatabaseNode nodeParent, JsonObject jsonNode, boolean isRootNode) throws Exception
    {
        // Read current db - skip if not defined; expected on initial read
        DatabaseNode child;
        UUID id;

        // Parse list of deleted children
        Set<UUID> deletedChildren;

        if (jsonNode.has("deleted"))
        {
            // Parse deleted children
            JsonArray jsonDeleted = jsonNode.get("deleted").getAsJsonArray();
            deletedChildren = new HashSet<>(jsonDeleted.size());

            for (JsonElement rawId : jsonDeleted)
            {
                id = UUID.fromString(rawId.getAsString());
                deletedChildren.add(id);;
            }
        }
        else
        {
            deletedChildren = new HashSet<>(0);
        }

        // Parse actual ID of new db
        id = UUID.fromString(jsonNode.get("id").getAsString());

        // Add new child to parent if not root
        if (!isRootNode)
        {
            // Read values
            String name = !jsonNode.get("name").isJsonNull() ? jsonNode.get("name").getAsString() : null;
            long lastModified = jsonNode.get("modified").getAsLong();

            EncryptedValue encryptedValue = encryptedValueJsonReaderWriter.read(jsonNode);

            // Create new node
            child = new DatabaseNode(database, id, name, lastModified, encryptedValue);
            child.getDeletedChildren().addAll(deletedChildren);

            // -- History
            databaseNodeHistoryReaderWriter.read(jsonNode, child.getHistory());

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
        if (jsonNode.has("children"))
        {
            JsonArray jsonChildren = jsonNode.get("children").getAsJsonArray();

            JsonObject jsonChild;
            for (JsonElement rawChild : jsonChildren)
            {
                jsonChild = rawChild.getAsJsonObject();
                readDatabaseNode(database, child, jsonChild, false);
            }
        }
    }

    private void writeDatabaseNode(DatabaseNode node, JsonObject jsonRoot, boolean isRootNode)
    {
        JsonObject jsonChild;

        // Build list of deleted IDs
        JsonArray jsonDeleted = new JsonArray();
        for (UUID id : node.getDeletedChildren())
        {
            jsonDeleted.add(id.toString());
        }

        // Build child to append to root (if not root)...
        if (!isRootNode)
        {
            // Create new JSON object
            jsonChild = new JsonObject();

            jsonChild.addProperty("id", node.getId());
            jsonChild.addProperty("name", node.getName());
            jsonChild.addProperty("modified", node.getLastModified());
            jsonChild.add("deleted", jsonDeleted);

            // -- value
            EncryptedValue encryptedValue = node.getValue();
            encryptedValueJsonReaderWriter.write(jsonChild, encryptedValue);

            // -- history
            databaseNodeHistoryReaderWriter.write(jsonChild, node.getHistory());

            // Add to parent
            JsonArray rootChildren;

            if (!jsonRoot.has("children"))
            {
                rootChildren = new JsonArray();
                jsonRoot.add("children", rootChildren);
            }
            else
            {
                rootChildren = jsonRoot.get("children").getAsJsonArray();
            }

            rootChildren.add(jsonChild);
        }
        else
        {
            jsonRoot.addProperty("id", node.getId());
            jsonRoot.add("deleted", jsonDeleted);
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

        JsonObject jsonRoot = new JsonObject();
        cryptoParamsJsonReaderWriter.write(jsonRoot, memoryCryptoParams);

        writeDatabaseNode(rootNode, jsonRoot, true);

        // Convert JSON object to bytes
        String jsonMemoryEncrypted = jsonRoot.toString();
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
        JsonObject jsonFileEncrypted = new JsonObject();

        cryptoParamsJsonReaderWriter.write(jsonFileEncrypted, fileCryptoParams);

        jsonFileEncrypted.addProperty("iv", Base64.toBase64String(fileEncrypted.getIv()));
        jsonFileEncrypted.addProperty("data", Base64.toBase64String(fileEncrypted.getValue()));

        // Convert JSON wrapper to bytes
        String fileEncryptedStr = jsonFileEncrypted.toString();
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

}
