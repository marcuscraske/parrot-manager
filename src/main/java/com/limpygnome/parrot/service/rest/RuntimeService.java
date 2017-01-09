package com.limpygnome.parrot.service.rest;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.service.AbstractService;
import javafx.scene.Scene;
import javafx.stage.Window;

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
        Scene scene = controller.getPresentationService().getScene();

        Window window = scene.getWindow();

        int targetHeight = newHeight + (int) scene.getY() + 1;

        if (targetHeight > 0)
        {
            window.setHeight(targetHeight);
            System.out.println("CHANGING HEIGHT TO " + targetHeight);
        }
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
