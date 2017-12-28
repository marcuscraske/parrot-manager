package com.limpygnome.parrot.library.db;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseLookupTest
{
    // SUT
    private DatabaseLookup databaseLookup;

    // Test objects
    private UUID uuid;
    private DatabaseNode node;

    @Before
    public void setup()
    {
        // Setup SUT
        databaseLookup = new DatabaseLookup();

        // Setup test objects
        Database database = new Database(null, null);

        uuid = UUID.randomUUID();
        node = new DatabaseNode(database, uuid, null, 0L, null);
    }

    @Test
    public void get_add_isRetrievedWhenExists()
    {
        // given
        databaseLookup.add(node);

        // when
        DatabaseNode result = databaseLookup.get(uuid);

        // then
        assertEquals("expected added node to be retrieved", node, result);
    }

    @Test
    public void get_isNullWhenMissing()
    {
        // given
        UUID randomUuid = UUID.randomUUID();

        // when
        DatabaseNode result = databaseLookup.get(randomUuid);

        // then
        assertNull("no result should have been returned", result);
    }

    @Test
    public void add_includesChildren()
    {
        // given
        DatabaseNode child = node.addNew();

        // when
        databaseLookup.add(node);
        DatabaseNode resultChild = databaseLookup.get(child.getUuid());

        // then
        assertNotNull("child should be an instance", child);
        assertNotEquals("child id should not be same as parent", child.getId(), node.getId());
        assertEquals("expected child to be returned", child, resultChild);
    }

    @Test
    public void remove_asExpected()
    {
        // given
        databaseLookup.add(node);
        assertEquals("should return node added", node, databaseLookup.get(uuid));

        // when
        databaseLookup.remove(node);
        DatabaseNode result = databaseLookup.get(uuid);

        // then
        assertNull("should not return result", result);
    }

    @Test
    public void remove_includesChildren()
    {
        // given
        DatabaseNode child = node.addNew();
        databaseLookup.add(node);

        DatabaseNode resultChild = databaseLookup.get(child.getUuid());
        assertEquals("expected child to be returned", child, resultChild);
        assertNotEquals("child id should not be same as parent", child.getId(), node.getId());

        // when
        databaseLookup.remove(node);

        // then
        resultChild = databaseLookup.get(child.getUuid());
        assertNull("child should have been removed", resultChild);
    }

}