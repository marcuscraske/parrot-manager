package com.limpygnome.parrot.service.server;

import com.limpygnome.parrot.Controller;
import javafx.application.Platform;

import java.io.IOException;
import java.net.*;

/**
 * A service for serving resources to the application through a mock URL connection handler.
 *
 * In the past this was designed to use Jetty, hence may need to re-visit design.
 */
public class ResourcesService
{

    public void start(Controller controller) throws Exception
    {
        setupFakeConnectionHandling(controller);
    }

    public void stop() throws Exception
    {
        // Do nothing...
    }

    private void setupFakeConnectionHandling(Controller controller)
    {
        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory()
        {
            @Override
            public URLStreamHandler createURLStreamHandler(String protocol)
            {
                // Only handle HTTP URLs...
                if (!protocol.equals("http"))
                {
                    System.err.println("IGNORING PROTO: " + protocol);
                    return null;
                }

                // Stream content from class path
                // TODO: onsider if should actually use new here
                return new URLStreamHandler()
                {
                    @Override
                    protected URLConnection openConnection(URL u) throws IOException
                    {
                        System.err.println("REQUESTING: " + u.toString());
                        String url = u.toString();
                        url = "webapp" + url.replace("http://localhost:8123", "");

                        System.err.println("translating to: " + url);

                        URL clazzpath = getClass().getClassLoader().getResource(url);
                        return clazzpath.openConnection();
                    }
                };
            }
        });
    }

}
