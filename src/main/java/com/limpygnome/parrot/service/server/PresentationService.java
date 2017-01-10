package com.limpygnome.parrot.service.server;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.service.rest.DatabaseService;
import com.limpygnome.parrot.service.rest.RuntimeService;
import com.sun.javafx.webkit.WebConsoleListener;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.w3c.dom.html.HTMLElement;

/**
 * A service for controlling a bridge between service and presentation layers.
 *
 * Any communication hooking should be done through this service.
 */
public class PresentationService
{
    private final Controller controller;

    private RuntimeService runtimeService;
    private DatabaseService databaseService;

    private WebView webView = null;
    private Scene scene = null;

    public PresentationService(Controller controller)
    {
        this.controller = controller;

        // Setup JS REST services
        this.runtimeService = new RuntimeService(controller);
        this.databaseService = new DatabaseService(controller);
    }

    /**
     * Creates a new scene with a web view, loaded with the client-side interface.
     *
     * Can only be invoked once, otherwise same scene is returned.
     */
    public synchronized Scene getScene()
    {
        // Load web view with client-side application
        if (webView == null)
        {
            // Create new web view and set it up
            webView = new WebView();

            setupDebugging();
            setupContextMenu();
            setupClientsideHooks();

            webView.getEngine().load("http://localhost:8123/index.html");
        }

        // Create scene if not already created
        if (scene == null)
        {
            scene = new Scene(webView);
        }

        return scene;
    }

    private void setupDebugging()
    {
        // TODO: move to logger
        WebEngine engine = webView.getEngine();

        WebConsoleListener.setDefaultListener((webView1, message, lineNumber, sourceId) -> {
            System.out.println("[WEB OUT] " + sourceId + " : " + message + " : line num: " + lineNumber);
        });
        engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("WebView state change : " + oldValue + " -> " + newValue);
        });
        engine.setOnError(event -> {
            System.out.println("ERROR : " + event.getMessage());
            event.getException().printStackTrace(System.out);
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
                String itemType = element.getAttribute("data-type");

                if (itemType != null)
                {
                    switch (itemType)
                    {
                        case "folder":
                            ctxMenuToShow = contextMenuFolder;
                            break;
                        case "entry":
                            ctxMenuToShow = contextMenuEntry;
                            break;
                    }
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
            // Ensure this runs on the JavaFX thread...
            Platform.runLater(() -> {

                // Expose rest service objects
                exposeJsObject("runtimeService", runtimeService);
                exposeJsObject("databaseService", databaseService);

                // TODO: use logger
                System.out.println("### hooked global vars ###");

            });
        });
    }

    /*
     * Exposes an object in the currently set web-view.
     */
    private void exposeJsObject(String variableName, Object object)
    {
        JSObject obj = (JSObject) webView.getEngine().executeScript("window");
        obj.setMember(variableName, object);
    }

}
