package com.limpygnome.parrot.library.crypto;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class EncryptedValueTest
{
    private static final long LAST_MODIFIED = 1490127843L * 1000L;

    private TestEncryptedValue encryptedValue;

    @Before
    public void setup()
    {
        encryptedValue = new TestEncryptedValue(LAST_MODIFIED);
    }

    @Test
    public void getLastModified_isReflected()
    {
        // When
        long lastModified = encryptedValue.getLastModified();

        // Then
        assertEquals("Last modified should be same as reflected", LAST_MODIFIED, lastModified);
    }

    @Test
    public void getFormattedLastModified_isCorrect()
    {
        // When
        String text = encryptedValue.getFormattedLastModified();

        // Then
        assertEquals("Text should be formatted the same", "21-03-2017 20:24:03", text);
    }

    @Test
    public void equals_isTrueWhenSame()
    {
        // Given
        TestEncryptedValue similar = new TestEncryptedValue(LAST_MODIFIED);

        // When
        boolean isEqual = similar.equals(encryptedValue);

        // Then
        assertTrue("Should be equal constructed the same", isEqual);
    }

    @Test
    public void equals_isFalseWhenDifferent()
    {
        // Given
        TestEncryptedValue similar = new TestEncryptedValue(1234);

        // When
        boolean isEqual = similar.equals(encryptedValue);

        // Then
        assertFalse("Should not be the same as constructed differently", isEqual);
    }

    // Test instance
    static class TestEncryptedValue extends EncryptedValue
    {
        public TestEncryptedValue(long lastModified)
        {
            super(lastModified);
        }

        @Override
        public EncryptedValue clone()
        {
            return null;
        }
    }

}
