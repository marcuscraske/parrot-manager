package com.limpygnome.parrot.library.crypto;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class EncryptedAesValueTest
{
    private static final long LAST_MODIFIED = 123;
    private static final byte[] IV = { 0x11, 0x11, 0x44 };
    private static final byte[] VALUE = { 0x55, 0x44, 0x33 };

    // SUT
    private EncryptedAesValue encryptedAesValue;

    @Before
    public void setup()
    {
        encryptedAesValue = new EncryptedAesValue(LAST_MODIFIED, IV, VALUE);
    }

    @Test
    public void getIv_isReflected()
    {
        // When
        byte[] iv = encryptedAesValue.getIv();

        // Then
        assertEquals("IV should be same as passed in", IV, iv);
    }

    @Test
    public void getValue_isReflected()
    {
        // When
        byte[] value = encryptedAesValue.getValue();

        // Then
        assertEquals("Value should be same as passed in", VALUE, value);
    }

    @Test
    public void clone_asExpected()
    {
        // When
        EncryptedAesValue clone = (EncryptedAesValue) encryptedAesValue.clone();

        // Then
        assertEquals("Identifier should be same", encryptedAesValue.getUuid(), clone.getUuid());
        assertArrayEquals("IV should be same", IV, clone.getIv());
        assertArrayEquals("Value should be same", VALUE, clone.getValue());
    }

    @Test
    public void equals_isTrueWhenSame()
    {
        // Given
        EncryptedAesValue similar = new EncryptedAesValue(LAST_MODIFIED, IV, VALUE);

        // When
        boolean isEqual = similar.equals(encryptedAesValue);

        // Then
        assertTrue("Should be equal as same params passed in", isEqual);
    }

    @Test
    public void equals_isFalseWhenDifferent()
    {
        // Given
        EncryptedAesValue similar = new EncryptedAesValue(LAST_MODIFIED, null, null);

        // When
        boolean isEqual = similar.equals(encryptedAesValue);

        // Then
        assertFalse("Should be equal as same params passed in", isEqual);
    }

}
