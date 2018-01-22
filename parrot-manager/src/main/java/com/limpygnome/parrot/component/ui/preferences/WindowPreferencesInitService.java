package com.limpygnome.parrot.component.ui.preferences;

import com.limpygnome.parrot.component.file.FileComponent;
import com.limpygnome.parrot.component.settings.Settings;
import com.limpygnome.parrot.component.settings.event.SettingsRefreshedEvent;
import com.limpygnome.parrot.component.ui.WebViewStage;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Used to load and persist window preferences (size, location and state), so that the window
 * appears the same when it's reopened.
 */
@Service
public class WindowPreferencesInitService implements SettingsRefreshedEvent
{
    private static final Logger LOG = LoggerFactory.getLogger(WindowPreferencesInitService.class);

    @Autowired
    private FileComponent fileComponent;

    // Holds current preferences
    private WindowPreferences windowPreferences;

    // Indicates whether saving window state is enabled
    private boolean saveWindowState;

    public WindowPreferencesInitService()
    {
        windowPreferences = new WindowPreferences();
    }

    /**
     * @param stage stage to be attached
     */
    public void attach(WebViewStage stage)
    {
        // Load old preferences and apply them
        loadAndApply(stage);
    }

    @Override
    public void eventSettingsRefreshed(Settings settings)
    {
        saveWindowState = settings.getSaveWindowState().getSafeBoolean(true);
    }

    private synchronized void loadAndApply(WebViewStage stage)
    {
        if (saveWindowState)
        {
            try
            {
                File preferencesFile = preferencesFile();

                if (preferencesFile.exists())
                {
                    ObjectMapper mapper = new ObjectMapper();
                    windowPreferences = mapper.readValue(preferencesFile, WindowPreferences.class);
                    LOG.info("window preferences applied");
                }
                else
                {
                    LOG.info("window preferences do not exist");
                }

                windowPreferences.apply(stage);
            }
            catch (IOException e)
            {
                LOG.error("failed to load window preferences", e);
            }
        }
        else
        {
            LOG.debug("windows references not loaded (disabled)");
        }
    }

    /**
     * @param stage saves preferences using provided stage
     */
    public synchronized void save(WebViewStage stage)
    {
        // check for change; persist if changed
        if (!saveWindowState)
        {
            LOG.debug("window preferences disabled (skipped save)");
        }
        else if (windowPreferences.copyFrom(stage))
        {
            try
            {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(preferencesFile(), windowPreferences);
                LOG.info("window preferences saved");
            }
            catch (IOException e)
            {
                LOG.error("failed to save window preferences", e);
            }
        }
        else
        {
            LOG.info("window preferences unchanged (skipped save)");
        }
    }

    private File preferencesFile()
    {
        return fileComponent.resolvePreferenceFile("window.json");
    }

}
