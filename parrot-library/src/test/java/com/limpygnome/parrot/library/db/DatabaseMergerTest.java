package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.crypto.CryptoParams;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
    @Mock
    private EncryptedValue oldValue;
    @Mock
    private EncryptedValue newValue;
    @Mock
    private EncryptedValue cloneValue;
    @Mock
    private DatabaseNodeHistory oldHistory;
    @Mock
    private DatabaseNodeHistory newHistory;
    @Mock
    private DatabaseNodeHistory clonedHistory;


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
        given(sourceFileCryptoParams.getLastModified()).willReturn(456L);
        given(destFileCryptoParams.getLastModified()).willReturn(123L);

        // when
        merger.merge(source, destination, PASSWORD);

        // then
        verify(destination).updateFileCryptoParams(sourceFileCryptoParams, PASSWORD);
    }

    @Test
    public void mergeDatabaseFileCryptoParams_destNewer_setsDirty()  throws Exception
    {
        // given
        given(sourceFileCryptoParams.getLastModified()).willReturn(123L);
        given(destFileCryptoParams.getLastModified()).willReturn(456L);

        // when
        merger.merge(source, destination, PASSWORD);

        // then
        verify(destination).setDirty(true);
    }



    @Test
    public void mergeDatabaseMemoryCryptoParams_destOlder_isUpdated()  throws Exception
    {
        // given
        given(sourceMemoryCryptoParams.getLastModified()).willReturn(456L);
        given(destMemoryCryptoParams.getLastModified()).willReturn(123L);

        // when
        merger.merge(source, destination, PASSWORD);

        // then
        verify(destination).updateMemoryCryptoParams(sourceMemoryCryptoParams, PASSWORD);
    }

    @Test
    public void mergeDatabaseMemoryCryptoParams_destNewer_setsDirty()  throws Exception
    {
        // given
        given(sourceFileCryptoParams.getLastModified()).willReturn(123L);
        given(destFileCryptoParams.getLastModified()).willReturn(456L);

        // when
        merger.merge(source, destination, PASSWORD);

        // then
        verify(destination).setDirty(true);
    }



    @Test
    public void mergeNodeDetails_srcNewerThanDest_updatesName()  throws Exception
    {
        // given
        givenSourceRootNewerThanDest();

        given(sourceRoot.getName()).willReturn("new");
        given(destinationRoot.getName()).willReturn("old");

        // when
        merger.merge(source, destination, PASSWORD);

        // then
        verify(destinationRoot).setName("new");
    }

    @Test
    public void mergeNodeDetails_srcNewerThanDest_updatesValue()  throws Exception
    {
        // given
        givenSourceRootNewerThanDest();

        given(sourceRoot.getValue()).willReturn(newValue);
        given(destinationRoot.getValue()).willReturn(oldValue);

        given(sourceRoot.getValue().clone()).willReturn(cloneValue);

        // when
        merger.merge(source, destination, PASSWORD);

        // then
        verify(destinationRoot).setValue(cloneValue);
    }

    @Test
    public void mergeNodeDetails_srcNewerThanDest_mergesHistory()  throws Exception
    {
        // given
        givenSourceRootNewerThanDest();

        given(sourceRoot.getHistory()).willReturn(newHistory);
        given(destinationRoot.getHistory()).willReturn(oldHistory);

        // when
        merger.merge(source, destination, PASSWORD);

        // then
        verify(oldHistory).merge(newHistory);
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

    private void givenSourceRootNewerThanDest()
    {
        given(sourceRoot.getLastModified()).willReturn(456L);
        given(destinationRoot.getLastModified()).willReturn(123L);
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
