package com.limpygnome.parrot.lib.urlStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * An implementation of {@link URLStreamHandlerFactory} for using {@link LocalResourceStreamHandler} to serve
 * HTTP requests.
 */
class LocalUrlStreamHandlerFactory implements URLStreamHandlerFactory
{
    private static final Logger LOG = LoggerFactory.getLogger(LocalUrlStreamHandlerFactory.class);

    private DetermineResourceHandler handler;

    LocalUrlStreamHandlerFactory(DetermineResourceHandler handler)
    {
        this.handler = handler;
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol)
    {
        URLStreamHandler streamHandler = null;

        if ("http".equals(protocol))
        {
            streamHandler = new LocalResourceStreamHandler(handler);
        }
        else
        {
            LOG.error("request for unhandled protocol - proto: {}", protocol);
        }

        return streamHandler;
    }

}
