import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';

import { SettingsService } from 'app/service/settings.service'
import { DatabaseService } from 'app/service/database.service'
import { ThemeService } from 'app/service/theme.service'

@Component({
    templateUrl: "settings.component.html",
    styleUrls: ["settings.component.css"]
})
export class SettingsComponent
{
    public currentTab: string;
    public lastSettings;

    public globalSettingsForm = this.fb.group({
        recentFilesEnabled: [false],
        recentFilesOpenLastOnStartup: [false],
        automaticBackupsOnSave: [false],
        automaticBackupsRetained: [""],
        automaticBackupDelay: [""],
        remoteSyncInterval: [""],
        remoteSyncIntervalEnabled: [false],
        remoteSyncOnOpeningDatabase: [false],
        remoteSyncOnChange: [false],
        theme: [""],
        saveWindowState: [""],
        inactivityTimeout: [""],
        wipeClipboardDelay: [""],
        autoSave: [true],
        mergeLogShowDetail: [false],
        keyboardLayout: [""]
    });

    constructor(
        public settingsService: SettingsService,
        public databaseService: DatabaseService,
        public themeService: ThemeService,
        public fb: FormBuilder
    ) {

    }

    ngOnInit()
    {
        // Check which page to show by default
        this.currentTab = (this.databaseService.isOpen() ? "changePassword" : "recentFiles");

        // Load settings
        this.populateSettings();

        // Subscribe to changes
        this.globalSettingsForm.valueChanges.subscribe(form =>
        {
            // Check settings have changed, as binding form controls between components will trigger this event
            if (JSON.stringify(form) != JSON.stringify(this.lastSettings))
            {
                console.log("settings changed");
                console.log(JSON.stringify(form));
                console.log(JSON.stringify(this.lastSettings));

                // Save changes
                this.settingsService.save(form);
            }
        });
    }

    populateSettings()
    {
        console.log("populating form with settings...");

        // Read existing settings
        var settings = this.settingsService.fetchAll();
        this.lastSettings = settings;

        // Apply to form
        this.globalSettingsForm.patchValue(settings);
    }

    save()
    {
        var form = this.globalSettingsForm;

        if (form.valid)
        {
            console.log("saving settings...");

            // Save settings
            var newSettings = form.value;
            var errorMessage = this.settingsService.save(newSettings);

            // Display notification
            if (errorMessage == null)
            {
                toastr.success("Settings saved");
            }
            else
            {
                toastr.error(errorMessage);
            }
        }
        else
        {
            console.log("settings form not valid");
        }
    }

    globalResetToDefault()
    {
        console.log("resetting to default settings");
        var errorMessage = this.settingsService.reset();

        if (errorMessage != null)
        {
            toastr.error(errorMessage);
        }
        else
        {
            toastr.info("Settings reset to default");
        }

        // reset theme
        var theme = this.settingsService.fetch("theme");
        this.themeService.set(theme);

        this.populateSettings();
    }

}
