package com.limpygnome.parrot.service.rest;

import com.limpygnome.parrot.ui.WebViewStage;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * REST service for runtime service.
 */
public class RuntimeService
{
    private static final Logger LOG = LogManager.getLogger(RuntimeService.class);

    private final WebViewStage stage;

    public RuntimeService(WebViewStage stage)
    {
        this.stage = stage;
    }

    /**
     * Changes the height of the window.
     *
     * @param newHeight the new desired height
     */
    public void changeHeight(int newHeight)
    {
        // Fetch actual window
        Scene scene = stage.getScene();

        // Set the new height, factoring size of window frame
        int targetHeight = newHeight + (int) scene.getY() + 1;

        if (targetHeight > 0)
        {
            LOG.debug("changing height - new: {}", targetHeight);

            Window window = scene.getWindow();
            window.setHeight(targetHeight);
        }
    }

    /**
     * Opens a native dialogue to open a database file.
     *
     * @param initialPath
     * @return the path to the file to open
     */
    public String openDatabaseFile(String initialPath)
    {
        // Build dialogue
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open existing database");

        if (initialPath != null && initialPath.length() > 0)
        {
            fileChooser.setInitialDirectory(new File(initialPath));
        }

        // Display and capture file
        File result = fileChooser.showOpenDialog(stage);

        String resultPath = null;

        try
        {
            result.getCanonicalPath();
        }
        catch (IOException e)
        {
            LOG.error("failed to convert file to path - file: {}", result, e);
        }

        return resultPath;
    }

    /**
     * Exits the application.
     */
    public void exit()
    {
        // Just exit the application...
        System.exit(0);
    }

}
