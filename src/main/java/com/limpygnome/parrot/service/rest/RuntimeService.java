package com.limpygnome.parrot.service.rest;

import com.limpygnome.parrot.ui.WebViewStage;
import javafx.scene.Scene;
import javafx.stage.Window;

/**
 * REST service for runtime service.
 */
public class RuntimeService
{
    private final WebViewStage stage;

    public RuntimeService(WebViewStage stage)
    {
        this.stage = stage;
    }

    /**
     * Changes the height of the window.
     *
     * @param newHeight the new desired height
     */
    public void changeHeight(int newHeight)
    {
        // Fetch actual window
        Scene scene = stage.getScene();
        Window window = scene.getWindow();

        // Set the new height, factoring size of window frame
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
