import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';

import { SettingsService } from 'app/service/settings.service'
import { RecentFileService } from 'app/service/recentFile.service'
import { DatabaseService } from 'app/service/database.service'
import { DatabaseOptimizerService } from 'app/service/databaseOptimizer.service'

@Component({
    moduleId: module.id,
    templateUrl: "settings.component.html",
    providers: [SettingsService, RecentFileService, DatabaseOptimizerService],
    styleUrls: ["settings.component.css"]
})
export class SettingsComponent {

    public settingsForm = this.fb.group({
        recentFilesEnabled: [false],
        recentFilesOpenLastOnStartup: [false],
        automaticBackupsOnSave: [false],
        automaticBackupsRetained: [""],
        newPassword: [""],
        newPasswordConfirm: [""]
    });

    recentFilesClearEnabled : boolean;

    constructor(
        private settingsService: SettingsService,
        private recentFileService: RecentFileService,
        private databaseService: DatabaseService,
        private databaseOptimizerService: DatabaseOptimizerService,
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
            var isError = false;

            // Check new passwords match (if changed)
            var newPassword = form.value["newPassword"];
            var newPasswordConfirm = form.value["newPasswordConfirm"];

            if (newPassword.length > 0)
            {
                if (newPassword != newPasswordConfirm)
                {
                    toastr.error("New database passwords do not match");
                    isError = true;
                }
                else
                {
                    // change it...
                    console.log("changing database password");

                    var database = this.databaseService.getDatabase();
                    database.changePassword(newPassword);

                    toastr.success("Updated database password");
                }
            }

            // Save settings
            if (!isError)
            {
                var newSettings = form.value;
                var errorMessage = this.settingsService.save(newSettings);

                if (errorMessage == null)
                {
                    toastr.success("Saved");
                }
                else
                {
                    toastr.error(errorMessage);
                }
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
        var errorMessage = this.settingsService.reset();

        if (errorMessage != null)
        {
            toastr.error(errorMessage);
        }

        this.populateSettings();
    }

    recentFilesClear()
    {
        console.log("clearing recent files");

        this.recentFileService.clear();
        this.recentFilesClearEnabled = false;

        toastr.info("Cleared recent files");
    }

    optimizeDeletedNodeHistory()
    {
        console.log("optimizing delete node history");
        this.databaseOptimizerService.optimizeDeletedNodeHistory();

        toastr.info("Cleared database node history");
    }

    optimizeValueHistory()
    {
        console.log("optimizing value history");
        this.databaseOptimizerService.optimizeValueHistory();

        toastr.info("Database value history cleared");
    }

}
