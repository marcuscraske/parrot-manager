package com.limpygnome.parrot.component.urlStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * A URL stream handler implementation to serve resources from the class-path.
 *
 * When a URL matches "http://localhost", the path of the request is used against the root of the resources.
 *
 * TODO: unit test
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

    /*
        The base file path of where resources are read for requests during development mode.

        This should be a path of actively transpiled assets.
     */
    private static final String DEVELOPMENT_RESOURCES_BASE_PATH = "src/main/resources/webapp/";

    /*
        The pattern for URLs to not be rewritten.
     */
    private static final String DONT_REWRITE_PATTERN = ".";

    /*
        The URL for rewritten requests when DONT_REWRITE_PATTTERN is not matched.

        This is for AngularJS routing.
     */
    private static final String REWRITTEN_URL = "http://localhost/index.html";

    /*
        Used to change the location from where resources are loaded.

        In development mode, the front-end UI is loaded from the resources directory, as to avoid the need to restart
        the application.
     */
    private final boolean developmentMode;

    LocalResourceStreamHandler(boolean developmentMode)
    {
        this.developmentMode = developmentMode;
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException
    {
        URLConnection connection = null;

        if (url != null)
        {
            String requestedUrl = url.toString();

            if (requestedUrl != null && requestedUrl.startsWith(LOCAL_URL_REQUESTS))
            {
                // See if to rewrite to index.html...
                if (!requestedUrl.contains(DONT_REWRITE_PATTERN)) {
                    requestedUrl = REWRITTEN_URL;
                }

                // Convert to relative path
                String classPathUrl = requestedUrl.substring(LOCAL_URL_REQUESTS.length());

                // Locate the resource and convert to a connection
                URL resourceUrl = determineResource(classPathUrl);

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

        if (connection == null && developmentMode)
        {
            LOG.warn("Unable to resolve resource, have you set the working directory to the root of the parrot-manager module?");
        }

        return connection;
    }

    private URL determineResource(String classPathUrl)
    {
        URL url;

        // Determine location of resource based on development mode
        if (developmentMode)
        {
            url = determineResourceDevelopment(classPathUrl);
        }
        else
        {
            url = determineResourceClassPath(classPathUrl);
        }

        return url;
    }

    private URL determineResourceClassPath(String classPathUrl)
    {
        URL resourceUrl = getClass().getClassLoader().getResource(LOCAL_RESOURCES_BASE_PATH + classPathUrl);
        return resourceUrl;
    }

    private URL determineResourceDevelopment(String classPathUrl)
    {
        URL resourceUrl = null;
        File file = new File(DEVELOPMENT_RESOURCES_BASE_PATH + classPathUrl);

        if (file.exists())
        {
            try
            {
                resourceUrl = file.toURI().toURL();
            }
            catch (MalformedURLException e)
            {
                LOG.debug("failed to locate development file", e);
            }
        }
        else
        {
            LOG.debug("failed to locate development file - path: {}", file.getAbsolutePath());
        }

        return resourceUrl;
    }

}
