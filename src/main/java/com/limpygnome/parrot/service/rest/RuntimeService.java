package com.limpygnome.parrot.service.rest;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.service.AbstractService;
import javafx.scene.Scene;

/**
 * REST service for runtime service.
 */
public class RuntimeService extends AbstractService
{
    private static final int MINIMUM_WINDOW_HEIGHT_THRESHOLD = 32;

    public RuntimeService(Controller controller)
    {
        super(controller);
    }

    /**
     * Changes the height of the window.
     *
     * @param newHeight the new desired height
     */
    public void changeHeight(int newHeight)
    {
        if (newHeight < MINIMUM_WINDOW_HEIGHT_THRESHOLD)
        {
            throw new IllegalArgumentException("Height cannot be less than threshold - height: " + newHeight);
        }

        Scene scene = controller.getPresentationService().getScene();
        scene.getWindow().setHeight(newHeight);
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
