package com.limpygnome.parrot.component.settings;

import com.limpygnome.parrot.component.file.FileComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

/**
 * Manages global application settings, saved under the user's config directory.
 */
@Service
public class SettingsService
{
    private static final Logger LOG = LogManager.getLogger(SettingsService.class);

    @Autowired
    private FileComponent fileComponent;

    private Settings settings;

    @PostConstruct
    public void initialLoad()
    {
        loadOrCreate();
    }

    public synchronized String loadOrCreate()
    {
        String result = null;
        File settingsFile = getSettingsPath();

        LOG.info("loading settings - file: {}", settingsFile.getAbsolutePath());

        try
        {
            if (settingsFile.exists())
            {
                if (settingsFile.canRead())
                {
                    // Deserialize from existing file
                    LOG.info("loading existing settings");
                    ObjectMapper mapper = new ObjectMapper();
                    settings = mapper.readValue(settingsFile, Settings.class);
                    LOG.debug("settings: {}", settings);
                }
                else
                {
                    LOG.error("unable to read settings file");
                    result = "Unable to read settings - path: " + settingsFile.getAbsolutePath();
                }
            }
            else
            {
                LOG.info("creating settings");

                // Create settings from default values
                settings = new Settings();
                result = save();
            }
        }
        catch (IOException e)
        {
            LOG.error("failed to load or create settings", e);
            result = "Failed to load or create settings";
        }

        return result;
    }

    public synchronized String save()
    {
        String result = null;
        File settingsFile = getSettingsPath();

        LOG.info("saving settings - file: {}", settingsFile.getAbsolutePath());
        LOG.debug("settings: {}", settings);

        try
        {
            // Check we can write to file
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(settingsFile, settings);
        }
        catch (IOException e)
        {
            LOG.error("failed to save settings", e);
            result = "Failed to save settings - path: " + settingsFile.getAbsolutePath();
        }

        return result;
    }

    /**
     * resets to default settings and persists them.
     *
     * @return error message; null if successful
     */
    public String reset()
    {
        settings = new Settings();

        String result = save();
        return result;
    }

    private File getSettingsPath()
    {
        return fileComponent.resolvePreferenceFile("settings.json");
    }

    /**
     * @return global application settings
     */
    public synchronized Settings getSettings()
    {
        return settings;
    }

}
