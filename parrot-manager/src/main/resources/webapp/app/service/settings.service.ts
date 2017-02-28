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

        var json = {
            "recentFilesEnabled" : settings.getRecentFilesEnabled().getValue(),
            "recentFilesOpenLastOnStartup" : settings.getRecentFilesOpenLastOnStartup().getValue(),
            "automaticBackupsOnSave" : settings.getAutomaticBackupsOnSave().getValue(),
            "automaticBackupsRetained" : settings.getAutomaticBackupsRetained().getValue(),
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
        settings.getAutomaticBackupsRetained().setValue(
            json.automaticBackupsRetained
        );

        // Save
        this.settingsService.save();
    }

}
