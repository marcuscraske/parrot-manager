package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.crypto.EncryptedValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

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
    private Database database;
    @Mock
    private EncryptedValue encryptedValue;
    @Mock
    private EncryptedValue encryptedValueClone;
    @Mock
    private EncryptedValue encryptedValue2;
    @Mock
    private EncryptedValue encryptedValue2Clone;

    // Test data
    private List<EncryptedValue> values;
    UUID encryptedValueId = UUID.randomUUID();
    UUID encryptedValueId2 = UUID.randomUUID();

    @Before
    public void setup()
    {
        history = new DatabaseNodeHistory(currentNode);

        given(currentNode.getDatabase()).willReturn(database);

        // Setup fake IDs
        given(encryptedValue.getUuid()).willReturn(encryptedValueId);
        given(encryptedValue2.getUuid()).willReturn(encryptedValueId2);
        given(encryptedValueClone.getUuid()).willReturn(encryptedValueId);
        given(encryptedValue2Clone.getUuid()).willReturn(encryptedValueId2);

        // Setup array of multiple values
        values = new LinkedList<>();
        values.add(encryptedValue);
        values.add(encryptedValue2);

        // Allow cloning of values
        given(encryptedValue.clone()).willReturn(encryptedValueClone);
        given(encryptedValue2.clone()).willReturn(encryptedValue2Clone);
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
        history.delete(encryptedValueId.toString());
        EncryptedValue[] fetchedValues = history.fetch();

        // Then
        EncryptedValue[] expectedValues = { encryptedValue2 };
        assertArrayEquals("Should only have non-removed element", expectedValues, fetchedValues);
    }

    @Test
    public void remove_setsDirty()
    {
        // When
        history.delete(encryptedValue.toString());

        // Then
        verify(database).setDirty(true);
    }

    @Test
    public void clearAll_isCleared()
    {
        // Given
        history.addAll(values);

        // When
        history.deleteAll();
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
        history.deleteAll();

        // Then
        verify(database, times(2)).setDirty(true);
    }

    @Test
    public void merge_missingValuesAdded()
    {
        // given
        DatabaseNodeHistory otherHistory = new DatabaseNodeHistory(currentNode);
        history.add(encryptedValue);
        history.add(encryptedValue2);

        // when
        otherHistory.merge(history);

        // then
        EncryptedValue[] result = otherHistory.fetch();
        EncryptedValue[] expected = { encryptedValueClone, encryptedValue2Clone };
        assertArrayContentsEqual("Should contain same elements", expected, result);
    }

    @Test
    public void merge_existingValueNotAdded()
    {
        // given
        DatabaseNodeHistory otherHistory = new DatabaseNodeHistory(currentNode);
        otherHistory.add(encryptedValue);

        history.add(encryptedValue);
        history.add(encryptedValue2);

        // when
        otherHistory.merge(history);

        // then
        EncryptedValue[] result = otherHistory.fetch();
        EncryptedValue[] expected = { encryptedValue, encryptedValue2Clone };
        assertArrayContentsEqual("Should contain same elements", expected, result);
    }

}
