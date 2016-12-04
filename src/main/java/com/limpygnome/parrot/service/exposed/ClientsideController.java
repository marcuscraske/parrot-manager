package com.limpygnome.parrot.service.exposed;

/**
 * A controller exposed to the client-side application.
 */
public class ClientsideController
{

    /**
     * Creates database.
     */
    public void create(String location, String password, int rounds)
    {
    }

    /**
     * Exits the application.
     */
    public void exit()
    {
        // Just exit the application...
        System.exit(0);
    }

}
