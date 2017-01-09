package com.limpygnome.parrot.service.server;

import com.limpygnome.parrot.Controller;
import com.limpygnome.parrot.service.rest.DatabaseService;
import com.sun.javafx.webkit.WebConsoleListener;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import javafx.concurrent.Worker;
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
    private WebView webView = null;
    private Scene scene = null;

    public PresentationService(Controller controller)
    {
        this.controller = controller;
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

    private void setupClientsideHooks()
    {
        // Setup hooks after each navigation
        webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) ->
        {
            if (newValue == Worker.State.SUCCEEDED)
            {
                // Create+expose REST services
                exposeJsObject("runtimeService", new com.limpygnome.parrot.service.rest.RuntimeService(controller));
                exposeJsObject("databaseService", new DatabaseService(controller));

                // TODO: use logger
                System.out.println("### hooked global vars ###");
            }
        });

        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory()
        {
            @Override
            public URLStreamHandler createURLStreamHandler(String protocol)
            {
                if (!protocol.equals("http"))
                {
                    System.err.println("IGNORING PROTO: " + protocol);
                    return null;
                }
                return new URLStreamHandler()
                {
                    @Override
                    protected URLConnection openConnection(URL u) throws IOException
                    {
                        System.err.println("REQUESTING: " + u.toString());
                        String url = u.toString();
                        url = "webapp" + url.replace("http://localhost:8123", "");

                        System.err.println("translating to: " + url);

                        URL clazzpath = getClass().getClassLoader().getResource(url);
                        return clazzpath.openConnection();
                    }
                };
            }
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
