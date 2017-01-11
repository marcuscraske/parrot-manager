package com.limpygnome.parrot;

import com.limpygnome.parrot.ui.WebViewStage;
import com.limpygnome.parrot.ui.urlstream.ResourceUrlConfig;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The entry-point into the application.
 */
public class Program extends Application
{
    private Controller controller;

    public Program()
    {
        controller = new Controller();
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // Serve resources from class path
        ResourceUrlConfig resourceUrlConfig = new ResourceUrlConfig();
        resourceUrlConfig.enable();

        // Create and show stage
        WebViewStage stage = new WebViewStage(controller);
        stage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }

}
