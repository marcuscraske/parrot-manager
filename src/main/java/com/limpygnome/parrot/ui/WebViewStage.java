package com.limpygnome.parrot.ui;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.service.rest.DatabaseService;
import com.limpygnome.parrot.service.rest.RuntimeService;
import com.sun.javafx.webkit.WebConsoleListener;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.html.HTMLElement;

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

    // JavaFX controls
    private Scene scene;
    private WebView webView;

    // Injected POJOs
    private RuntimeService runtimeService;
    private DatabaseService databaseService;

    public WebViewStage(Controller controller)
    {
        // Setup injected POJOs
        this.runtimeService = new RuntimeService(this);
        this.databaseService = new DatabaseService(controller);

        // Setup webview
        webView = new WebView();

        setupDebugging();
        setupContextMenu();
        setupClientsideHooks();

        webView.getEngine().load("http://localhost/index.html");

        // Build scene for web view
        scene = new Scene(webView);

        // General window config
        setScene(scene);
        setTitle("parrot");
        setWidth(900.0);
        setHeight(150.0);

        // Prevent window from closing to prevent data loss for dirty databases
        setOnCloseRequest(event -> {
            // Prevent exit by consuming event...
            event.consume();

            // Trigger event for JS to handle action
            triggerEvent("document", "nativeExit");
        });
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
        // Disable default ctx menu
        webView.setContextMenuEnabled(false);

        // Build menu for folder
        ContextMenu contextMenuFolder = new ContextMenu();
        {
            MenuItem ctxAddEntry = new MenuItem("Add Entry");
            ctxAddEntry.setOnAction(e -> webView.getEngine().reload());

            MenuItem ctxRemoveFolder = new MenuItem("Remove Folder");
            ctxRemoveFolder.setOnAction(e -> webView.getEngine().reload());

            contextMenuFolder.getItems().addAll(ctxAddEntry, ctxRemoveFolder);
        }

        // Build menu for entry
        ContextMenu contextMenuEntry = new ContextMenu();
        {
            MenuItem ctxRemoveEntry = new MenuItem("Remove Entry");
            ctxRemoveEntry.setOnAction(e -> webView.getEngine().reload());

            contextMenuEntry.getItems().addAll(ctxRemoveEntry);
        }

        // Hook
        webView.setOnMousePressed(e -> {
            ContextMenu ctxMenuToShow = null;

            // Determine if to show ctx menu
            if (e.getButton() == MouseButton.SECONDARY)
            {
                // Fetch element clicked
                netscape.javascript.JSObject object = (netscape.javascript.JSObject) webView.getEngine().executeScript("document.elementFromPoint(" + e.getX() + "," + e.getY() + ");");
                HTMLElement element = (HTMLElement) object;
                String itemType = element.getAttribute("id");

                if (itemType != null)
                {
                    LOG.error("ITEM TYPE: {}", itemType);
//                    switch (itemType)
//                    {
//                        case "folder":
//                            ctxMenuToShow = contextMenuFolder;
//                            break;
//                        case "entry":
//                            ctxMenuToShow = contextMenuEntry;
//                            break;
//                    }
                }
            }

            // Hide all menus
            contextMenuFolder.hide();
            contextMenuEntry.hide();

            // Show menu
            if (ctxMenuToShow != null)
            {
                ctxMenuToShow.show(webView, e.getScreenX(), e.getScreenY());
            }
        });
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
                    // Expose rest service objects
                    exposeJsObject("runtimeService", runtimeService);
                    exposeJsObject("databaseService", databaseService);

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
    public void triggerEvent(String domElement, String eventName)
    {
        String script = domElement + ".dispatchEvent(new CustomEvent(\"" + eventName + "\"))";
        webView.getEngine().executeScript(script);

        LOG.info("triggered event - dom element: {}, event name: {}", domElement, eventName);
    }

}
