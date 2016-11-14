package com.limpygnome.parrot.service;

import java.util.Base64;
import java.util.Random;

/**
 * Controls access to REST API based on access tokens.
 */
public class AccessTokenService
{
    private static final String randomAccessToken;

    static
    {
        // Generate 32 random bytes
        Random rand = new Random(System.currentTimeMillis());
        byte[] randomBytes = new byte[32];
        rand.nextBytes(randomBytes);

        // Convert to base64 string as our token
        randomAccessToken = Base64.getEncoder().encodeToString(randomBytes);

        System.out.println("Random access token: " + randomAccessToken);
    }

    /**
     * @return randomly-generated access token for this session.
     */
    public String getRandomAccessToken()
    {
        return randomAccessToken;
    }

}
