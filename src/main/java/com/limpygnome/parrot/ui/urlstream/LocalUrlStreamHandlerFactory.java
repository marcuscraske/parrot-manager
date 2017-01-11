package com.limpygnome.parrot.ui.urlstream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * An implementation of {@link URLStreamHandlerFactory} for using {@link LocalResourceStreamHandler} to serve
 * HTTP requests.
 */
public class LocalUrlStreamHandlerFactory implements URLStreamHandlerFactory
{
    private static final Logger LOG = LogManager.getLogger(LocalUrlStreamHandlerFactory.class);

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol)
    {
        URLStreamHandler handler = null;

        if ("http".equals(protocol))
        {
            handler = new LocalResourceStreamHandler();
        }
        else
        {
            LOG.error("request for unhandled protocol - proto: {}", protocol);
        }

        return handler;
    }

}
