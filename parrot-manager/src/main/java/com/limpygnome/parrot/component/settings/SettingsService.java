package com.limpygnome.parrot.component.settings;

import com.limpygnome.parrot.component.backup.BackupService;
import com.limpygnome.parrot.component.file.FileComponent;
import com.limpygnome.parrot.component.remote.RemoteSyncIntervalService;
import com.limpygnome.parrot.component.settings.event.SettingsRefreshedEvent;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Manages global application settings, saved under the user's config directory.
 */
@Service
public class SettingsService
{
    private static final Logger LOG = LoggerFactory.getLogger(SettingsService.class);

    @Lazy
    @Autowired
    private RemoteSyncIntervalService remoteSyncIntervalService;
    @Lazy
    @Autowired
    private BackupService backupService;

    @Autowired
    private FileComponent fileComponent;

    // Dependent components with refresh event
    @Lazy
    @Autowired
    private List<SettingsRefreshedEvent> refreshEventListeners;

    // Current settings
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

            raiseChangeEvent();
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

        // create backup
        result = backupService.create();

        if (result == null)
        {
            LOG.info("saving settings - file: {}", settingsFile.getAbsolutePath());
            LOG.debug("settings: {}", settings);

            try
            {
                // Write settings to file
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(settingsFile, settings);

                // Invoke listeners
                raiseChangeEvent();
            }
            catch (IOException e)
            {
                LOG.error("failed to save settings", e);
                result = "Failed to save settings - path: " + settingsFile.getAbsolutePath();
            }
        }
        else
        {
            LOG.warn("skipped saving settings due to backup failure - message: {}", result);
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

    private void raiseChangeEvent()
    {
        LOG.debug("raising settings change event...");

        for (SettingsRefreshedEvent eventListener : refreshEventListeners)
        {
            eventListener.eventSettingsRefreshed(settings);
        }

        LOG.debug("finished settings changed event");
    }

}
