package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.crypto.EncryptedValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static com.limpygnome.parrot.library.test.ParrotAssert.assertArrayContentsEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseNodeTest
{
    // SUT
    private DatabaseNode node;

    // Mock objects
    private Database database;
    private UUID uuid;
    @Mock
    private EncryptedValue encryptedValue;
    @Mock
    private EncryptedValue encryptedValue2;

    @Before
    public void setup()
    {
        database = new Database(null, null);
        uuid = UUID.randomUUID();
        node = new DatabaseNode(database, null);
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
        // Given
        assertFalse("Database should not be dirty", database.isDirty());

        // When
        database.setDirty(true);

        // Then
        assertTrue("Database should be dirty", database.isDirty());
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
        // Given
        assertFalse("Database should have not yet changed", database.isDirty());

        // WHen
        node.setName("foobar");

        // Then
        assertTrue("Database should now be dirty", database.isDirty());
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
        // Given
        assertFalse("Database should have not yet changed", database.isDirty());

        // When
        node.setValue(encryptedValue2);

        // Then
        assertTrue("Database should now be dirty", database.isDirty());
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
    }

    @Test
    public void getByName_isNullWhenNotExists()
    {
    }

    @Test
    public void getByName_isReturned()
    {
    }

    @Test
    public void getChildrenMap_isUnmodifiable()
    {
    }

    @Test
    public void getChildCount_isCorrect()
    {
    }

    @Test
    public void getDeletedChildren_hasDeletedNode()
    {
    }

    @Test
    public void rebuildCrypto_noDecryptForNullValue()
    {
    }

    @Test
    public void rebuildCrypto_reEncryptsValue()
    {
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
