package com.limpygnome.parrot.lib.urlStream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.URL;

/**
 * Serves assets from the class-path.
 */
@Qualifier("default")
@Component
public class ClasspathDetermineResourceHandler implements DetermineResourceHandler
{
    /*
        The base class path of where resources are read for requests.
     */
    private static final String LOCAL_RESOURCES_BASE_PATH = "webapp/";

    @Override
    public URL find(String relativePath)
    {
        URL resourceUrl = getClass().getClassLoader().getResource(LOCAL_RESOURCES_BASE_PATH + relativePath);
        return resourceUrl;
    }

}
