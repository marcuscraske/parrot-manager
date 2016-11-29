package com.limpygnome.parrot.model.db;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.model.params.CryptoParams;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class DatabaseNodeTest {

    private static final byte[] TEST_DECRYPTED_DATA = { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77 };

    // SUT
    private DatabaseNode node;

    // Dependencies
    private Controller controller;
    private CryptoParams memoryCryptoParams;
    private Database database;

    @Before
    public void setup() throws Exception
    {
        // Setup SUT dependencies
        controller = new Controller();
        memoryCryptoParams = new CryptoParams(controller, "PASSWORD".toCharArray(), 1234, 0);
        database = new Database(controller, memoryCryptoParams, null);

        // Setup SUT
        node = new DatabaseNode(database, UUID.randomUUID(), "random name", 123456L, TEST_DECRYPTED_DATA);
    }

    @Test
    public void rebuildCrypto_whenChanged_thenDecryptedValueIsSame() throws Exception
    {
        // Given
        CryptoParams newMemoryCryptoParams = new CryptoParams(controller, "PASSWORD NEW".toCharArray(), 123456, 0);
        database.memoryCryptoParams = newMemoryCryptoParams;
        byte[] currentEncryptedValue = node.getValue().getValue();

        // When
        node.rebuildCrypto(memoryCryptoParams);

        // Then
        byte[] newlyEncryptedValue = node.getValue().getValue();

        assertFalse("Expected the before and after encrypted byte array to differ", currentEncryptedValue.equals(newlyEncryptedValue));
        assertArrayEquals("Expected decrypted value to remain the same", TEST_DECRYPTED_DATA, node.getDecryptedValue());
    }

    @Test
    public void clone_whenInvoked_thenProducesHardCopy() throws Exception
    {
        // When
        DatabaseNode clone = node.clone(database);

        // Then
        assertEquals("Objects should be equal", node, clone);
        assertTrue("Reference is not the same for parent node", System.identityHashCode(node) != System.identityHashCode(clone));

        // TODO: need to go deeper (with ref checking)...
    }

}