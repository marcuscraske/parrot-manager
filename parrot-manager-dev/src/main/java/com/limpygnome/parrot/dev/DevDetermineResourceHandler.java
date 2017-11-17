package com.limpygnome.parrot.dev;

import com.limpygnome.parrot.lib.urlStream.DetermineResourceHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Resource handler for loading assets from the file-system, rather than from within the class-path.
 */
@Qualifier("dev")
@Component
public class DevDetermineResourceHandler implements DetermineResourceHandler
{
    private static final Logger LOG = LogManager.getLogger(DevDetermineResourceHandler.class);

    /*
        The base file path of where resources are read for requests during development mode.

        This should be a path of actively transpiled assets.
     */
    private static final String DEVELOPMENT_RESOURCES_BASE_PATH = "src/main/resources/webapp/";

    @Override
    public URL find(String relativePath)
    {
        URL resourceUrl = null;
        File file = new File(DEVELOPMENT_RESOURCES_BASE_PATH + relativePath);

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
            LOG.warn("failed to locate development file - path: {}", file.getAbsolutePath());
            LOG.warn("have you set the working directory to the root of the parrot-manager module?");
        }

        return resourceUrl;
    }

}
