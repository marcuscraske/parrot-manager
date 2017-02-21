package com.limpygnome.parrot;

import com.limpygnome.parrot.component.WebStageInitComponent;
import com.limpygnome.parrot.config.AppConfig;
import com.limpygnome.parrot.ui.WebViewStage;
import com.limpygnome.parrot.ui.urlstream.ResourceUrlConfig;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;

/**
 * The entry-point into the application.
 */
public class Program extends Application
{
    private static String[] args;
    private WebStageInitComponent webStageInitComponent;

    public Program()
    {
        // Read args as values source
        CommandLinePropertySource cmdLineSource = new SimpleCommandLinePropertySource(args);

        // Setup Spring context
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.getEnvironment().getPropertySources().addFirst(cmdLineSource);
        applicationContext.register(AppConfig.class);
        applicationContext.refresh();

        // Create/fetch init component
        webStageInitComponent = applicationContext.getBean(WebStageInitComponent.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // Serve resources from class path
        ResourceUrlConfig resourceUrlConfig = new ResourceUrlConfig();
        resourceUrlConfig.enable(webStageInitComponent.isDevelopmentMode());

        // Create and show stage
        WebViewStage stage = new WebViewStage(webStageInitComponent);
        stage.show();
    }

    public static void main(String[] args)
    {
        // Store args
        Program.args = args;

        // Launch JavaFX app
        launch(args);
    }

}
