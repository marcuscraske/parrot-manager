package com.limpygnome.parrot.service.rest;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.service.AbstractService;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
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
        if (newHeight < MINIMUM_WINDOW_HEIGHT_THRESHOLD)
        {
            throw new IllegalArgumentException("Height cannot be less than threshold - height: " + newHeight);
        }

        Scene scene = controller.getPresentationService().getScene();
        Window window = scene.getWindow();

        WebView webView = (WebView) scene.getRoot();

        int frameHeight = (int) window.getHeight() - ( (int) scene.getHeight() + (int) scene.getY() );
        scene.getWindow().setHeight(100 + frameHeight);

        System.out.println("CHANGING HEIGHT TO " + newHeight);
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
