package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.crypto.EncryptedValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.LinkedList;
import java.util.List;

import static com.limpygnome.parrot.library.test.ParrotAssert.assertArrayContentsEqual;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseNodeHistoryTest
{

    // SUT
    private DatabaseNodeHistory history;

    // Captors
    @Captor
    private ArgumentCaptor<DatabaseNodeHistory> historyCaptor;

    // Mocks
    @Mock
    private DatabaseNode currentNode;
    @Mock
    private DatabaseNode targetNode;
    @Mock
    private Database database;
    @Mock
    private EncryptedValue encryptedValue;
    @Mock
    private EncryptedValue encryptedValue2;

    // Test data
    private List<EncryptedValue> values;

    @Before
    public void setup()
    {
        history = new DatabaseNodeHistory(currentNode);

        given(currentNode.getDatabase()).willReturn(database);

        // Setup array of multiple values
        values = new LinkedList<>();
        values.add(encryptedValue);
        values.add(encryptedValue2);
    }

    @Test
    public void add_isAdded()
    {
        // Given
        history.add(encryptedValue);

        // When
        EncryptedValue fetched = history.fetch()[0];

        // Then
        assertEquals("Should be same as value added", encryptedValue, fetched);
        assertEquals("Size should be one as only single item", 1, history.size());
    }

    @Test
    public void add_setsDirty()
    {
        // When
        history.add(encryptedValue);

        // Then
        verify(database).setDirty(true);
    }

    @Test
    public void addAll_isAdded()
    {
        // When
        history.addAll(values);

        // Then
        EncryptedValue[] expectedValues = { encryptedValue, encryptedValue2 };
        EncryptedValue[] fetchedValues = history.fetch();
        assertArrayContentsEqual("Should contain both items", expectedValues, fetchedValues);
        assertEquals("Size should be two as two elements", 2, history.size());
    }

    @Test
    public void addAll_isDirty()
    {
        // When
        history.addAll(values);

        // Then
        verify(database).setDirty(true);
    }

    @Test
    public void fetch_isCached()
    {
        // Given
        history.add(encryptedValue);

        // When
        EncryptedValue[] fetchedValues = history.fetch();

        // Then
        assertArrayEquals("Fetched values should be cached", fetchedValues, history.historyCached);
    }

    @Test
    public void remove_isRemoved()
    {
        // Given
        history.add(encryptedValue);
        history.add(encryptedValue2);

        // When
        history.remove(encryptedValue);
        EncryptedValue[] fetchedValues = history.fetch();

        // Then
        EncryptedValue[] expectedValues = { encryptedValue2 };
        assertArrayEquals("Should only have non-removed element", expectedValues, fetchedValues);
    }

    @Test
    public void remove_setsDirty()
    {
        // When
        history.remove(encryptedValue);

        // Then
        verify(database).setDirty(true);
    }

    @Test
    public void clearAll_isCleared()
    {
        // Given
        history.addAll(values);

        // When
        history.clearAll();
        EncryptedValue[] fetchedValues = history.fetch();

        // Then
        assertArrayEquals("Should be empty array as no items", new EncryptedValue[0], fetchedValues);
    }

    @Test
    public void clearAll_setsDirty()
    {
        // Given
        history.addAll(values);

        // When
        history.clearAll();

        // Then
        verify(database, times(2)).setDirty(true);
    }

    @Test
    public void cloneToNode_targetHasNewInstanceWithClonedChildren()
    {
        // Given
        history.add(encryptedValue);
        given(encryptedValue.clone()).willReturn(encryptedValue2);

        // When
        history.cloneToNode(targetNode);

        // Then
        verify(encryptedValue).clone();
        verify(targetNode).setHistory(historyCaptor.capture());

        EncryptedValue clonedInstance = historyCaptor.getValue().fetch()[0];
        assertEquals("Cloned instance should be in target result", encryptedValue2, clonedInstance);
    }

}
