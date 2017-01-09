package com.limpygnome.parrot.service.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

import java.net.InetSocketAddress;

/**
 * Created by limpygnome on 14/11/16.
 */
public class JettyService
{
    private Server server;

    public void start() throws Exception
    {
        server = new Server(new InetSocketAddress("localhost", 8123));

        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setBaseResource(Resource.newClassPathResource("/webapp"));

        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(new Handler[]{ resourceHandler });
        server.setHandler(handlerList);

        //server.start();
    }

    public void stop() throws Exception
    {
        //server.stop();
    }

}
