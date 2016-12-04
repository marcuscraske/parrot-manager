package com.limpygnome.parrot;

import com.limpygnome.parrot.service.server.AccessTokenService;
import com.limpygnome.parrot.service.server.CryptographyService;
import com.limpygnome.parrot.service.server.DatabaseIOService;
import com.limpygnome.parrot.service.server.JettyService;
import com.limpygnome.parrot.service.server.PresentationService;

/**
 * The facade for holding instances of all services and components.
 */
public class Controller
{
    private PresentationService presentationService;
    private JettyService jettyService;
    private AccessTokenService accessTokenService;
    private CryptographyService cryptographyService;
    private DatabaseIOService databaseIOService;

    public Controller()
    {
        this.presentationService = new PresentationService(this);
        this.jettyService = new JettyService();
        this.accessTokenService = new AccessTokenService();
        this.cryptographyService = new CryptographyService();
        this.databaseIOService = new DatabaseIOService(this);
    }

    public PresentationService getPresentationService()
    {
        return presentationService;
    }

    public JettyService getJettyService()
    {
        return jettyService;
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

}
