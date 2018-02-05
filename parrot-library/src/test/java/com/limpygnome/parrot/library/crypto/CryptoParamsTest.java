package com.limpygnome.parrot.library.crypto;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.crypto.SecretKey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class CryptoParamsTest
{
    private static final byte[] SALT = { 0x12, 0x34, 0x56 };
    private static final long LAST_MODIFIED = 123456789;
    private static final int ROUNDS = 1001;

    // SUT
    private CryptoParams cryptoParams;

    // Mocks
    @Mock
    private SecretKey secretKey;

    @Before
    public void setup() throws Exception
    {
        cryptoParams = new CryptoParams(SALT, ROUNDS, LAST_MODIFIED, secretKey);
    }

    @Test
    public void getSalt_isReflected()
    {
        // When
        byte[] salt = cryptoParams.getSalt();

        // Then
        assertEquals("Salt is not reflected back", SALT, salt);
    }

    @Test
    public void getRounds_isReflected()
    {
        // When
        int rounds = cryptoParams.getRounds();

        // Then
        assertEquals("Rounds is not reflected back", ROUNDS, rounds);
    }

    @Test
    public void getLastModified_isReflected()
    {
        // When
        long lastModified = cryptoParams.getLastModified();

        // Then
        assertEquals("Last modified is not reflected back", LAST_MODIFIED, lastModified);
    }

    @Test
    public void getSecretKey_isReflected()
    {
        // When
        SecretKey secretKey = cryptoParams.getSecretKey();

        // Then
        assertEquals("Secret key is not reflected back", this.secretKey, secretKey);
    }

    @Test
    public void equals_trueWhenSame()
    {
        // Given
        CryptoParams instance = new CryptoParams(SALT, ROUNDS, LAST_MODIFIED, secretKey);

        // When/Then
        assertTrue("Should be true as same values", instance.equals(cryptoParams));
    }

    @Test
    public void equals_falseWhenNotSame()
    {
        // Given
        CryptoParams instance = new CryptoParams(SALT, ROUNDS, 0, secretKey);

        // When/Then
        assertFalse("Should be false as different values", instance.equals(cryptoParams));
    }

    @Test
    public void cloned()
    {
        // When
        CryptoParams clone = cryptoParams.clone();

        // THen
        assertEquals("Salt should be same", SALT, clone.getSalt());
        assertEquals("Rounds should be same", ROUNDS, clone.getRounds());
        assertEquals("Last modified should be same", LAST_MODIFIED, clone.getLastModified());
        assertEquals("Secret key should be same", secretKey, clone.getSecretKey());

        assertThat("Should be different object", clone, not(sameInstance(cryptoParams)));
    }

}
