package com.limpygnome.parrot.library.crypto;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.crypto.SecretKey;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CryptoParamsFactoryTest
{
    private static final char[] PASSWORD = "foobar".toCharArray();
    private static final byte[] SALT = { 0x55, 0x77 };
    private static final int ROUNDS = 123;
    private static final long LAST_MODIFIED = 987654321;

    // SUT
    private CryptoParamsFactory cryptoParamsFactory;

    // Mock objects
    @Mock
    private CryptoFactory cryptoFactory;
    @Mock
    private SecretKey secretKey;

    @Before
    public void setup()
    {
        cryptoParamsFactory = new CryptoParamsFactory(cryptoFactory);
    }

    @Test
    public void create_withoutSalt_usesCryptoFactoryForSalt() throws Exception
    {
        // When
        cryptoParamsFactory.create(null, 0, 0);

        // Then
        verify(cryptoFactory).generateRandomSalt();
    }

    @Test
    public void create_usesCryptoFaactoryForSecretKey() throws Exception
    {
        // When
        cryptoParamsFactory.create(PASSWORD, ROUNDS, 0, SALT);

        // Then
        verify(cryptoFactory).createSecretKey(PASSWORD, SALT, ROUNDS);
    }

    @Test
    public void create_asExpected() throws Exception
    {
        // Given
        given(cryptoFactory.createSecretKey(PASSWORD, SALT, ROUNDS)).willReturn(secretKey);

        // When
        CryptoParams cryptoParams = cryptoParamsFactory.create(PASSWORD, ROUNDS, LAST_MODIFIED, SALT);

        // Then
        assertEquals("Incorrect salt", SALT, cryptoParams.getSalt());
        assertEquals("Incorrect rounds", ROUNDS, cryptoParams.getRounds());
        assertEquals("Incorrect last modified", LAST_MODIFIED, cryptoParams.getLastModified());
        assertEquals("Incorrect secret key", secretKey, cryptoParams.getSecretKey());
    }

    @Test
    public void clone_asExpected() throws Exception
    {
        // Given
        CryptoParams cryptoParams = new CryptoParams(SALT, ROUNDS, LAST_MODIFIED, secretKey);
        given(cryptoFactory.createSecretKey(PASSWORD, SALT, ROUNDS)).willReturn(secretKey);

        // When
        CryptoParams clone = cryptoParamsFactory.clone(cryptoParams, PASSWORD);

        // Then
        assertArrayEquals("Salt is incorrect", SALT, clone.getSalt());
        assertEquals("Rounds is incorrect", ROUNDS, clone.getRounds());
        assertEquals("Last modified is incorrect", LAST_MODIFIED, clone.getLastModified());
        assertEquals("Secret key is incorrect", secretKey, clone.getSecretKey());
    }

}
