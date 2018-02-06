package com.limpygnome.parrot.dev;

import com.limpygnome.parrot.lib.init.WebViewInit;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Initializes web view with live node server when --live=true is specified as parameter for parrot. Otherwise
 * this will use the default {@link WebViewInit} bean.
 */
@Qualifier("dev")
@Component
public class DevWebViewInit implements WebViewInit
{
    private static final Logger LOG = LoggerFactory.getLogger(DevWebViewInit.class);

    private static final String NODE_SERVER_DEV_URL = "http://localhost:4200";

    @Autowired
    @Qualifier("default")
    private WebViewInit webViewInit;

    // Indicates whether to load assets off the class-path, rather than directly from node lite-server
    @Value("${classpath:false}")
    private Boolean classpathMode;

    @Override
    public void init(Stage stage, WebView webView)
    {
        // Setup web view
        if (classpathMode)
        {
            webViewInit.init(stage, webView);
        }
        else
        {
            webView.getEngine().load(NODE_SERVER_DEV_URL);
        }

        // Hook special key combo for taking screenshots
        stage.addEventFilter(KeyEvent.KEY_PRESSED, event ->
        {
            if (event.isControlDown() && event.getText().equals("."))
            {
                try
                {
                    // Grab screenshot
                    Robot robot = new Robot();
                    Rectangle rect = new Rectangle((int) stage.getX(), (int) stage.getY(), (int) stage.getWidth(), (int) stage.getHeight());
                    BufferedImage image = robot.createScreenCapture(rect);

                    // Read app version
                    BufferedReader br = new BufferedReader(new FileReader("pom.xml"));
                    String version = null;
                    String line;
                    Pattern pattern = Pattern.compile(".+<version>(.+)<.+");

                    while (version == null && (line = br.readLine()) != null)
                    {
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.matches())
                        {
                            version = matcher.group(1);
                        }
                    }

                    if (version == null)
                    {
                        version = "unknown";
                    }

                    // Create folder
                    File screenshotDir = new File("docs/screenshots/" + version);
                    if (!screenshotDir.exists() && !screenshotDir.mkdir())
                    {
                        throw new RuntimeException("Screenshot dir parent does not exist or could not create - path: " + screenshotDir.getAbsolutePath());
                    }

                    // Write to disk
                    File file = new File(screenshotDir, System.currentTimeMillis() + ".png");
                    ImageIO.write(image, "png", file);

                    LOG.info("screenshot taken - path: {}", file.getAbsolutePath());
                }
                catch (Exception e)
                {
                    LOG.error("failed to take dev screenshot", e);
                }
            }
        });
    }

}
