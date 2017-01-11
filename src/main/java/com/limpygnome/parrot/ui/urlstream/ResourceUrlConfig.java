package com.limpygnome.parrot.ui.urlstream;

import java.net.URL;
import java.net.URLStreamHandlerFactory;

/**
 * Used to redirect requests within the application to be served from the class-path.
 */
public class ResourceUrlConfig
{

    /**
     * Enables local resources to be served by class path.
     *
     * Refer to {@link LocalResourceStreamHandler} for more information.
     */
    public void enable()
    {
        URLStreamHandlerFactory factory = new LocalUrlStreamHandlerFactory();
        URL.setURLStreamHandlerFactory(factory);
    }

}
