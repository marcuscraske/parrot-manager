package com.limpygnome.parrot;

import com.sun.javafx.webkit.WebConsoleListener;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.w3c.dom.html.HTMLElement;

/**
 * Created by limpygnome on 13/11/16.
 */
public class Program extends Application
{
    private Controller controller;

    public Program()
    {
        controller = new Controller();
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // Start local Jetty server
        controller.getJettyService().start();

        // Locate main page
        String url = "http://localhost:8123/index.html";

        // Build browser
        final WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        engine.load(url);

        // Debug only
        System.out.println("Output enabled - loading: " + url);
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

        // Hook custom context menu
        setupContextMenu(webView);

        // Build view
        primaryStage.setScene(new Scene(webView));
        primaryStage.setTitle("parrot - version 1.x.x");

        // Hook to shutdown jetty on close
        primaryStage.setOnCloseRequest(event -> {
            try
            {
                controller.getJettyService().stop();
            }
            catch (Exception e) { }
        });

        // Show view
        primaryStage.show();
    }

    private void setupContextMenu(final WebView webView)
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
                JSObject object = (JSObject) webView.getEngine().executeScript("document.elementFromPoint(" + e.getX() + "," +  e.getY() + ");");
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

    public static void main(String[] args)
    {
        launch(args);
    }

}
