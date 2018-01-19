import { Injectable } from '@angular/core';

@Injectable()
export class SettingsService {

    settingsService : any;

    constructor()
    {
        this.settingsService = (window as any).settingsService;
    }

    fetchAll()
    {
        var settings = this.settingsService.getSettings();

        var remoteSyncInterval = settings.getRemoteSyncInterval().getValue();
        var inactivityTimeout = settings.getInactivityTimeout().getValue();

        var json = {
            "recentFilesEnabled" : settings.getRecentFilesEnabled().getValue(),
            "recentFilesOpenLastOnStartup" : settings.getRecentFilesOpenLastOnStartup().getValue(),
            "automaticBackupsOnSave" : settings.getAutomaticBackupsOnSave().getValue(),
            "automaticBackupsRetained" : settings.getAutomaticBackupsRetained().getValue(),
            "automaticBackupDelay" : settings.getAutomaticBackupDelay().getValue(),
            "remoteSyncInterval" : (remoteSyncInterval != null ? remoteSyncInterval / 60 / 1000 : null),
            "remoteSyncIntervalEnabled" : settings.getRemoteSyncIntervalEnabled().getValue(),
            "remoteSyncOnOpeningDatabase" : settings.getRemoteSyncOnOpeningDatabase().getValue(),
            "remoteSyncOnChange" : settings.getRemoteSyncOnChange().getValue(),
            "theme" : settings.getTheme().getValue(),
            "inactivityTimeout" : (inactivityTimeout != null ? inactivityTimeout / 60 / 1000 : null),
            "autoSave" : settings.getAutoSave().getValue(),
            "keyboardLayout" : settings.getKeyboardLayout().getValue()
        };

        return json;
    }

    fetch(name: string)
    {
        var json = this.fetchAll();
        return json[name];
    }

    save(json)
    {
        var settings = this.settingsService.getSettings();

        // Update settings
        settings.getRecentFilesEnabled().setValue(
            json.recentFilesEnabled
        );
        settings.getRecentFilesOpenLastOnStartup().setValue(
            json.recentFilesOpenLastOnStartup
        );
        settings.getAutomaticBackupsOnSave().setValue(
            json.automaticBackupsOnSave
        );
        settings.getAutomaticBackupsRetained().setValueLong(
            json.automaticBackupsRetained
        );
        settings.getAutomaticBackupDelay().setValueLong(
            json.automaticBackupDelay
        );
        settings.getRemoteSyncInterval().setValueLong(
            (json.remoteSyncInterval != null ? json.remoteSyncInterval * 60 * 1000 : null)
        );
        settings.getRemoteSyncIntervalEnabled().setValue(
            json.remoteSyncIntervalEnabled
        );
        settings.getRemoteSyncOnOpeningDatabase().setValue(
            json.remoteSyncOnOpeningDatabase
        );
        settings.getRemoteSyncOnChange().setValue(
            json.remoteSyncOnChange
        );
        settings.getTheme().setValue(
            json.theme
        );
        settings.getInactivityTimeout().setValueLong(
            (json.inactivityTimeout != null ? json.inactivityTimeout * 60 * 1000 : null)
        );
        settings.getAutoSave().setValue(
            json.autoSave
        );
        settings.getKeyboardLayout().setValue(
            json.keyboardLayout
        );

        // Save
        var result = this.settingsService.save();
        return result;
    }

    reset()
    {
        var result = this.settingsService.reset();
        return result;
    }

}
