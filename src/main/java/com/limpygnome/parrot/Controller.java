package com.limpygnome.parrot;

import com.limpygnome.parrot.service.AccessTokenService;
import com.limpygnome.parrot.service.DatabaseService;
import com.limpygnome.parrot.service.JettyService;
import com.limpygnome.parrot.service.RuntimeService;
import com.limpygnome.parrot.service.exposed.ClientsideController;

/**
 * The facade for holding instances of all services and components.
 */
public class Controller
{
    private RuntimeService runtimeService;
    private JettyService jettyService;
    private DatabaseService databaseService;
    private AccessTokenService accessTokenService;

    // Exposed
    private ClientsideController clientsideController;

    public Controller()
    {
        this.runtimeService = new RuntimeService(this);
        this.jettyService = new JettyService();
        this.databaseService = new DatabaseService();
        this.accessTokenService = new AccessTokenService();

        this.clientsideController = new ClientsideController();
    }

    public RuntimeService getRuntimeService()
    {
        return runtimeService;
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

    public ClientsideController getClientsideController()
    {
        return clientsideController;
    }

}
