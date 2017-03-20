package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.crypto.CryptoParams;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.limpygnome.parrot.library.test.ParrotAssert.assertArrayContentsEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseNodeTest
{
    private static final String NAME = "foobar";
    private static final long LAST_MODIFIED = 11223344;

    // SUT
    private DatabaseNode node;

    // Mock objects
    @Mock
    private Database database;
    private UUID uuid;
    @Mock
    private EncryptedValue encryptedValue;
    @Mock
    private EncryptedValue encryptedValue2;
    @Mock
    private CryptoParams cryptoParams;
    @Mock
    private Map<UUID, DatabaseNode> databaseLookup;

    @Before
    public void setup()
    {
        uuid = UUID.randomUUID();
        node = new DatabaseNode(database, uuid, NAME, LAST_MODIFIED, encryptedValue);
        given(database.getLookup()).willReturn(databaseLookup);
    }

    @Test
    public void constructor_fiveParams_isReflected()
    {
        // When
        DatabaseNode node = new DatabaseNode(database, uuid, "test", 1234L, encryptedValue);

        // Then
        assertEquals("Database is different", database, node.database);
        assertEquals("Identifier is different", uuid.toString(), node.getId());
        assertEquals("Name is different", "test", node.getName());
        assertEquals("Last modified is different", 1234L, node.getLastModified());
        assertEquals("Encrypted value is different", encryptedValue, node.getValue());
    }

    @Test
    public void constructor_twoParams_passedIsReflected()
    {
        //fail("TBC");
    }

    @Test
    public void constructor_twoParams_currentTime()
    {
        //fail("TBC");
    }

    @Test
    public void constructor_twoParams_randomUuid()
    {
        //fail("TBC");
    }

    @Test
    public void setId_isReflected()
    {
        // Given
        UUID uuid = UUID.randomUUID();

        // When
        node.setId(uuid);

        // Then
        assertEquals("UUID is different", uuid.toString(), node.getId());
    }

    @Test
    public void setId_setsDirty()
    {
        // When
        node.setId(UUID.randomUUID());

        // Then
        verify(database).setDirty(true);
    }

    @Test
    public void setId_removesOldFromLookup()
    {
        // Given
        UUID uuid = UUID.randomUUID();

        // When
        node.setId(uuid);

        // Then
        verify(databaseLookup).remove(this.uuid);
        verify(databaseLookup).put(uuid, node);
    }

    @Test
    public void setId_addsNewToLookup()
    {
        // Given

        // When

        // Then
    }

    @Test
    public void setName_isReflected()
    {
        // Given
        String name = "blah123";

        // When
        node.setName(name);

        // Then
        assertEquals("Name not correct", name, node.getName());
    }

    @Test
    public void setName_setsDirty()
    {
        // WHen
        node.setName("foobar");

        // Then
        verify(database).setDirty(true);
    }

    @Test
    public void getFormattedLastModified_isCorrect()
    {
        // Given
        long lastModified = 1489772047L * 1000L;

        // When
        node = new DatabaseNode(database, uuid, null, lastModified, null);

        // Then
        assertEquals("Incorrect date format", "17-03-2017 17:34:07", node.getFormattedLastModified());
    }

    @Test
    public void setValue_isReflected()
    {
        // When
        node.setValue(encryptedValue2);

        // Then
        assertEquals("Value has not changed", encryptedValue2, node.getValue());
    }

    @Test
    public void setValue_setsDirty()
    {
        // When
        node.setValue(encryptedValue2);

        // Then
        verify(database, times(2)).setDirty(true);
    }

    @Test
    public void history_isInstance()
    {
        // When
        DatabaseNodeHistory history = node.history();

        // Then
        assertNotNull("Should be an instance", history);
    }

    @Test
    public void getChildren_isCorrect()
    {
        // Given
        DatabaseNode child1 = node.addNew();
        DatabaseNode child2 = node.add(new DatabaseNode(database, "foobar"));

        // When
        DatabaseNode[] children = node.getChildren();

        // Then
        DatabaseNode[] expected = new DatabaseNode[] { child1, child2 };
        assertArrayContentsEqual("Not returning the correct two children", expected, children);
    }

    @Test
    public void getChildren_isStoredInternallyToPreventGarbageCollection()
    {
        // Given
        DatabaseNode child1 = node.addNew();
        DatabaseNode child2 = node.add(new DatabaseNode(database, "foobar"));

        // When
        node.getChildren();

        // Then
        DatabaseNode[] expected = new DatabaseNode[] { child1, child2 };
        DatabaseNode[] cached = node.childrenCached;
        assertArrayContentsEqual("Children array should be cached when invoking getChildren", expected, cached);
    }

    @Test
    public void getByName_handlesNullName()
    {
        // When
        node.getByName(null);
    }

    @Test
    public void getByName_isNullWhenNotExists()
    {
        // Given
        node.addNew().setName("blah");

        // When
        DatabaseNode result = node.getByName("foobar");

        // Then
        assertNull("Should have not been found", result);
    }

    @Test
    public void getByName_isReturned()
    {
        // Given
        DatabaseNode newChild = node.addNew();
        newChild.setName("blah");

        // When
        DatabaseNode result = node.getByName("blah");

        // Then
        assertEquals("New node should be returned by its name", newChild, result);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getChildrenMap_isUnmodifiable()
    {
        // When
        Map<UUID, DatabaseNode> children = node.getChildrenMap();
        children.put(UUID.randomUUID(), new DatabaseNode(database, null));
    }

    @Test
    public void getChildCount_isCorrect()
    {
        // Given
        assertEquals("Should be no children", 0, node.getChildCount());

        // When
        node.addNew();
        node.add(new DatabaseNode(database, null));

        // Then
        assertEquals("Should be two children", 2, node.getChildCount());
    }

    @Test
    public void getDeletedChildren_hasDeletedNode()
    {
        // Given
        DatabaseNode child = node.addNew();
        assertEquals("Should be no deleted children", 0, node.getDeletedChildren().size());

        // When
        child.remove();

        // Then
        assertEquals("Should be deleted child", 1, node.getDeletedChildren().size());
        assertEquals("Should be identifier of deleted child", child.getUuid(), node.getDeletedChildren().iterator().next());
    }

    @Test
    public void rebuildCrypto_noReEncryptForNullValue() throws Exception
    {
        // Given
        node.setValue(null);

        // When
        node.rebuildCrypto(cryptoParams);

        // Then
        verify(database, never()).encrypt(any());
        verify(database, never()).decrypt(any());
    }

    @Test
    public void rebuildCrypto_reEncryptsValue() throws Exception
    {
        // Given
        byte[] decrypted = { 0x77, 0x66, 0x55 };
        given(database.decrypt(encryptedValue, cryptoParams)).willReturn(decrypted);
        given(database.encrypt(decrypted)).willReturn(encryptedValue2);

        // When
        node.rebuildCrypto(cryptoParams);

        // Then
        verify(database).decrypt(encryptedValue, cryptoParams);
        verify(database).encrypt(decrypted);

        assertEquals("Value of node should be re-encrypted instance", encryptedValue2, node.getValue());
    }

    @Test
    public void rebuildCrypto_isRecursive()
    {
    }

    @Test
    public void clone_isNullWhenNullValue()
    {
    }

    @Test
    public void clone_hasSameValues()
    {
    }

    @Test
    public void clone_hasClonedChildren()
    {
    }

    @Test
    public void add_isReflected()
    {
    }

    @Test
    public void add_setsParentOnChild()
    {
    }

    @Test
    public void add_setsDirty()
    {
    }

    @Test
    public void addNew_blankValuesAndChild()
    {
    }

    @Test
    public void addNew_setsDirty()
    {
    }

    @Test
    public void remove_nothingWhenParentNull()
    {
    }

    @Test
    public void remove_isReflected()
    {
    }

    @Test
    public void remove_isNoLongerInDatabaseLookup()
    {
    }

    @Test
    public void remove_parentNull()
    {
    }

    @Test
    public void remove_setsDirty()
    {
    }

    @Test
    public void isRoot_whenParentNull()
    {
    }

    @Test
    public void isRoot_notWhenParentNotNUll()
    {
    }

    @Test
    public void getParent_isReflected()
    {
    }

    @Test
    public void getPath_whenRoot()
    {
    }

    @Test
    public void getPath_whenChild()
    {
    }

}
