package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.crypto.CryptoParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;
import java.util.UUID;

import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseMergerTest
{
    private static final char[] PASSWORD = "password".toCharArray();
    
    // SUT
    private DatabaseMerger merger;

    // Mock objects
    @Mock
    private Database source;
    @Mock
    private CryptoParams sourceFileCryptoParams;
    @Mock
    private CryptoParams sourceMemoryCryptoParams;

    @Mock
    private DatabaseNode sourceRoot;
    @Mock
    private Set<UUID> sourceRootDeletedChildren;

    @Mock
    private Database destination;
    @Mock
    private CryptoParams destFileCryptoParams;
    @Mock
    private CryptoParams destMemoryCryptoParams;

    @Mock
    private DatabaseNode destinationRoot;
    @Mock
    private Set<UUID> destinationRootDeletedChildren;


    @Before
    public void setup()
    {
        merger = new DatabaseMerger();

        // Source
        given(source.getFileCryptoParams()).willReturn(sourceFileCryptoParams);
        given(source.getMemoryCryptoParams()).willReturn(sourceMemoryCryptoParams);
        given(source.getRoot()).willReturn(sourceRoot);

        given(sourceRoot.getDeletedChildren()).willReturn(sourceRootDeletedChildren);

        // Destination
        given(destination.getFileCryptoParams()).willReturn(destFileCryptoParams);
        given(destination.getMemoryCryptoParams()).willReturn(destMemoryCryptoParams);
        given(destination.getRoot()).willReturn(destinationRoot);

        given(destinationRoot.getDeletedChildren()).willReturn(destinationRootDeletedChildren);
    }



    @Test
    public void mergeDatabaseFileCryptoParams_destOlder_isUpdated()  throws Exception
    {
        // given

        // when
        merger.merge(source, destination, PASSWORD);

        // then
    }

    @Test
    public void mergeDatabaseFileCryptoParams_destNewer_setsDirty()  throws Exception
    {
        // given

        // when
        merger.merge(source, destination, PASSWORD);

        // then
    }



    @Test
    public void mergeDatabaseMemoryCryptoParams_destOlder_isUpdated()  throws Exception
    {
        // given

        // when
        merger.merge(source, destination, PASSWORD);

        // then
    }

    @Test
    public void mergeDatabaseMemoryCryptoParams_destNewer_setsDirty()  throws Exception
    {
        // given

        // when
        merger.merge(source, destination, PASSWORD);

        // then
    }



    @Test
    public void mergeNodeDetails_srcNewerThanDest_updatesName()  throws Exception
    {
        // given

        // when
        merger.merge(source, destination, PASSWORD);

        // then
    }

    @Test
    public void mergeNodeDetails_srcNewerThanDest_updatesValue()  throws Exception
    {
        // given

        // when
        merger.merge(source, destination, PASSWORD);

        // then
    }

    @Test
    public void mergeNodeDetails_srcNewerThanDest_updatesHistory()  throws Exception
    {
        // given

        // when
        merger.merge(source, destination, PASSWORD);

        // then
    }

    @Test
    public void mergeNodeDetails_srcNewerThanDest_updatesLastModified()  throws Exception
    {
        // given

        // when
        merger.merge(source, destination, PASSWORD);

        // then
    }

    @Test
    public void mergeNodeDetails_srcNewerThanDest_setsDirty()  throws Exception
    {
        // given

        // when
        merger.merge(source, destination, PASSWORD);

        // then
    }

    @Test
    public void mergeNodeDetails_deletedChildrenUpdated()  throws Exception
    {
        // given

        // when
        merger.merge(source, destination, PASSWORD);

        // then
    }



    @Test
    public void mergeDestNodeChildren_srcAndDestNodesExist_areRecursivelyMerged()  throws Exception
    {
        // given

        // when
        merger.merge(source, destination, PASSWORD);

        // then
    }

    @Test
    public void mergeDestNodeChildren_srcDeletedDest_removesDest()  throws Exception
    {
        // given

        // when
        merger.merge(source, destination, PASSWORD);

        // then
    }

    @Test
    public void mergeDestNodeChildren_srcMissingDest_setsDirty()  throws Exception
    {
        // given

        // when
        merger.merge(source, destination, PASSWORD);

        // then
    }



    @Test
    public void mergeSrcNodeChildren_srcAddedToDestWhenMissing()  throws Exception
    {
        // given

        // when
        merger.merge(source, destination, PASSWORD);

        // then
    }

    @Test
    public void mergeSrcNodeChildren_destDeletedSrc_setsDirty()  throws Exception
    {
        // given

        // when
        merger.merge(source, destination, PASSWORD);

        // then
    }

}
