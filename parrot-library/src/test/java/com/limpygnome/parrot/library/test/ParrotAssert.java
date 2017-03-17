package com.limpygnome.parrot.library.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Custom assertions.
 */
public class ParrotAssert
{

    /**
     * Asserts two arrays contain the same objects.
     *
     * @param message message for when assertion fails
     * @param expected expected array
     * @param result result array
     */
    public static void assertArrayContentsEqual(String message, Object[] expected, Object[] result)
    {
        if (expected != null && result != null)
        {
            Set<Object> expectedSet = expected != null ? new HashSet<>(Arrays.asList(expected)) : null;
            Set<Object> resultSet = result != null ? new HashSet<>(Arrays.asList(result)) : null;

            assertTrue(message, expectedSet.equals(resultSet));
        }
        else if (expected != result)
        {
            fail(message + " - expected or result is null");
        }
    }

}
