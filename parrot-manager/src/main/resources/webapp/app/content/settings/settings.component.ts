import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';

import { SettingsService } from 'app/service/settings.service'
import { RecentFileService } from 'app/service/recentFile.service'
import { DatabaseService } from 'app/service/database.service'

@Component({
    moduleId: module.id,
    templateUrl: "settings.component.html",
    providers: [SettingsService, RecentFileService],
    styleUrls: ["settings.component.css"]
})
export class SettingsComponent {

    public settingsForm = this.fb.group({
        recentFilesEnabled: [false],
        recentFilesOpenLastOnStartup: [false],
        automaticBackupsOnSave: [false],
        automaticBackupsRetained: [""]
    });

    errorMessage : string = null;
    successMessage : string = null;

    recentFilesClearEnabled : boolean;

    constructor(
        private settingsService: SettingsService,
        private recentFileService: RecentFileService,
        private databaseService: DatabaseService,
        public fb: FormBuilder,
        private router: Router
    ) { }

    ngOnInit()
    {
        this.populateSettings();
    }

    populateSettings()
    {
        console.log("populating form with settings...");

        // Read existing settings
        var settings = this.settingsService.fetch();

        // Apply to form
        this.settingsForm.patchValue(settings);

        // Determine if any recent files
        this.recentFilesClearEnabled = this.recentFileService.isAny();
    }

    save()
    {
        var form = this.settingsForm;

        if (form.valid)
        {
            // Save settings
            var newSettings = form.value;
            this.errorMessage = this.settingsService.save(newSettings);

            if (this.errorMessage == null)
            {
                this.successMessage = "Saved.";
            }
        }
        else
        {
            console.log("settings form not valid");
        }
    }

    resetToDefault()
    {
        console.log("resetting to default settings");
        this.errorMessage = this.settingsService.reset();

        this.populateSettings();
    }

    recentFilesClear()
    {
        console.log("clearing recent files");

        this.recentFileService.clear();
        this.recentFilesClearEnabled = false;
    }

}
