package com.limpygnome.parrot;

import com.limpygnome.parrot.service.server.CryptographyService;
import com.limpygnome.parrot.service.server.DatabaseIOService;

/**
 * The facade for holding instances of common (shared) runtime services.
 */
public class Controller
{
    // Services
    private CryptographyService cryptographyService;
    private DatabaseIOService databaseIOService;

    // Properties
    private boolean developmentMode;

    public Controller(boolean developmentMode)
    {
        this.cryptographyService = new CryptographyService();
        this.databaseIOService = new DatabaseIOService(this);

        this.developmentMode = developmentMode;
    }

    public CryptographyService getCryptographyService() {
        return cryptographyService;
    }

    public DatabaseIOService getDatabaseIOService() {
        return databaseIOService;
    }

    /**
     * @return indicates if running in development mode
     */
    public boolean isDevelopmentMode()
    {
        return developmentMode;
    }

}
