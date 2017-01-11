package com.limpygnome.parrot.ui.urlstream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * A URL stream handler implementation to serve resources from the class-path.
 *
 * When a URL matches "http://localhost", the path of the request is used against the root of the resources.
 */
class LocalResourceStreamHandler extends URLStreamHandler
{
    private static final Logger LOG = LogManager.getLogger(LocalResourceStreamHandler.class);

    /*
        The base URL for requests served by the class path.
     */
    private static final String LOCAL_URL_REQUESTS = "http://localhost/";

    /*
        The base class path of where resources are read for requests.
     */
    private static final String LOCAL_RESOURCES_BASE_PATH = "webapp/";

    @Override
    protected URLConnection openConnection(URL url) throws IOException
    {
        URLConnection connection = null;

        if (url != null)
        {
            String requestedUrl = url.toString();

            if (requestedUrl != null && requestedUrl.startsWith(LOCAL_URL_REQUESTS))
            {
                String classPathUrl = requestedUrl.substring(LOCAL_URL_REQUESTS.length());

                URL resourceUrl = getClass().getClassLoader().getResource(LOCAL_RESOURCES_BASE_PATH + classPathUrl);

                if (resourceUrl != null)
                {
                    connection = resourceUrl.openConnection();
                    LOG.info("served resource - url: {}, class path url: {}", requestedUrl, classPathUrl);
                }
                else
                {
                    LOG.error("resource not found - url: {}, class path url: {}", requestedUrl, classPathUrl);
                }
            }
            else
            {
                LOG.error("invalid or non-matched URL, ignoring - url: {}", requestedUrl);
            }
        }
        else
        {
            LOG.error("unable to serve request as URL is null");
        }

        return connection;
    }

}
