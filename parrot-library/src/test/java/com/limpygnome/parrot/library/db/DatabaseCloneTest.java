package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.crypto.CryptoParams;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseCloneTest
{
    private Database database;

    // Mock objects
    @Mock
    private CryptoParams fileCryptoParams;
    @Mock
    private CryptoParams memoryCryptoParams;
    @Mock
    private CryptoParams fileCryptoParamsClone;
    @Mock
    private CryptoParams memoryCryptoParamsClone;
    @Mock
    private DatabaseNode root;
    @Mock
    private DatabaseNode rootClone;

    @Before
    public void setup()
    {
        database = new Database(memoryCryptoParams, fileCryptoParams);
        database.setRoot(root);
    }

    @Test
    public void cloned()
    {
        // Given
        given(root.clone(any(Database.class))).willReturn(rootClone);
        given(fileCryptoParams.clone()).willReturn(fileCryptoParamsClone);
        given(memoryCryptoParams.clone()).willReturn(memoryCryptoParamsClone);

        // When
        Database databaseClone = database.clone();

        // Then
        verify(fileCryptoParams).clone();
        verify(memoryCryptoParams).clone();
        verify(root).clone(databaseClone);

        assertEquals("Should be cloned file crypto params", fileCryptoParamsClone, databaseClone.getFileCryptoParams());
        assertEquals("Should be cloned memory crypto params", memoryCryptoParamsClone, databaseClone.getMemoryCryptoParams());
        assertEquals("Should be cloned root", rootClone, databaseClone.getRoot());
    }

}
