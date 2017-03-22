package com.limpygnome.parrot.library.db;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseMergerTest
{
    // SUT
    private DatabaseMerger databaseMerger;

    // Mock objects
    @Mock
    private Database srcDatabase;
    @Mock
    private Database destDatabase;

    @Test
    public void merge_doesNothingWhenEqual()
    {
    }



    @Test
    public void mergeDatabaseFileCryptoParams_destOlder_isUpdated()
    {
    }

    @Test
    public void mergeDatabaseFileCryptoParams_destNewer_setsDirty()
    {
    }



    @Test
    public void mergeDatabaseMemoryCryptoParams_destOlder_isUpdated()
    {
    }

    @Test
    public void mergeDatabaseMemoryCryptoParams_destNewer_setsDirty()
    {
    }



    @Test
    public void mergeNodeDetails_srcNewerThanDest_updatesName()
    {
    }

    @Test
    public void mergeNodeDetails_srcNewerThanDest_updatesValue()
    {
    }

    @Test
    public void mergeNodeDetails_srcNewerThanDest_updatesHistory()
    {
    }

    @Test
    public void mergeNodeDetails_srcNewerThanDest_updatesLastModified()
    {
    }

    @Test
    public void mergeNodeDetails_srcNewerThanDest_setsDirty()
    {
    }

    @Test
    public void mergeNodeDetails_deletedChildrenUpdated()
    {
    }



    @Test
    public void mergeDestNodeChildren_srcAndDestNodesExist_areRecursivelyMerged()
    {
    }

    @Test
    public void mergeDestNodeChildren_srcDeletedDest_removesDest()
    {
    }

    @Test
    public void mergeDestNodeChildren_srcMissingDest_setsDirty()
    {
    }



    @Test
    public void mergeSrcNodeChildren_srcAddedToDestWhenMissing()
    {
    }

    @Test
    public void mergeSrcNodeChildren_destDeletedSrc_setsDirty()
    {
    }

}
