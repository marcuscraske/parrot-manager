package com.limpygnome.parrot.library.db;

import com.limpygnome.parrot.library.crypto.CryptoParams;
import com.limpygnome.parrot.library.crypto.CryptoParamsFactory;
import com.limpygnome.parrot.library.crypto.CryptoReaderWriter;
import com.limpygnome.parrot.library.crypto.EncryptedValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseTest
{

    // SUT
    private Database database;

    // Mock dependencies
    @Mock
    private CryptoReaderWriter cryptoReaderWriter;
    @Mock
    private CryptoParamsFactory cryptoParamsFactory;

    // Mock objects
    @Mock
    private CryptoParams memoryCryptoParams;
    @Mock
    private CryptoParams fileCryptoParams;
    @Mock
    private CryptoParams cryptoParams;
    @Mock
    private CryptoParams cryptoParams2;
    @Mock
    private DatabaseNode node;
    @Mock
    private EncryptedValue encryptedValue;

    // Test data
    private final byte[] TEST_BYTES = new byte[] { 0x11, 0x22, 0x33 };
    private final char[] PASSWORD_CHAR_ARRAY = "foobar".toCharArray();

    @Before
    public void setup()
    {
        database = new Database(
                memoryCryptoParams,
                fileCryptoParams,
                cryptoReaderWriter,
                cryptoParamsFactory
        );
    }

    @Test
    public void getRoot_isInstance()
    {
        // When
        DatabaseNode root = database.getRoot();

        // Then
        assertNotNull("Should be an instance", root);
    }

    @Test
    public void getNode_isNullWhenNotExists()
    {
        // When
        DatabaseNode result = database.getNode(UUID.randomUUID().toString());

        // Then
        assertNull("Expected no node to be returned", result);
    }

    @Test
    public void getNode_retrievesNode()
    {
        // Given
        DatabaseNode child = database.getRoot().addNew();

        // When
        DatabaseNode result = database.getNode(child.getUuid().toString());

        // Then
        assertEquals("Expected new child to be returned", child, result);
    }

    @Test
    public void getNodeByUuid_isNullWhenNotExists()
    {
        // When
        DatabaseNode result = database.getNodeByUuid(UUID.randomUUID());

        // Then
        assertNull("Expected no node to be returned", result);
    }

    @Test
    public void getNodeByUuid_retrievesNode()
    {
        // Given
        DatabaseNode child = database.getRoot().addNew();

        // When
        DatabaseNode result = database.getNodeByUuid(child.getUuid());

        // Then
        assertEquals("Expected new child to be returned", child, result);
    }

    @Test
    public void setNode_isReflectedWhenRetrieved()
    {
        // Given
        DatabaseNode node = new DatabaseNode(database, "blah");

        // When
        database.setRoot(node);

        // Then
        DatabaseNode root = database.getRoot();
        assertEquals("Expected set node to be the root", node, root);
    }

    @Test
    public void changePassword_clonesParamsWithNewPassword() throws Exception
    {
        // When
        database.changePassword("foobar");

        // Then
        verify(cryptoParamsFactory).clone(fileCryptoParams, PASSWORD_CHAR_ARRAY);
        verify(cryptoParamsFactory).clone(memoryCryptoParams, PASSWORD_CHAR_ARRAY);
        verifyNoMoreInteractions(cryptoParamsFactory);
    }

    @Test
    public void changePassword_rebuildsMemoryCrypto() throws Exception
    {
        // Given
        database.setRoot(node);

        // When
        database.changePassword("foobar");

        // Then
        verify(node).rebuildCrypto(memoryCryptoParams);
        verifyNoMoreInteractions(node);
    }

    @Test
    public void encrypt_usesCryptoReaderWriter() throws Exception
    {
        // When
        database.encrypt(TEST_BYTES);

        // Then
        verify(cryptoReaderWriter).encrypt(memoryCryptoParams, TEST_BYTES);
        verifyNoMoreInteractions(cryptoReaderWriter);
    }

    @Test
    public void decrypt_usesCryptoReaderWriter() throws Exception
    {
        // When
        database.decrypt(encryptedValue);

        // Then
        verify(cryptoReaderWriter).decrypt(memoryCryptoParams, encryptedValue);
        verifyNoMoreInteractions(cryptoReaderWriter);
    }

    @Test
    public void decrypt_withCryptoParams_usesCryptoReaderWriter() throws Exception
    {
        // When
        database.decrypt(encryptedValue, cryptoParams);

        // Then
        verify(cryptoReaderWriter).decrypt(cryptoParams, encryptedValue);
        verifyNoMoreInteractions(cryptoReaderWriter);
    }

    @Test
    public void getFileCryptoParams_isReflected()
    {
        // When
        CryptoParams result = database.getFileCryptoParams();

        // Then
        assertEquals("Should be same as passed in", fileCryptoParams, result);
    }

    @Test
    public void updateFileCryptoParams_isReflected() throws Exception
    {
        // Given
        given(cryptoParamsFactory.clone(cryptoParams, PASSWORD_CHAR_ARRAY)).willReturn(cryptoParams2);

        // When
        database.updateFileCryptoParams(cryptoParams, PASSWORD_CHAR_ARRAY);

        // Then
        verify(cryptoParamsFactory).clone(cryptoParams, PASSWORD_CHAR_ARRAY);
        verifyNoMoreInteractions(cryptoParamsFactory);

        CryptoParams result = database.getFileCryptoParams();
        assertEquals("Should be crypto params from factory", cryptoParams2, result);
    }

    @Test
    public void updateMemoryCryptoParams_isReflected() throws Exception
    {
        // Given
        given(cryptoParamsFactory.clone(cryptoParams, PASSWORD_CHAR_ARRAY)).willReturn(cryptoParams2);

        // When
        database.updateMemoryCryptoParams(cryptoParams, PASSWORD_CHAR_ARRAY);

        // Then
        verify(cryptoParamsFactory).clone(cryptoParams, PASSWORD_CHAR_ARRAY);
        verifyNoMoreInteractions(cryptoParamsFactory);

        CryptoParams result = database.getMemoryCryptoParams();
        assertEquals("Should be crypto params from factory", cryptoParams2, result);
    }

    @Test
    public void updateMemoryCryptoParams_rebuildsCryptoOnNodes() throws Exception
    {
        // Given
        database.setRoot(node);

        // When
        database.updateMemoryCryptoParams(cryptoParams, PASSWORD_CHAR_ARRAY);

        // Then
        verify(node).rebuildCrypto(memoryCryptoParams);
    }

    @Test
    public void updateMemoryCryptoParams_setsDirty() throws Exception
    {
        // Given
        assertFalse("Database dirty flag should not be set", database.isDirty());

        // When
        database.updateMemoryCryptoParams(cryptoParams, PASSWORD_CHAR_ARRAY);

        // Then
        assertTrue("Database dirty flag should be set", database.isDirty());
    }

    @Test
    public void getMemoryCryptoParams_isReflected()
    {
        // When
        CryptoParams result = database.getMemoryCryptoParams();

        // Then
        assertEquals("Should be same as passed in", memoryCryptoParams, result);
    }

    @Test
    public void setDirty_isReflected()
    {
        // When
        database.setDirty(true);

        // Then
        assertTrue(database.isDirty());
    }

    @Test
    public void isDirty_initiallyFalse()
    {
        // Then
        assertFalse(database.isDirty());
    }

}
