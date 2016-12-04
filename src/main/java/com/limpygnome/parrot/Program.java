package com.limpygnome.parrot;

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
        // Start local Jetty server
        controller.getJettyService().start();

        // Setup scene
        Scene scene = controller.getPresentationService().createScene();
        primaryStage.setScene(scene);
        primaryStage.setTitle("parrot - version 1.x.x");

        // Hook to shutdown jetty on close
        primaryStage.setOnCloseRequest(event -> {
            try
            {
                controller.getJettyService().stop();
            }
            catch (Exception e) { }
        });

        // Show view
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }

}
