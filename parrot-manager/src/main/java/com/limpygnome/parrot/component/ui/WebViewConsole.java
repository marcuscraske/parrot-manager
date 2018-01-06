package com.limpygnome.parrot.component.ui;

import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs console messages.
 *
 * As of JDK 9, it's no longer possible to get detailed source information. Thus this is designed to override
 * console.log to invoke this object (exposed as JavaScript object).
 */
public class WebViewConsole
{
    private static final Logger LOG = LoggerFactory.getLogger(WebViewConsole.class);

    /*
     * Sets up stage with console logging.
     *
     * This instance must have a strong reference, thus should be stored as field in stage to avoid gc.
     *
     * @param stage stage
     */
    void setup(WebViewStage stage)
    {
        // expose this instance as object
        stage.exposeJsObject("logger", this);

        // override console.log and write a test/init message
        WebView webView = stage.getWebView();
        webView.getEngine().executeScript("console.log = function(m){ logger.log(m); }");
        webView.getEngine().executeScript("console.log('web console setup');");
    }

    /**
     * @param message message to be logged
     */
    public void log(String message)
    {
        LOG.info(message);
    }

}
