package com.limpygnome.parrot.lib.urlStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.net.URLStreamHandlerFactory;

/**
 * Used to redirect requests within the application to be served from the class-path.
 */
@Service
public class UrlStreamOverrideService
{
    private static final Logger LOG = LoggerFactory.getLogger(UrlStreamOverrideService.class);

    @Qualifier("default")
    @Autowired
    private DetermineResourceHandler handlerDefault;

    @Qualifier("dev")
    @Autowired(required = false)
    private DetermineResourceHandler handlerDev;

    /**
     * Enables local resources to be served by class path.
     *
     * Refer to {@link LocalResourceStreamHandler} for more information.
     */
    public synchronized void enable()
    {
        // TODO ugly, improve later
        DetermineResourceHandler handler = handlerDev != null ? handlerDev : handlerDefault;
        URLStreamHandlerFactory factory = new LocalUrlStreamHandlerFactory(handler);
        URL.setURLStreamHandlerFactory(factory);

        LOG.info("URL stream overriding enabled");
    }

}
