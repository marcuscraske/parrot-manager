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
    public void encrypt_decrypt_whenGivenValue_thenCanGoInAndOut()
    {
        // Given

        // When

        // Then
    }

    @Test
    public void updateFileCryptoParams_whenChanged_thenHasDiffRef()
    {
        // Given

        // When

        // Then
    }

    @Test
    public void updateMemoryCryptoParams_whenChanged_thenHasDiffRefAndUpdatesRootNodeEncryption()
    {
        // Given

        // When

        // Then
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