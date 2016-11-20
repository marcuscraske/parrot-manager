package com.limpygnome.parrot;

import com.limpygnome.parrot.service.AccessTokenService;
import com.limpygnome.parrot.service.DatabaseService;
import com.limpygnome.parrot.service.JettyService;

/**
 * The facade for holding all instances of services and components.
 */
public class Controller
{
    private JettyService jettyService;
    private DatabaseService databaseService;
    private AccessTokenService accessTokenService;

    public Controller()
    {
        this.jettyService = new JettyService();
        this.databaseService = new DatabaseService();
        this.accessTokenService = new AccessTokenService();
    }

    public JettyService getJettyService()
    {
        return jettyService;
    }

    public DatabaseService getDatabaseService()
    {
        return databaseService;
    }

    public AccessTokenService getAccessTokenService()
    {
        return accessTokenService;
    }

}
