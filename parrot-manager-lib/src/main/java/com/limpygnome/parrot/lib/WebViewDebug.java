package com.limpygnome.parrot.lib;

import javafx.scene.web.WebView;

/**
 * Interface for separating implementation of debug/dev classes for {@link WebView} instances.
 */
public interface WebViewDebug
{

    /**
     * @param webView instance for debugging
     */
    void start(WebView webView);

    /**
     * stops debugging.
     */
    void stop();

}
