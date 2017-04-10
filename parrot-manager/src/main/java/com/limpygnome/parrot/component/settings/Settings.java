package com.limpygnome.parrot.component.settings;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;

/**
 * Stores a collection of settings.
 */
public class Settings implements Serializable
{
    private SettingsValue<Boolean> recentFilesEnabled;
    private SettingsValue<Boolean> recentFilesOpenLastOnStartup;
    private SettingsValue<Boolean> automaticBackupsOnSave;
    private SettingsValue<Long> automaticBackupsRetained;

    public Settings()
    {
        this.recentFilesEnabled = new SettingsValue<>(true);
        this.recentFilesOpenLastOnStartup = new SettingsValue<>(true);
        this.automaticBackupsOnSave = new SettingsValue<>(true);
        this.automaticBackupsRetained = new SettingsValue(10);
    }

    public SettingsValue<Boolean> getRecentFilesEnabled()
    {
        return recentFilesEnabled;
    }

    public SettingsValue<Boolean> getRecentFilesOpenLastOnStartup()
    {
        return recentFilesOpenLastOnStartup;
    }

    public SettingsValue<Boolean> getAutomaticBackupsOnSave()
    {
        return automaticBackupsOnSave;
    }

    public SettingsValue<Long> getAutomaticBackupsRetained()
    {
        return automaticBackupsRetained;
    }

    @JsonIgnore
    @Override
    public String toString()
    {
        return "Settings{" +
                "recentFilesEnabled=" + recentFilesEnabled +
                ", recentFilesOpenLastOnStartup=" + recentFilesOpenLastOnStartup +
                ", automaticBackupsOnSave=" + automaticBackupsOnSave +
                ", automaticBackupsRetained=" + automaticBackupsRetained +
                '}';
    }

}
