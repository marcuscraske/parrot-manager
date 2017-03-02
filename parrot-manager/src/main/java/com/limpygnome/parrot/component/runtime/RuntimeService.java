package com.limpygnome.parrot.component.runtime;

import com.limpygnome.parrot.component.ui.WebStageInitService;
import com.limpygnome.parrot.component.ui.WebViewStage;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;

/**
 * A service for runtime state and functionality.
 */
@Service
public class RuntimeService
{
    private static final Logger LOG = LogManager.getLogger(RuntimeService.class);

    @Autowired
    private WebStageInitService webStageInitService;

    /**
     * @return indicates if running in development mode
     */
    public boolean isDevelopmentMode()
    {
        return webStageInitService.isDevelopmentMode();
    }

    /**
     * Loads specified URL.
     */
    public void loadPage(String currentUrl)
    {
        webStageInitService.getStage().loadPage(currentUrl);
    }

    /**
     * Changes the height of the window.
     *
     * @param newHeight the new desired height
     */
    public void changeHeight(int newHeight)
    {
        // Fetch actual window
        Scene scene = webStageInitService.getStage().getScene();

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
     * Opens a native dialogue to open or save a file.
     *
     * @param title the title used for the dialogue
     * @param initialPath the initial directory displayed; can be null
     * @param isSave indicates if to show a save dialogue (true), otherwise an open dialogue (false)
     * @return the path to the file to open
     */
    public String pickFile(String title, String initialPath, boolean isSave)
    {
        WebViewStage stage = webStageInitService.getStage();

        // Build dialogue
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);

        if (initialPath != null && initialPath.length() > 0)
        {
            fileChooser.setInitialDirectory(new File(initialPath));
        }

        // Display and capture file
        File result;
        if (isSave)
        {
            result = fileChooser.showSaveDialog(stage);
        }
        else
        {
            result = fileChooser.showOpenDialog(stage);
        }

        String resultPath = null;

        // Check we got a result (can be null if cancelled)
        if (result != null)
        {
            try
            {
                resultPath = result.getCanonicalPath();
            }
            catch (IOException e)
            {
                LOG.error("failed to convert file to path - file: {}", result, e);
            }
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

    /**
     * @param value the text value to be set as the clipboard contents
     */
    public void setClipboard(String value)
    {
        if (value != null)
        {
            // TODO: make single use? might not be easily possible...
            StringSelection selection = new StringSelection(value);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

            LOG.info("copied value to clipboard - length: {}", value.length());
        }
        else
        {
            LOG.warn("attempted to set null value as clipboard");
        }
    }

}
