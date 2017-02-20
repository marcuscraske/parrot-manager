package com.limpygnome.parrot;

import com.limpygnome.parrot.config.AppConfig;
import com.limpygnome.parrot.ui.WebViewStage;
import com.limpygnome.parrot.ui.urlstream.ResourceUrlConfig;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.stream.Stream;

/**
 * The entry-point into the application.
 */
public class Program extends Application
{
    private static boolean developmentMode = false;

    private Controller controller;

    public Program()
    {
        // Create new controller using Spring
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        controller = applicationContext.getBean(Controller.class, developmentMode);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // Serve resources from class path
        ResourceUrlConfig resourceUrlConfig = new ResourceUrlConfig();
        resourceUrlConfig.enable(developmentMode);

        // Create and show stage
        WebViewStage stage = new WebViewStage(controller);
        stage.show();
    }

    public static void main(String[] args)
    {
        Stream argStream = Stream.of(args);

        // Read params
        developmentMode = argStream.anyMatch(s -> "--development".equals(s));

        // Launch JavaFX app
        launch(args);
    }

}
