package com.limpygnome.parrot.component.randomGenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * A archive for generating secure random values.
 */
@Service
public class RandomGeneratorService
{
    private static final Logger LOG = LogManager.getLogger(RandomGeneratorService.class);

    private static final char[] NUMBERS = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58 };

    private static final char[] UPPERCASE;
    private static final char[] LOWERCASE;
    private static final char[] SPECIAL_CHARS = {'£', '$', '^', '&', '*', '#', '@', '?' };

    static
    {
        // TODO: clean this up, move to descriptive constants...
        // Upper-case
        UPPERCASE = new char[90-65+1];
        for (int i = 65; i <= 90; i++)
        {
            UPPERCASE[i-65] = (char) i;
        }

        // Lower-case
        LOWERCASE = new char[122-97+1];
        for (int i = 97; i <= 122; i++)
        {
            LOWERCASE[i-97] = (char) i;
        }
    }

    private SecureRandom secureRandom;

    public RandomGeneratorService()
    {
        // TODO: review if seed is needed...
        secureRandom = new SecureRandom();
    }

    /**
     * Generates a random string, using characters from the enabled groups of chars.
     *
     * @param useNumbers Enables number chars
     * @param useUppercase Enables upper-case chars
     * @param useLowercase Enables lower-case chars
     * @param useSpecialChars Enables special chars
     * @param minLength The minimum length of the string
     * @param maxLength The maximum length of the string
     * @return the generated text, or null if invalid params
     */
    public String generate(boolean useNumbers, boolean useUppercase, boolean useLowercase, boolean useSpecialChars,
                           int minLength, int maxLength)
    {
        String result;

        if (!useNumbers && !useUppercase && !useLowercase && !useSpecialChars)
        {
            result = null;
            LOG.warn("failed to generate random string - no char groups selected");
        }
        else if (minLength > maxLength || minLength < 1)
        {
            result = null;
            LOG.warn("failed to generate random string - invalid min/max");
        }
        else
        {
            // Build array of possible chars
            final char[] possibleChars = getPossibleChars(useNumbers, useUppercase, useLowercase, useSpecialChars);

            // Generate random length
            int length;

            if (minLength == maxLength)
            {
                length = minLength;
            }
            else
            {
                length = minLength + secureRandom.nextInt(maxLength - minLength + 1);
            }

            // Pick random chars
            char[] randomText = new char[length];
            int randomIndex;

            for (int i = 0; i < length; i++)
            {
                randomIndex = secureRandom.nextInt(possibleChars.length);
                randomText[i] = possibleChars[randomIndex];
            }

            result = new String(randomText);
        }

        return result;
    }

    private char[] getPossibleChars(boolean useNumbers, boolean useUppercase, boolean useLowercase, boolean useSpecialChars)
    {
        // Calculate length required
        int totalChars = 0;

        if (useNumbers)         totalChars += NUMBERS.length;
        if (useUppercase)       totalChars += UPPERCASE.length;
        if (useLowercase)       totalChars += LOWERCASE.length;
        if (useSpecialChars)    totalChars += SPECIAL_CHARS.length;

        // Add chars
        char[] result = new char[totalChars];
        int position = 0;

        if (useNumbers)
        {
            System.arraycopy(NUMBERS, 0, result, position, NUMBERS.length);
            position += NUMBERS.length;
        }

        if (useUppercase)
        {
            System.arraycopy(UPPERCASE, 0, result, position, UPPERCASE.length);
            position += UPPERCASE.length;
        }

        if (useLowercase)
        {
            System.arraycopy(LOWERCASE, 0, result, position, LOWERCASE.length);
            position += LOWERCASE.length;
        }

        if (useSpecialChars)
        {
            System.arraycopy(SPECIAL_CHARS, 0, result, position, SPECIAL_CHARS.length);
            position += SPECIAL_CHARS.length;
        }

        // Sanity
        if (position != totalChars)
        {
            throw new RuntimeException("Failed to generate array of possible chars, this should never happen");
        }

        return result;
    }

}
