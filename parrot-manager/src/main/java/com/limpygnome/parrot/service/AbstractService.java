package com.limpygnome.parrot.service;

import com.limpygnome.parrot.Controller;

/**
 * Common class for all services.
 */
public class AbstractService
{
    protected final Controller controller;

    protected AbstractService(Controller controller)
    {
        this.controller = controller;
    }

}
