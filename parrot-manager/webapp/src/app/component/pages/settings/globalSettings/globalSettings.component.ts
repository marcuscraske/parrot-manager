import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';

import { RuntimeService } from 'app/service/runtime.service'
import { SettingsService } from 'app/service/settings.service'
import { DatabaseService } from 'app/service/database.service'
import { RecentFileService } from 'app/service/recentFile.service'
import { BackupService } from 'app/service/backup.service'
import { ThemeService } from 'app/service/theme.service'
import { SendKeysService } from 'app/service/sendKeys.service'

@Component({
    templateUrl: "globalSettings.component.html",
    providers: [RecentFileService],
    selector: "globalSettings"
})
export class GlobalSettingsComponent
{
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
        inactivityTimeout: [""],
        autoSave: [true],
        keyboardLayout: [""]
    });

    recentFilesClearEnabled : boolean;

    constructor(
        public runtimeService: RuntimeService,
        public settingsService: SettingsService,
        public databaseService: DatabaseService,
        public recentFileService: RecentFileService,
        public backupService: BackupService,
        public themeService: ThemeService,
        public sendKeysService: SendKeysService,
        public fb: FormBuilder,
        public router: Router
    ) { }

    ngOnInit()
    {
        this.populateSettings();
    }

    ngOnDestroy()
    {
        // reset theme (in case unsaved)
        var theme = this.settingsService.fetch("theme");
        this.themeService.set(theme);
    }

    populateSettings()
    {
        console.log("populating form with settings...");

        // Read existing settings
        var settings = this.settingsService.fetchAll();

        // Apply to form
        this.globalSettingsForm.patchValue(settings);

        // Determine if any recent files
        this.recentFilesClearEnabled = this.recentFileService.isAny();
    }

    save()
    {
        var form = this.globalSettingsForm;

        if (form.valid)
        {
            console.log("saving settings...");

            // Make a backup
            if (this.backupService.create())
            {
                var errorMessage = null;

                // Save settings
                var newSettings = form.value;
                errorMessage = this.settingsService.save(newSettings);

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
                console.log("failed to create backup, aborted save settings");
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

        this.populateSettings();
    }

    globalRecentFilesClear()
    {
        console.log("clearing recent files");

        this.recentFileService.clear();
        this.recentFilesClearEnabled = false;

        toastr.success("Cleared recent files");
    }

    trackChildrenKeyboardLayouts(index, layout)
    {
        return layout ? layout.getName() : null;;
    }

}
