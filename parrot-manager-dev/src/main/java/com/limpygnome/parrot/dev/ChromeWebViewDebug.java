package com.limpygnome.parrot.dev;

import com.limpygnome.parrot.lib.WebViewDebug;
import com.mohamnag.fxwebview_debugger.DevToolsDebuggerServer;
import com.sun.javafx.scene.web.Debugger;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * Sets up debugging for a {@link WebView} instance.
 *
 * Once running, visit the following URL from Chrome for devtools:
 * chrome-devtools://devtools/bundled/inspector.html?ws=localhost:51742/
 */
@Component
public class ChromeWebViewDebug implements WebViewDebug
{
    private static final int WEBVIEW_DEBUG_PORT = 51742;

    private static final Logger LOG = LoggerFactory.getLogger(ChromeWebViewDebug.class);

    @Override
    public void start(WebView webView)
    {
        try
        {
            Class webEngineClazz = WebEngine.class;

            Field debuggerField = webEngineClazz.getDeclaredField("debugger");
            debuggerField.setAccessible(true);

            Debugger debugger = (Debugger) debuggerField.get(webView.getEngine());
            DevToolsDebuggerServer.startDebugServer(debugger, WEBVIEW_DEBUG_PORT);

            LOG.debug("Debugging available: chrome-devtools://devtools/bundled/inspector.html?ws=localhost:51742/");
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to setup debugging for WebView", e);
        }
    }

    @Override
    public void stop()
    {
        try
        {
            DevToolsDebuggerServer.stopDebugServer();
        }
        catch (Exception e)
        {
            LOG.error("failed to stop debugging for WebView", e);
        }
    }

}
