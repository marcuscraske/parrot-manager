package com.limpygnome.parrot.lib.init;

import com.limpygnome.parrot.lib.urlStream.UrlStreamOverrideService;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Sandboxes the entire application from making external HTTP calls through {@link java.net.URLConnection}, with
 * assets also being served off the class-path.
 */
@Qualifier("default")
@Component
public class SandboxedWebViewInit implements WebViewInit
{
    /**
     * Default URL for served assets.
     */
    public static final String DEFAULT_INIT_URL = "http://localhost";

    @Autowired
    private UrlStreamOverrideService urlStreamOverrideService;

    @Override
    public void init(Stage stage, WebView webView)
    {
        // enable sandboxing
        urlStreamOverrideService.enable();

        // load initial URL
        webView.getEngine().load(DEFAULT_INIT_URL);
    }

}
