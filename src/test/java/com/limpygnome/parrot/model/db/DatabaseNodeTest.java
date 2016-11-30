package com.limpygnome.parrot.model.db;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.model.params.CryptoParams;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
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

        assertIdentityHashCode("Reference should not be the same for parent node", node, clone);

        assertIdentityHashCode("Reference should not be the same for value", node.getValue(), clone.getValue());
        assertIdentityHashCode("Reference should not be the same for children", node.getChildren(), clone.getChildren());
        assertIdentityHashCode("Reference should not be the same for deleted children", node.getDeletedChildren(), clone.getDeletedChildren());
    }

    @Test
    public void merge_whenSrcOlder_thenLocalPropertiesRemainSame() throws Exception {
        // Given
        DatabaseNode src = new DatabaseNode(database, UUID.randomUUID(), "src", 2000L, new byte[]{ 0x11, 0x44 });
        DatabaseNode dest = new DatabaseNode(database, UUID.randomUUID(), "dest", 3000L, TEST_DECRYPTED_DATA);
        DatabaseNode destBackup = dest.clone(database);

        // When
        dest.merge(src);

        // Then
        assertEquals("Clone and original should remain the same", destBackup, dest);
    }

    @Test
    public void merge_whenSrcNewer_thenLocalPropertiesCopied() throws Exception {
        // Given
        DatabaseNode src = new DatabaseNode(database, UUID.randomUUID(), "new name", 3000L, TEST_DECRYPTED_DATA);
        DatabaseNode dest = new DatabaseNode(database, UUID.randomUUID(), "old name", 1L, new byte[]{ 0x11, 0x44 });

        // When
        dest.merge(src);

        // Then
        assertEquals("Expected name to have changed", "new name", dest.getName());
        assertEquals("Expected last modified to have changed", 3000L, dest.getLastModified());
        assertArrayEquals("Expected value to have changed", TEST_DECRYPTED_DATA, dest.getDecryptedValue());
    }

    @Test
    public void merge_whenChildModified_thenRecursivelyMerged() throws Exception {
        // Given
        DatabaseNode src = new DatabaseNode(database, UUID.randomUUID(), "old name", 3000L, TEST_DECRYPTED_DATA);
        DatabaseNode srcChild = new DatabaseNode(database, UUID.randomUUID(), "child name new", 2000L, TEST_DECRYPTED_DATA);
        src.getChildren().put(srcChild.getId(), srcChild);

        DatabaseNode dest = new DatabaseNode(database, UUID.randomUUID(), "unchanged name", 5000L, new byte[]{ 0x11, 0x44 });
        DatabaseNode destChild = new DatabaseNode(database, srcChild.getId(), "child name old", 1000L, new byte[]{ 0x22, 0x33, 0x44 });
        dest.getChildren().put(destChild.getId(), destChild);

        // When
        dest.merge(src);

        // Then
        // -- Top-level
        assertEquals("Top-level node name should be unchanged", "unchanged name", dest.getName());
        assertEquals("Top-level node last modified should be unchanged", 5000L, dest.getLastModified());
        assertArrayEquals("Top-level node value should be unchanged", new byte[]{ 0x11, 0x44 }, dest.getDecryptedValue());

        // -- Child node
        assertEquals("Child node name should be changed", "child name new", destChild.getName());
        assertEquals("Child node last modified should be changed", 2000L, destChild.getLastModified());
        assertArrayEquals("Child node value should be changed", TEST_DECRYPTED_DATA, destChild.getDecryptedValue());
    }

    @Test
    public void merge_whenChildDeleted_thenDeletedChildIsRemoved() throws Exception {
        // Given
        UUID uuidDeleted = UUID.randomUUID();

        // -- Last modified should not matter
        DatabaseNode src = new DatabaseNode(database, UUID.randomUUID(), "unchanged name", 1234L, TEST_DECRYPTED_DATA);
        src.getDeletedChildren().add(uuidDeleted);

        DatabaseNode dest = new DatabaseNode(database, UUID.randomUUID(), "unchanged name", 1234L, TEST_DECRYPTED_DATA);
        DatabaseNode destChild = new DatabaseNode(database, uuidDeleted, "deleted node", 1234L, TEST_DECRYPTED_DATA);
        dest.getChildren().put(uuidDeleted, destChild);

        // -- Quick sanity check...
        assertTrue("Dest should contain child as not yet merged", dest.getChildren().containsKey(uuidDeleted));

        // When
        dest.merge(src);

        // Then
        assertFalse("Dest should not contain a child after merge because it was deleted in src", dest.getChildren().containsKey(uuidDeleted));
    }

    @Test
    public void merge_whenChildAdded_thenChildAdded() throws Exception {
        // Given
        DatabaseNode src = new DatabaseNode(database, UUID.randomUUID(), "unchanged name", 1234L, TEST_DECRYPTED_DATA);
        DatabaseNode srcNewChild = new DatabaseNode(database, UUID.randomUUID(), "new child", 5000L, new byte[]{ 0x11, 0x22, 0x44 });
        src.getChildren().put(srcNewChild.getId(), srcNewChild);

        DatabaseNode dest = new DatabaseNode(database, UUID.randomUUID(), "unchanged name", 1234L, TEST_DECRYPTED_DATA);

        // -- Quick sanity check...
        assertTrue("Dest should not have any children", dest.getChildren().isEmpty());

        // When
        dest.merge(src);

        // Then
        assertTrue("Expected dest to contain new child added at src", dest.getChildren().containsKey(srcNewChild.getId()));
        assertEquals("Expected dest to contain node which is equal to child node at src", srcNewChild, dest.getChildren().get(srcNewChild.getId()));
    }

    @Test
    public void merge_whenDifferingListsOfDeletedItems_thenMerged() throws Exception {
        // Given
        UUID[] srcPool = new UUID[]{
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        };

        UUID[] destPool = new UUID[]{
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        };

        DatabaseNode src = new DatabaseNode(database, UUID.randomUUID(), "unchanged name", 1234L, TEST_DECRYPTED_DATA);
        src.getDeletedChildren().addAll(Arrays.asList(srcPool));

        DatabaseNode dest = new DatabaseNode(database, UUID.randomUUID(), "unchanged name", 1234L, TEST_DECRYPTED_DATA);
        dest.getDeletedChildren().addAll(Arrays.asList(destPool));

        // -- Sanity check
        assertEquals("Expected only three deleted items in src", 3, src.getDeletedChildren().size());
        assertEquals("Expected only three deleted items in dest", 3, dest.getDeletedChildren().size());

        // When
        dest.merge(src);

        // Then
        final HashSet<UUID> expectedItems = new HashSet<>(6);
        expectedItems.addAll(Arrays.asList(srcPool));
        expectedItems.addAll(Arrays.asList(destPool));

        assertEquals("Expected six deleted items", 6, dest.getDeletedChildren().size());
        assertEquals("Expected all items", expectedItems, dest.getDeletedChildren());
    }

    private void assertIdentityHashCode(String message, Object source, Object clone)
    {
        assertTrue(message, System.identityHashCode(source) != System.identityHashCode(clone));
    }

}