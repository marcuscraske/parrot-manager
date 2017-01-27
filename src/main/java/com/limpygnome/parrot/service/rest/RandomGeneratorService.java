package com.limpygnome.parrot.service.rest;

import java.security.SecureRandom;

/**
 * A REST-only service of useful functions for randomly generating values.
 */
public class RandomGeneratorService
{

    private static final char[] NUMBERS = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58 };

    private static final char[] UPPERCASE;
    private static final char[] LOWERCASE;
    private static final char[] SPECIAL_CHARS = {'£', '$', '^', '&', '*', '#', '@', '?' };

    static
    {
        // TODO: clean this up, move to descriptive constants...
        // Upper-case
        UPPERCASE = new char[90-65];
        for (int i = 65; i <= 90; i++)
        {
            UPPERCASE[i] = (char) i;
        }

        // Lower-case
        LOWERCASE = new char[122-97];
        for (int i = 97; i <= 122; i++)
        {
            LOWERCASE[i] = (char) i;
        }
    }

    private SecureRandom secureRandom;

    public RandomGeneratorService()
    {
        // TODO: review if seed is needed...
        secureRandom = new SecureRandom();
    }

    public char[] generate(boolean useNumbers, boolean useUppercase, boolean useLowercase, boolean useSpecialChars,
                           int minLength, int maxLength)
    {
        char[] result;

        if (maxLength < minLength)
        {
            result = null;
        }
        else
        {
            // Build array of possible chars
            final char[] possibleChars;

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
            result = new char[length];
            for (int i = 0; i < length; i++)
            {
                possibleChars[i] = secureRandom.nextInt(possibleChars.length);
            }
        }

        return result;
    }

}
