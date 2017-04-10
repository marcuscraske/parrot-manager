package com.limpygnome.parrot.library.crypto;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.crypto.SecretKey;
import java.security.SecureRandom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CryptoFactoryTest
{

    // SUT
    private CryptoFactory cryptoFactory;

    // Mock objects
    @Mock
    private SecureRandom random;

    @Before
    public void setup()
    {
        cryptoFactory = new CryptoFactory(random);
    }

    @Test
    public void generateRandomSalt_invokesRandomCorrectly()
    {
        // When
        cryptoFactory.generateRandomSalt();

        // Then
        verify(random).nextInt(33);
    }

    @Test
    public void generateRandomSalt_fetchesRandomBytes()
    {
        // Given
        given(random.nextInt(33)).willReturn(4);

        // When
        byte[] result = cryptoFactory.generateRandomSalt();

        // Then
        ArgumentCaptor<byte[]> captor =  ArgumentCaptor.forClass(byte[].class);
        verify(random).nextBytes(captor.capture());

        byte[] valuePassedToRandom = captor.getValue();
        assertEquals("Captured value matches result", valuePassedToRandom, result);
    }

    @Test
    public void createSecretKey_isInstance() throws Exception
    {
        // When
        SecretKey secretKey = cryptoFactory.createSecretKey(
                "foobar".toCharArray(),
                new byte[]{ 0x1, 0x2, 0x3},
                123
        );

        // Then
        assertNotNull("Secret key instance should have been returned", secretKey);
    }

}
