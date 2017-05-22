import { Injectable } from '@angular/core';

@Injectable()
export class SettingsService {

    settingsService : any;

    constructor()
    {
        this.settingsService = (window as any).settingsService;
    }

    fetch()
    {
        var settings = this.settingsService.getSettings();

        var remoteSyncInterval = settings.getRemoteSyncInterval().getValue();

        var json = {
            "recentFilesEnabled" : settings.getRecentFilesEnabled().getValue(),
            "recentFilesOpenLastOnStartup" : settings.getRecentFilesOpenLastOnStartup().getValue(),
            "automaticBackupsOnSave" : settings.getAutomaticBackupsOnSave().getValue(),
            "automaticBackupsRetained" : settings.getAutomaticBackupsRetained().getValue(),
            "remoteSyncInterval" : remoteSyncInterval != null ? remoteSyncInterval / 60 / 1000 : null,
            "remoteSyncIntervalEnabled" : settings.getRemoteSyncIntervalEnabled().getValue(),
            "remoteSyncOnOpeningDatabase" : settings.getRemoteSyncOnOpeningDatabase().getValue(),
            "remoteSyncOnChange" : settings.getRemoteSyncOnChange().getValue(),
        };

        return json;
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
