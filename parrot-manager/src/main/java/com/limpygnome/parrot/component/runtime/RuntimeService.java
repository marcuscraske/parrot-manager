package com.limpygnome.parrot.component.runtime;

import com.limpygnome.parrot.component.settings.StandAloneComponent;
import com.limpygnome.parrot.component.ui.WebStageInitService;
import com.limpygnome.parrot.component.ui.WebViewStage;
import com.limpygnome.parrot.lib.urlStream.UrlStreamOverrideService;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * A archive for runtime state and functionality.
 */
@Service
public class RuntimeService
{
    private static final Logger LOG = LoggerFactory.getLogger(RuntimeService.class);

    @Autowired
    private WebStageInitService webStageInitService;
    @Autowired
    private UrlStreamOverrideService urlStreamOverrideService;
    @Autowired
    private StandAloneComponent standAloneComponent;

    /* Flag to indicate whether webapp is ready to run. */
    private boolean ready;

    /**
     * @return indicates if running in development mode
     */
    public boolean isDevelopmentMode()
    {
        return webStageInitService.getWebViewDebug() != null;
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
        else if (standAloneComponent.isStandalone())
        {
            // Set to working directory by default on stand-alone
            fileChooser.setInitialDirectory(standAloneComponent.getRoot());
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
        LOG.info("terminating jvm");
        System.exit(0);
    }

    /**
     * @return indicates whether runtime is ready
     */
    public boolean isReady()
    {
        return ready;
    }

    /**
     * @param ready sets whether runtime is ready
     */
    public void setReady(boolean ready)
    {
        this.ready = ready;
    }

    /**
     * @return indicates whether in stand-alone mode
     */
    public boolean isStandalone()
    {
        return standAloneComponent.isStandalone();
    }

}
