package com.limpygnome.parrot.component.ui;

import com.limpygnome.parrot.lib.WebViewDebug;
import com.limpygnome.parrot.lib.init.WebViewInit;
import java.io.InputStream;

//import com.sun.javafx.webkit.WebConsoleListener;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A JavaFX web view stage.
 *
 * - Debugging for web view
 * - Native context menus
 * - Injects POJOs as JavaScript objects
 */
public class WebViewStage extends Stage
{
    private static final Logger LOG = LogManager.getLogger(WebViewStage.class);

    private WebStageInitService webStageInitService;
    private ContextMenuHandler contextMenuHandler;
    private WebViewConsole console;

    // JavaFX controls
    private Scene scene;
    private WebView webView;

    public WebViewStage(WebStageInitService webStageInitService)
    {
        this.webStageInitService = webStageInitService;

        // setup webview
        webView = new WebView();

        setupDebugging(webStageInitService);
        setupContextMenu();
        setupClientsideHooks();

        // set initial background
        WebEngine engine = webView.getEngine();
        engine.loadContent("<html><head><style>body{ background: #333; }</style></head></html>");

        // initialize webview (load page)
        WebViewInit webViewInit = webStageInitService.getWebViewInit();
        webViewInit.init(webView);

        // build scene for web view
        scene = new Scene(webView, Color.valueOf("#333333"));

        // general window config
        setScene(scene);
        setTitle("parrot manager");
        setWidth(1200.0);
        setHeight(550.0);
        setMaximized(true);

        // setup icons
        addIcon("/icons/parrot-icon.png");
        addIcon("/icons/parrot-icon-64.png");
        addIcon("/icons/parrot-icon-512.png");
        addIcon("/icons/parrot.svg");

        // prevent window from closing to prevent data loss for dirty databases
        setOnCloseRequest(new CloseHandler(this));
    }

    private void addIcon(String path)
    {
        InputStream iconStream = getClass().getResourceAsStream(path);

        if (iconStream == null)
        {
            throw new RuntimeException("Unable to add icon - path: " + path);
        }

        Image icon = new Image(iconStream);
        getIcons().add(icon);
        LOG.debug("added icon - path: {}", path);
    }

    private void setupDebugging(WebStageInitService webStageInitService)
    {
        WebEngine engine = webView.getEngine();

        // override to provide basic logging of console messages
        console = new WebViewConsole();
        console.setup(this);

        // monitor navigation
        engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            LOG.info("WebView state has changed - old: {}, new: {}", oldValue, newValue);
        });

        // log errors
        engine.setOnError(event -> {
            LOG.error("wehview error - message: {}", event.getMessage(), event.getException());
        });

        // hook debugging component (if available)
        WebViewDebug webViewDebug = webStageInitService.getWebViewDebug();

        if (webViewDebug != null)
        {
            webViewDebug.start(webView);
        }
    }

    private void setupContextMenu()
    {
        contextMenuHandler = new ContextMenuHandler(this);
        contextMenuHandler.attach();
    }

    /**
     * Sets up client-side hooks on web view.
     */
    protected void setupClientsideHooks()
    {
        // Setup hooks after each navigation
        webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) ->
        {
            if (newValue == Worker.State.SUCCEEDED)
            {
                // Ensure this runs on the JavaFX thread...
                Platform.runLater(() ->
                {
                    // Attach controller to this stage
                    // WARNING: due to JDK bug, do not pass newly constructed instances here...
                    webStageInitService.attach(this);

                    LOG.info("injected REST POJOs into window");
                });
            }
        });
    }

    /**
     * Exposes a POJO object as a field on 'window' for use by JavaScript.
     *
     * @param variableName
     * @param object
     */
    public void exposeJsObject(String variableName, Object object)
    {
        JSObject obj = (JSObject) webView.getEngine().executeScript("window");
        obj.setMember(variableName, object);

        LOG.info("injected POJO as JS object - var name: {}, class: {}", variableName, object.getClass());
    }

    /**
     * Triggers an event on the specified element.
     *
     * @param domElement the element e.g. document
     * @param eventName the event name e.g. nativeExit
     */
    public void triggerEvent(String domElement, String eventName, Object eventData)
    {
        Platform.runLater(() -> {

            // Create event
            JSObject customEventObject = (JSObject) webView.getEngine().executeScript("new CustomEvent(\"" + eventName + "\")");

            if (eventData != null)
            {
                customEventObject.setMember("data", eventData);
            }

            // Fetch DOM element
            JSObject domElementObject = (JSObject) webView.getEngine().executeScript(domElement);

            if (domElementObject == null)
            {
                throw new RuntimeException("DOM element '" + domElement + "' not found for raising JS event '" + eventName + "'");
            }

            // Raise event
            domElementObject.call("dispatchEvent", customEventObject);

            LOG.info("triggered event - dom element: {}, event name: {}", domElement, eventName);

        });
    }

    /**
     * Loads specified URL.
     */
    public void loadPage(String url)
    {
        LOG.info("loading page - url: {}", url);
        webView.getEngine().load(url);
    }

    /**
     * @return current web view control
     */
    public WebView getWebView()
    {
        return webView;
    }

    /**
     * @return retrieves facade for access to common services.
     */
    public WebStageInitService getServiceFacade()
    {
        return webStageInitService;
    }

}
