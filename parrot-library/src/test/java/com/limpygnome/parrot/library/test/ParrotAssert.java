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

            boolean outcome = expectedSet.equals(resultSet);

            if (outcome)
            {
                // Just assert true for stats
                assertTrue(message, outcome);
            }
            else
            {
                // Build useful message about failure
                StringBuilder diagnostics = new StringBuilder(message);

                // -- Append expected elements
                diagnostics.append("\nexpected: ").append(buildArray(expected));
                diagnostics.append("\nresult:   ").append(buildArray(result)).append("\n");

                // -- Append result elements

                // Fail the test with explanation...
                fail(diagnostics.toString());
            }
        }
        else if (expected != result)
        {
            fail(message + " - expected or result is null");
        }
    }

    private static String buildArray(Object[] elements)
    {
        String message;

        if (elements == null)
        {
            message = "null";
        }
        else if (elements.length == 0)
        {
            message = "empty";
        }
        else
        {
            StringBuilder buffer = new StringBuilder("{");
            for (Object obj : elements)
            {
                buffer.append(obj).append(",");
            }
            buffer.append("}").deleteCharAt(buffer.length() - 1);
            message = buffer.toString();
        }

        return message;
    }

}
