package com.limpygnome.parrot;

import com.limpygnome.parrot.service.server.CryptographyService;
import com.limpygnome.parrot.service.server.DatabaseIOService;

/**
 * The facade for holding instances of common (shared) runtime services.
 */
public class Controller
{
    private CryptographyService cryptographyService;
    private DatabaseIOService databaseIOService;

    public Controller()
    {
        this.cryptographyService = new CryptographyService();
        this.databaseIOService = new DatabaseIOService(this);
    }

    public CryptographyService getCryptographyService() {
        return cryptographyService;
    }

    public DatabaseIOService getDatabaseIOService() {
        return databaseIOService;
    }

}
