package com.limpygnome.parrot.component.ui;

import com.sun.javafx.webkit.WebConsoleListener;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;

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

    // JavaFX controls
    private Scene scene;
    private WebView webView;

    public WebViewStage(WebStageInitService webStageInitService)
    {
        this.webStageInitService = webStageInitService;

        // Setup webview
        webView = new WebView();

        setupDebugging();
        setupContextMenu();
        setupClientsideHooks();

        // Load initial page
        webView.getEngine().load("http://localhost/index.html");

        // Build scene for web view
        scene = new Scene(webView);

        // General window config
        setScene(scene);
        setTitle("parrot");
        setWidth(1024.0);
        setHeight(200.0);

        // Setup icons
        addIcon("/icons/parrot-icon.png");
        addIcon("/icons/parrot-icon-64.png");
        addIcon("/icons/parrot-icon-512.png");
        addIcon("/icons/parrot.svg");

        // Prevent window from closing to prevent data loss for dirty databases
        setOnCloseRequest(event -> {
            // Prevent exit by consuming event...
            event.consume();

            // Trigger event for JS to handle action
            triggerEvent("document", "nativeExit", null);
        });
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

    private void setupDebugging()
    {
        WebEngine engine = webView.getEngine();

        WebConsoleListener.setDefaultListener((webView1, message, lineNumber, sourceId) -> {
            LOG.info("web console message - src: {}, line num: {}, message: {}", sourceId, lineNumber, message);
        });
        engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            LOG.info("WebView state has changed - old: {}, new: {}", oldValue, newValue);
        });
        engine.setOnError(event -> {
            LOG.error("wehview error - message: {}", event.getMessage(), event.getException());
        });
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
