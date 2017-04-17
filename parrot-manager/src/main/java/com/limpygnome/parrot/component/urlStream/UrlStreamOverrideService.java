package com.limpygnome.parrot.component.urlStream;

import com.limpygnome.parrot.component.ui.WebStageInitService;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Used to redirect requests within the application to be served from the class-path.
 */
@Service
public class UrlStreamOverrideService
{
    private static final Logger LOG = LogManager.getLogger(UrlStreamOverrideService.class);

    @Autowired
    private WebStageInitService webStageInitService;

    /**
     * Enables local resources to be served by class path.
     *
     * Refer to {@link LocalResourceStreamHandler} for more information.
     */
    public synchronized void enable()
    {
        boolean developmentMode = webStageInitService.isDevelopmentMode();

        URLStreamHandlerFactory factory = new LocalUrlStreamHandlerFactory(developmentMode);
        URL.setURLStreamHandlerFactory(factory);

        LOG.info("resource URL configuration enabled - development mode: {}", developmentMode);
    }

}
