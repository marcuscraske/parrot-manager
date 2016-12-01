package com.limpygnome.parrot.model.db;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.model.params.CryptoParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseTest {

    private static final char[] PASSWORD = "test password".toCharArray();
    private static final long DEFAULT_LAST_MODIFIED = 1000L;
    private static final byte[] TEST_DATA = { 0x11, 0x22, 0x33, 0x44, 0x11, 0x22, 0x33 };

    // SUT
    private Database database;

    // Mock objects
    @Mock
    private DatabaseNode node;
    @Mock
    private DatabaseNode node2;

    // Objects
    private Controller controller;

    @Before
    public void setup() throws Exception
    {
        controller = new Controller();
        database = createDatabase(PASSWORD, DEFAULT_LAST_MODIFIED, DEFAULT_LAST_MODIFIED);
    }

    @Test
    public void encrypt_decrypt_whenGivenValue_thenCanGoInAndOut() throws Exception
    {
        // When
        EncryptedAesValue encrypted = database.encrypt(TEST_DATA);
        byte[] decrypted = database.decrypt(encrypted);

        // Then
        assertArrayEquals("Decrypted data should be same as original input", TEST_DATA, decrypted);
        assertTrue("Encrypted data should not be same as input", !TEST_DATA.equals(encrypted.getValue()));
    }

    @Test
    public void updateFileCryptoParams_whenChanged_thenHasDiffRef() throws Exception
    {
        // Given
        CryptoParams oldFileCryptoParams = database.fileCryptoParams;
        CryptoParams newFileCryptoParams = new CryptoParams(controller, "new pass".toCharArray(), 123, 0);
        long previousHash = System.identityHashCode(oldFileCryptoParams);

        // When
        database.updateFileCryptoParams(controller, newFileCryptoParams, PASSWORD);

        // Then
        assertTrue("Expected file crypto params to have changed from old", !oldFileCryptoParams.equals(database.fileCryptoParams));

        long newParamsHash = System.identityHashCode(newFileCryptoParams);
        long currentHash = System.identityHashCode(database.fileCryptoParams);
        assertTrue("Expected identity hash to not be the same as new params i.e. made new instance", currentHash != newParamsHash);
        assertTrue("Expected identity hash to be different to old params hash", currentHash != previousHash);
    }

    @Test
    public void updateMemoryCryptoParams_whenChanged_thenHasDiffRef() throws Exception
    {
        // Given
        CryptoParams oldMemoryCryptoParams = database.memoryCryptoParams;
        CryptoParams newMemoryCryptoParams = new CryptoParams(controller, "new pass".toCharArray(), 123, 0);
        long previousHash = System.identityHashCode(oldMemoryCryptoParams);

        // When
        database.updateMemoryCryptoParams(controller, newMemoryCryptoParams, PASSWORD);

        // Then
        assertTrue("Expected memory crypto params to have changed from old", !oldMemoryCryptoParams.equals(database.memoryCryptoParams));

        long newParamsHash = System.identityHashCode(newMemoryCryptoParams);
        long currentHash = System.identityHashCode(database.memoryCryptoParams);
        assertTrue("Expected identity hash to not be the same as new params i.e. made new instance", currentHash != newParamsHash);
        assertTrue("Expected identity hash to be different to old params hash", currentHash != previousHash);
    }

    @Test
    public void updateMemoryCryptoParams_whenChanged_thenUpdatesRootNode() throws Exception
    {
        // Given
        CryptoParams oldMemoryCryptoParams = database.memoryCryptoParams;
        CryptoParams newMemoryCryptoParams = new CryptoParams(controller, "new pass".toCharArray(), 123, 0);
        database.root = node;

        // When
        database.updateMemoryCryptoParams(controller, newMemoryCryptoParams, PASSWORD);

        // Then
        verify(node).rebuildCrypto(oldMemoryCryptoParams);
    }

    @Test
    public void merge_whenFileCryptoParamsOlder_thenUnchanged() throws Exception
    {
        // Given
        Database databaseRemote = createDatabase(PASSWORD, DEFAULT_LAST_MODIFIED, DEFAULT_LAST_MODIFIED);
        long currentHash = System.identityHashCode(database.fileCryptoParams);

        // When
        databaseRemote.merge(databaseRemote, PASSWORD);

        // Then
        long updatedHash = System.identityHashCode(database.fileCryptoParams);
        assertEquals("File crypto params should be unchanged", currentHash, updatedHash);
    }

    @Test
    public void merge_whenFileCryptoParamsNewer_thenChanged() throws Exception
    {
        // Given
        Database databaseRemote = createDatabase(PASSWORD, DEFAULT_LAST_MODIFIED, DEFAULT_LAST_MODIFIED + 1);
        long currentHash = System.identityHashCode(databaseRemote.fileCryptoParams);

        // When
        database.merge(databaseRemote, PASSWORD);

        // Then
        long updatedHash = System.identityHashCode(database.fileCryptoParams);
        assertTrue("Expected file crypto params identity hash to change", currentHash != updatedHash);

        // -- Check it can still do crypto
        EncryptedAesValue value = controller.getCryptographyService().encrypt(database.fileCryptoParams.getSecretKey(), TEST_DATA);
        assertTrue("Expected encrypted value to be different to decrypted value", !value.getValue().equals(TEST_DATA));

        byte[] decryptd = controller.getCryptographyService().decrypt(databaseRemote.fileCryptoParams.getSecretKey(), value);
        assertArrayEquals("Expected decrypted value to be same after file crypto param two-way cryption", TEST_DATA, decryptd);
    }

    @Test
    public void merge_whenMemoryCryptoParamsOlder_thenUnchanged() throws Exception
    {
        // Given
        Database databaseRemote = createDatabase(PASSWORD, DEFAULT_LAST_MODIFIED, DEFAULT_LAST_MODIFIED);
        long currentHash = System.identityHashCode(database.memoryCryptoParams);

        // When
        databaseRemote.merge(databaseRemote, PASSWORD);

        // Then
        long updatedHash = System.identityHashCode(database.memoryCryptoParams);
        assertEquals("Memory crypto params should be unchanged", currentHash, updatedHash);
    }

    @Test
    public void merge_whenMemoryCryptoParamsNewer_thenChanged() throws Exception
    {
        // Given
        Database databaseRemote = createDatabase("different password".toCharArray(), DEFAULT_LAST_MODIFIED + 1, DEFAULT_LAST_MODIFIED);
        long currentHash = System.identityHashCode(database.memoryCryptoParams);
        long remoteHash = System.identityHashCode(databaseRemote.memoryCryptoParams);
        byte[] oldSalt = database.memoryCryptoParams.getSalt();

        // When
        database.merge(databaseRemote, PASSWORD);

        // Then
        long updatedHash = System.identityHashCode(database.memoryCryptoParams);
        assertTrue("Expected memory crypto params to change", currentHash != updatedHash);
        assertTrue("Expected new object for updated memory crypto params", updatedHash != remoteHash);
        assertTrue("Expected salt to be different", !oldSalt.equals(database.memoryCryptoParams.getSalt()));

        DatabaseNode child = database.getRoot().getChildren().values().iterator().next();
        // TODO: pad block corrupted - seen for line below, investigate if seen again...
        assertArrayEquals("Expected to be able to decrypt child data", TEST_DATA, child.getDecryptedValue());
    }

    @Test
    public void merge_whenInvoked_thenNodesMerged() throws Exception
    {
        // Given
        database.root = node;

        Database databaseRemote = createDatabase(PASSWORD, DEFAULT_LAST_MODIFIED, DEFAULT_LAST_MODIFIED);
        databaseRemote.root = node2;

        // When
        database.merge(databaseRemote, PASSWORD);

        // Then
        verify(node).merge(node2);
    }

    private Database createDatabase(char[] password, long lastModifiedMemoryCryptoParams, long lastModifiedFileCryptoParams) throws Exception
    {
        // Build params
        CryptoParams memoryCryptoParams = new CryptoParams(controller, password, 1234, lastModifiedMemoryCryptoParams);
        CryptoParams fileCryptoParams = new CryptoParams(controller, password, 1234, lastModifiedFileCryptoParams);

        // Build database
        Database database = new Database(controller, memoryCryptoParams, fileCryptoParams);

        // Give it a basic child
        DatabaseNode child = new DatabaseNode(database, UUID.randomUUID(), "child name", 0L, TEST_DATA);
        database.getRoot().getChildren().put(child.getId(), child);

        return database;
    }

}