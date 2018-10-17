package com.limpygnome.parrot.library.db;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseOptimizerTest
{

    // SUT
    private DatabaseOptimizer databaseOptimizer;

    // Mocks
    @Mock
    private Database database;

    @Mock
    private DatabaseNode rootNode;
    @Mock
    private Set<UUID> deletedChildren;
    @Mock
    private DatabaseNodeHistory history;

    @Mock
    private DatabaseNode childNode;
    @Mock
    private Set<UUID> childDeletedChildren;
    @Mock
    private DatabaseNodeHistory childHistory;

    @Before
    public void setup()
    {
        databaseOptimizer = new DatabaseOptimizer();

        given(database.getRoot()).willReturn(rootNode);

        given(rootNode.getChildren()).willReturn(new DatabaseNode[]{ childNode });
        given(rootNode.getDeletedChildren()).willReturn(deletedChildren);
        given(rootNode.getHistory()).willReturn(history);

        given(childNode.getChildren()).willReturn(new DatabaseNode[0]);
        given(childNode.getDeletedChildren()).willReturn(childDeletedChildren);
        given(childNode.getHistory()).willReturn(childHistory);
    }

    @Test
    public void deleteAllDeletedNodeHistory_deletedChildrenCleared()
    {
        // When
        databaseOptimizer.deleteAllDeletedNodeHistory(database);

        // Then
        verify(deletedChildren).clear();
    }

    @Test
    public void deleteAllDeletedNodeHistory_isRecursive()
    {
        // When
        databaseOptimizer.deleteAllDeletedNodeHistory(database);

        // Then
        verify(childDeletedChildren).clear();
    }

    @Test
    public void deleteAllDeletedNodeHistory_setsDirty()
    {
        // Given

        // When
        databaseOptimizer.deleteAllDeletedNodeHistory(database);

        // Then
        verify(database).setDirty(true);
    }

    @Test
    public void deleteAllValueHistory_clearsHistory()
    {
        // When
        databaseOptimizer.deleteAllValueHistory(database);

        // Then
        verify(history).deleteAll();
    }

    @Test
    public void deleteAllValueHistory_isRecursive()
    {
        // When
        databaseOptimizer.deleteAllValueHistory(database);

        // Then
        verify(childHistory).deleteAll();
    }

}
