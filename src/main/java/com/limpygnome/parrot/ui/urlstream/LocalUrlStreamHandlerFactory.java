package com.limpygnome.parrot.ui.urlstream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * An implementation of {@link URLStreamHandlerFactory} for using {@link LocalResourceStreamHandler} to serve
 * HTTP requests.
 *
 * TODO: unit test
 */
class LocalUrlStreamHandlerFactory implements URLStreamHandlerFactory
{
    private static final Logger LOG = LogManager.getLogger(LocalUrlStreamHandlerFactory.class);

    /*
        Enables/disables development mode for LocalResourceStreamHandler
     */
    private final boolean developmentMode;

    LocalUrlStreamHandlerFactory(boolean developmentMode)
    {
        this.developmentMode = developmentMode;
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol)
    {
        URLStreamHandler handler = null;

        if ("http".equals(protocol))
        {
            handler = new LocalResourceStreamHandler(developmentMode);
        }
        else
        {
            LOG.error("request for unhandled protocol - proto: {}", protocol);
        }

        return handler;
    }

}
