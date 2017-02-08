package com.limpygnome.parrot.model.db;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.model.dbaction.Action;
import com.limpygnome.parrot.model.dbaction.ActionsLog;
import com.limpygnome.parrot.model.dbaction.MergeInfo;
import com.limpygnome.parrot.model.params.CryptoParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a database for storing confidential details.
 *
 * Simple tree structure with a root db, which breaks down into recursive child nodes.
 *
 * This is also responsible for all cryptography for the database.
 *
 * Thread safe.
 */
public final class Database
{
    private static final Logger LOG = LogManager.getLogger(Database.class);

    // Indicates if the database has been modified
    private boolean isDirty;

    // An instance of the current controller
    private Controller controller;

    // Params used for file crypto
    protected CryptoParams fileCryptoParams;

    // Params used for memory crypto
    protected CryptoParams memoryCryptoParams;

    // The root db of the database
    protected DatabaseNode root;

    // Quick ref map of UUID (String) to node; this must be updated in places where nodes are added/removed etc
    protected Map<UUID, DatabaseNode> lookup;

    /**
     * Creates a new instance.
     *
     * @param controller current instance
     * @param memoryCryptoParams params for in-memory crypto
     * @param fileCryptoParams params used for file crypto; only required for writing to file later, can be null
     */
    public Database(Controller controller, CryptoParams memoryCryptoParams, CryptoParams fileCryptoParams)
    {
        this.controller = controller;
        this.memoryCryptoParams = memoryCryptoParams;
        this.fileCryptoParams = fileCryptoParams;
        this.lookup = new HashMap<>();

        // Setup an initial blank root db
        root = new DatabaseNode(this, UUID.randomUUID(), null, 0, (EncryptedAesValue) null);
    }

    /**
     * @return the root db of this database
     */
    public DatabaseNode getRoot()
    {
        return root;
    }

    /**
     * Retrieves a node by its identifier.
     *
     * TODO: add tests
     *
     * @param rawUuid the id/uuid as raw text
     * @return the node, or null if not found or the id cannot be parsed as a uuid
     */
    public DatabaseNode getNode(String rawUuid)
    {
        DatabaseNode result;

        try
        {
            UUID uuid = UUID.fromString(rawUuid);
            result = getNode(uuid);
        }
        catch (IllegalArgumentException e)
        {
            result = null;
            LOG.warn("Failed to retrieve node due to invalid uuid - raw uuid: {}", rawUuid, e);
        }

        return result;
    }

    /**
     * Retrieves a node by its identifier.
     *
     * TODO: add tests
     *
     * @param uuid the node identifier
     * @return the node, or null if not found
     */
    public DatabaseNode getNode(UUID uuid)
    {
        DatabaseNode result = lookup.get(uuid);
        return result;
    }

    /**
     * Sets the root db of this database.
     *
     * @param node the db to become root
     */
    public synchronized void setRoot(DatabaseNode node)
    {
        this.root = node;
    }

    /**
     * Performs memory encryption on data.
     *
     * Intended for encrypting data for nodes.
     *
     * @param data the data to be encrypted
     * @return the encrypted wrapper
     * @throws Exception
     */
    public synchronized EncryptedAesValue encrypt(byte[] data) throws Exception
    {
        EncryptedAesValue value = controller.getCryptographyService().encrypt(memoryCryptoParams.getSecretKey(), data);
        return value;
    }

    /**
     * Performs memory decryption.
     *
     * Intended for decrypting data for nodes.
     *
     * @param data the encrypted wrapper
     * @return the decrypted data
     * @throws Exception if crypto fails
     */
    public synchronized byte[] decrypt(EncryptedAesValue data) throws Exception
    {
        byte[] result = decrypt(data, memoryCryptoParams);
        return result;
    }

    /**
     * Performs memory decryption.
     *
     * Intended for decrypting data for nodes.
     *
     * @param data the encrypted wrapper
     * @param memoryCryptoParams the crypto params; allows using params not held in the database
     * @return the decrypted data
     * @throws Exception if crypto fails
     */
    public synchronized byte[] decrypt(EncryptedAesValue data, CryptoParams memoryCryptoParams) throws Exception
    {
        byte[] value = controller.getCryptographyService().decrypt(memoryCryptoParams.getSecretKey(), data);
        return value;
    }

    /**
     * @return params used for file crypto; can be null if this DB is not written to file
     */
    public CryptoParams getFileCryptoParams()
    {
        return fileCryptoParams;
    }

    /**
     * Updates the file crypto params for this database.
     *
     * The passed instance is cloned.
     *
     * @param controller the current runtime controller
     * @param fileCryptoParams the new params
     * @param password the current password
     * @throws Exception if crypto fails
     */
    public void updateFileCryptoParams(Controller controller, CryptoParams fileCryptoParams, char[] password) throws Exception
    {
        // Update params
        this.fileCryptoParams = new CryptoParams(
                controller, password, fileCryptoParams.getSalt(), fileCryptoParams.getRounds(), fileCryptoParams.getLastModified());

        // No more actions required, as data is written during I/O...
    }

    /**
     * Updates the memory crypto params for this database.
     *
     * The passed instance is cloned.
     *
     * WARNING: this will re-encrypt all in-memory data.
     *
     * @param controller the current runtime controller
     * @param memoryCryptoParams the new params
     * @param password the current password
     * @throws Exception if crypto fails
     */
    public synchronized void updateMemoryCryptoParams(Controller controller, CryptoParams memoryCryptoParams, char[] password) throws Exception
    {
        // Keep ref of current params, we'll need it to re-encrypt
        CryptoParams oldMemoryCryptoParams = this.memoryCryptoParams;

        // Update to use new params
        this.memoryCryptoParams = new CryptoParams(
                controller, password, memoryCryptoParams.getSalt(), memoryCryptoParams.getRounds(), memoryCryptoParams.getLastModified());

        // Re-encrypt in-memory data...
        root.rebuildCrypto(oldMemoryCryptoParams);
    }


    /**
     * @return params used for in-memory crypto
     */
    public CryptoParams getMemoryCryptoParams()
    {
        return memoryCryptoParams;
    }

    /**
     * Merges the two databases together.
     *
     * @param actionsLog used to log any actions/changes to the database during the merge
     * @param database the other database to be merged with this database
     * @param password the password for the current database
     * @throws Exception when unable to merge
     */
    public synchronized void merge(ActionsLog actionsLog, Database database, char[] password) throws Exception
    {
        // Merge params
        if (fileCryptoParams.getLastModified() < database.fileCryptoParams.getLastModified()) {
            updateFileCryptoParams(controller, database.fileCryptoParams, password);
            actionsLog.add(new Action("updated file crypto parameters"));
        }

        if (memoryCryptoParams.getLastModified() < database.memoryCryptoParams.getLastModified()) {
            updateMemoryCryptoParams(controller, database.memoryCryptoParams, password);
            actionsLog.add(new Action("updated memory crypto parameters"));
        }

        // Merge nodes
        root.merge(new MergeInfo(actionsLog, database.root), database.root);
    }

    /**
     * @param dirty sets whether database is dirty; true = dirty/changed, false = unchanged/saved
     */
    public void setDirty(boolean dirty)
    {
        isDirty = dirty;
    }

    /**
     * @return true = dirty/modified, false = unchanged
     */
    public boolean isDirty()
    {
        return isDirty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Database database = (Database) o;

        if (fileCryptoParams != null ? !fileCryptoParams.equals(database.fileCryptoParams) : database.fileCryptoParams != null)
            return false;
        if (memoryCryptoParams != null ? !memoryCryptoParams.equals(database.memoryCryptoParams) : database.memoryCryptoParams != null)
            return false;
        return root != null ? root.equals(database.root) : database.root == null;

    }

    @Override
    public int hashCode()
    {
        int result = fileCryptoParams != null ? fileCryptoParams.hashCode() : 0;
        result = 31 * result + (memoryCryptoParams != null ? memoryCryptoParams.hashCode() : 0);
        result = 31 * result + (root != null ? root.hashCode() : 0);
        return result;
    }

}
