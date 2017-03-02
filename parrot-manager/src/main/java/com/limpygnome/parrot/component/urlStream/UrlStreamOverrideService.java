package com.limpygnome.parrot.component.urlStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.net.URLStreamHandlerFactory;

/**
 * Used to redirect requests within the application to be served from the class-path.
 *
 * TODO: unit test
 */
public class UrlStreamOverrideService
{
    private static final Logger LOG = LogManager.getLogger(UrlStreamOverrideService.class);

    /**
     * Enables local resources to be served by class path.
     *
     * Refer to {@link LocalResourceStreamHandler} for more information.
     *
     * @param developmentMode enables/disables development mode, which affects the location from where resources are loaded
     */
    public void enable(boolean developmentMode)
    {
        URLStreamHandlerFactory factory = new LocalUrlStreamHandlerFactory(developmentMode);
        URL.setURLStreamHandlerFactory(factory);

        LOG.info("resource URL configuration enabled - development mode: {}", developmentMode);
    }

}
