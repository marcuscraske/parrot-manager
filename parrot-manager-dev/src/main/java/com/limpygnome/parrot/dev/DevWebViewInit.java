package com.limpygnome.parrot.dev;

import com.limpygnome.parrot.lib.init.WebViewInit;
import javafx.scene.web.WebView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Initializes web view with live node server when --live=true is specified as parameter for parrot. Otherwise
 * this will use the default {@link WebViewInit} bean.
 */
@Qualifier("dev")
@Component
public class DevWebViewInit implements WebViewInit
{
    private static final String NODE_SERVER_DEV_URL = "http://localhost:3000";

    @Autowired
    @Qualifier("default")
    private WebViewInit webViewInit;

    // Indicates whether to load assets off the class-path, rather than directly from node lite-server
    @Value("${classpath:false}")
    private Boolean classpathMode;

    @Override
    public void init(WebView webView)
    {
        if (classpathMode)
        {
            webViewInit.init(webView);
        }
        else
        {
            webView.getEngine().load(NODE_SERVER_DEV_URL);
        }
    }

}
