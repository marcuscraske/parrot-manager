package com.limpygnome.parrot.lib.urlStream;

import com.limpygnome.parrot.lib.init.SandboxedWebViewInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger(LocalResourceStreamHandler.class);

    /*
        The base URL for requests served by the class path.
     */
    private static final String LOCAL_URL_REQUESTS = SandboxedWebViewInit.DEFAULT_INIT_URL + "/";

    /*
        The pattern for URLs to not be rewritten.
     */
    private static final String DONT_REWRITE_PATTERN = ".";

    /*
        The URL for rewritten requests when DONT_REWRITE_PATTTERN is not matched.

        This is for AngularJS routing.
     */
    private static final String REWRITTEN_URL = SandboxedWebViewInit.DEFAULT_INIT_URL + "/index.html";

    private DetermineResourceHandler handler;

    LocalResourceStreamHandler(DetermineResourceHandler handler)
    {
        this.handler = handler;
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException
    {
        URLConnection connection = null;

        LOG.debug("request for url - {}", url);

        if (url != null)
        {
            String requestedUrl = url.toString();

            if (requestedUrl != null && requestedUrl.startsWith(LOCAL_URL_REQUESTS))
            {
                // See if to rewrite to index.html...
                if (!requestedUrl.contains(DONT_REWRITE_PATTERN))
                {
                    requestedUrl = REWRITTEN_URL;
                }

                // Convert to relative path
                String assetRelativeUrl = requestedUrl.substring(LOCAL_URL_REQUESTS.length());

                // Exclude query-string
                int queryStringIndex = assetRelativeUrl.indexOf("?");

                if (queryStringIndex > 0)
                {
                    assetRelativeUrl = assetRelativeUrl.substring(0, queryStringIndex - 1);
                }

                // Locate the resource and convert to a connection
                URL resourceUrl = handler.find(assetRelativeUrl);

                if (resourceUrl != null)
                {
                    connection = new CustomUrlConnection(resourceUrl);
                    LOG.info("served resource - url: {}, class path url: {}", requestedUrl, resourceUrl.getPath());
                }
                else
                {
                    LOG.error("resource not found - url: {}, class path url: {}", requestedUrl, assetRelativeUrl);
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
