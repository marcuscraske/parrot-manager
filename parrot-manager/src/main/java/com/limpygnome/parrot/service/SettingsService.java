package com.limpygnome.parrot.service;

import com.limpygnome.parrot.model.setting.Settings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * Manages global application settings, saved under the user's config directory.
 */
@Service
public class SettingsService
{
    private static final Logger LOG = LogManager.getLogger(SettingsService.class);

    private Settings settings;

    public SettingsService()
    {
        // Immediately load settings
        loadOrCreate();
    }

    public synchronized String loadOrCreate()
    {
        String result = null;
        File settingsFile = getSettingsPath();

        LOG.info("settings file: {}", settingsFile.getAbsolutePath());

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

        LOG.info("settings file: {}", settingsFile.getAbsolutePath());

        try
        {
            // Create parent path if it does not exist
            File settingsParentFile = settingsFile.getParentFile();
            if (!settingsParentFile.exists() && !settingsParentFile.mkdirs())
            {
                LOG.info("unable to create parent dir for settings");
                result = "Failed to create directory for settings - path: " + settingsParentFile.getAbsolutePath();
            }

            // Check we can write to file
            if (settingsFile.canWrite())
            {
                // Serialize to file
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(settingsFile, settings);
            }
            else
            {
                LOG.info("unable to write settings file");
                result = "Unable to write to settings file - path: " + settingsFile.getAbsolutePath();
            }
        }
        catch (IOException e)
        {
            LOG.error("failed to save settings", e);
            result = "Failed to save settings - path: " + settingsFile.getAbsolutePath();
        }

        return result;
    }

    private File getSettingsPath()
    {
        // TODO: need to consider windows
        String homeDir = System.getProperty("user.home");
        File result = new File(homeDir + "/.config/parrot-manager/settings.json");
        return result;
    }

    /**
     * @return global application settings
     */
    public synchronized Settings getSettings()
    {
        return settings;
    }

}
