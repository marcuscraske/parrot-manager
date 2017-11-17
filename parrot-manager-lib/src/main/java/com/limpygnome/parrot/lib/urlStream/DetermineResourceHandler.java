package com.limpygnome.parrot.lib.urlStream;

import java.net.URL;

/**
 * Used to determine the {@link URL} to assets.
 */
public interface DetermineResourceHandler
{

    /**
     * @param relativePath relative path of asset
     * @return URL to path, or null if not found/valid
     */
    URL find(String relativePath);

}
