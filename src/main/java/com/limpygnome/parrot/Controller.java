package com.limpygnome.parrot;

import com.limpygnome.parrot.service.*;
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
    private CryptographyService cryptographyService;
    private DatabaseIOService databaseIOService;

    // Exposed
    private ClientsideController clientsideController;

    public Controller()
    {
        this.runtimeService = new RuntimeService(this);
        this.jettyService = new JettyService();
        this.databaseService = new DatabaseService();
        this.accessTokenService = new AccessTokenService();
        this.cryptographyService = new CryptographyService();
        this.databaseIOService = new DatabaseIOService(this);

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

    public CryptographyService getCryptographyService() {
        return cryptographyService;
    }

    public DatabaseIOService getDatabaseIOService() {
        return databaseIOService;
    }

    public ClientsideController getClientsideController()
    {
        return clientsideController;
    }

}
