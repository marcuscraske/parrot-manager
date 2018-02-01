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
    private SettingsValue<Long> automaticBackupDelay;
    private SettingsValue<Long> remoteSyncInterval;
    private SettingsValue<Boolean> remoteSyncIntervalEnabled;
    private SettingsValue<Boolean> remoteSyncOnOpeningDatabase;
    private SettingsValue<Boolean> remoteSyncOnChange;
    private SettingsValue<String> theme;
    private SettingsValue<Boolean> saveWindowState;
    private SettingsValue<Long> inactivityTimeout;
    private SettingsValue<Long> wipeClipboardDelay;
    private SettingsValue<Boolean> autoSave;
    private SettingsValue<Boolean> mergeLogShowDetail;
    private SettingsValue<String> keyboardLayout;
    private SettingsValue<Long> remoteBackupsRetained;

    public Settings()
    {
        this.recentFilesEnabled = new SettingsValue<>(true);
        this.recentFilesOpenLastOnStartup = new SettingsValue<>(true);
        this.automaticBackupsOnSave = new SettingsValue<>(true);
        this.automaticBackupsRetained = new SettingsValue(30L);
        this.automaticBackupDelay = new SettingsValue<>(60L);
        this.remoteSyncInterval = new SettingsValue<>(10L * 60L * 1000L);
        this.remoteSyncIntervalEnabled = new SettingsValue<>(true);
        this.remoteSyncOnOpeningDatabase = new SettingsValue<>(true);
        this.remoteSyncOnChange = new SettingsValue<>(true);
        this.theme = new SettingsValue<>("light");
        this.saveWindowState = new SettingsValue<>(true);
        this.inactivityTimeout = new SettingsValue<>(0L);
        this.wipeClipboardDelay = new SettingsValue<>(10L);
        this.autoSave = new SettingsValue<>(true);
        this.mergeLogShowDetail = new SettingsValue<>(false);
        this.keyboardLayout = new SettingsValue<>(null);
        this.remoteBackupsRetained = new SettingsValue<>(30L);
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

    public SettingsValue<Long> getAutomaticBackupDelay()
    {
        return automaticBackupDelay;
    }

    public SettingsValue<Long> getRemoteSyncInterval()
    {
        return remoteSyncInterval;
    }

    public SettingsValue<Boolean> getRemoteSyncIntervalEnabled()
    {
        return remoteSyncIntervalEnabled;
    }

    public SettingsValue<Boolean> getRemoteSyncOnOpeningDatabase()
    {
        return remoteSyncOnOpeningDatabase;
    }

    public SettingsValue<Boolean> getRemoteSyncOnChange()
    {
        return remoteSyncOnChange;
    }

    public SettingsValue<String> getTheme() {
        return theme;
    }

    public SettingsValue<Boolean> getSaveWindowState()
    {
        return saveWindowState;
    }

    public SettingsValue<Long> getInactivityTimeout()
    {
        return inactivityTimeout;
    }

    public SettingsValue<Long> getWipeClipboardDelay()
    {
        return wipeClipboardDelay;
    }

    public SettingsValue<Boolean> getAutoSave()
    {
        return autoSave;
    }

    public SettingsValue<Boolean> getMergeLogShowDetail()
    {
        return mergeLogShowDetail;
    }

    public SettingsValue<String> getKeyboardLayout()
    {
        return keyboardLayout;
    }

    public SettingsValue<Long> getRemoteBackupsRetained()
    {
        return remoteBackupsRetained;
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
                ", automaticBackupDelay=" + automaticBackupDelay +
                ", remoteSyncInterval=" + remoteSyncInterval +
                ", remoteSyncIntervalEnabled=" + remoteSyncIntervalEnabled +
                ", remoteSyncOnOpeningDatabase=" + remoteSyncOnOpeningDatabase +
                ", remoteSyncOnChange=" + remoteSyncOnChange +
                ", theme=" + theme +
                ", saveWindowState=" + saveWindowState +
                ", inactivityTimeout=" + inactivityTimeout +
                ", wipeClipboardDelay=" + wipeClipboardDelay +
                ", autoSave=" + autoSave +
                ", mergeLogShowDetail=" + mergeLogShowDetail +
                ", keyboardLayout=" + keyboardLayout +
                ", remoteBackupsRetained=" + remoteBackupsRetained +
                '}';
    }

}
