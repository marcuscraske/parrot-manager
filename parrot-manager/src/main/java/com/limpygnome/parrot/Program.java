package com.limpygnome.parrot;

import com.limpygnome.parrot.component.ui.WebStageInitService;
import com.limpygnome.parrot.config.AppConfig;
import com.limpygnome.parrot.component.ui.WebViewStage;
import com.limpygnome.parrot.component.urlStream.UrlStreamOverrideService;
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

    // Spring beans
    private UrlStreamOverrideService urlStreamOverrideService;
    private WebStageInitService webStageInitService;

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
        webStageInitService = applicationContext.getBean(WebStageInitService.class);
        urlStreamOverrideService = applicationContext.getBean(UrlStreamOverrideService.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // Sandbox class-path to serve only local files; prevents external requests as well
        urlStreamOverrideService.enable();

        // Create and show stage
        WebViewStage stage = new WebViewStage(webStageInitService);
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
