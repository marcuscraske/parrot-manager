package com.limpygnome.parrot.lib.init;

import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * Used to initialize the web view, such as navigating to the starting URL / page.
 */
public interface WebViewInit
{

    /**
     * @param webView target to be setup
     */
    void init(Stage stage, WebView webView);

}
