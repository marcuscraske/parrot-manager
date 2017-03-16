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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
    private DatabaseNode node;
    @Mock
    private EncryptedValue encryptedValue;

    // Test data
    private final byte[] TEST_DATA = new byte[] { 0x11, 0x22, 0x33 };

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
        verify(cryptoParamsFactory).clone(fileCryptoParams, "foobar".toCharArray());
        verify(cryptoParamsFactory).clone(memoryCryptoParams, "foobar".toCharArray());
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
        database.encrypt(TEST_DATA);

        // Then
        verify(cryptoReaderWriter).encrypt(memoryCryptoParams, TEST_DATA);
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
    public void updateFileCryptoParams_isReflected()
    {
        // When
    }

    @Test
    public void updateMemoryCryptoParams_isReflected()
    {
    }

    @Test
    public void updateMemoryCryptoParams_rebuildsCryptoOnNodes()
    {
    }

    @Test
    public void updateMemoryCryptoParams_setsDirty()
    {
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
    }

    @Test
    public void isDirty_initiallyFalse()
    {
    }

}
