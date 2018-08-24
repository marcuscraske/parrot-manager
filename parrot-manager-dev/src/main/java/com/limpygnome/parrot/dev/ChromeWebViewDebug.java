package com.limpygnome.parrot.dev;

import com.limpygnome.parrot.lib.WebViewDebug;
import com.sun.javafx.scene.web.Debugger;
import com.vladsch.javafx.webview.debugger.DevToolsDebuggerJsBridge;
import com.vladsch.javafx.webview.debugger.DevToolsDebuggerServer;
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

    private DevToolsDebuggerServer bridge;

    @Override
    public void start(WebView webView)
    {
        try
        {
            Class webEngineClazz = WebEngine.class;

            Field debuggerField = webEngineClazz.getDeclaredField("debugger");
            debuggerField.setAccessible(true);

            Debugger debugger = (Debugger) debuggerField.get(webView.getEngine());
            bridge = new DevToolsDebuggerServer(debugger, WEBVIEW_DEBUG_PORT, 0, null, null);

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
            bridge.stopDebugServer(null);
        }
        catch (Exception e)
        {
            LOG.error("failed to stop debugging for WebView", e);
        }
    }

}
