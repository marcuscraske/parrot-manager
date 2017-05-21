import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';

import { SettingsService } from 'app/service/settings.service'
import { RecentFileService } from 'app/service/recentFile.service'
import { DatabaseService } from 'app/service/database.service'
import { DatabaseOptimizerService } from 'app/service/databaseOptimizer.service'
import { BackupService } from 'app/service/backup.service'

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
        remoteSyncStartup: [true],
        remoteSyncInterval: [""],
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
        private backupService: BackupService,
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
            console.log("saving settings...");

            // Check new passwords match (if changed)
            var newPassword = form.value["newPassword"];
            var newPasswordConfirm = form.value["newPasswordConfirm"];

            // Check passwords match (if specified)
            if (newPassword.length > 0 && newPassword != newPasswordConfirm)
            {
                toastr.error("New database passwords do not match");
            }

            // Make a backup
            else if (this.backupService.create())
            {
                var errorMessage = null;

                // Change password (if specified)
                if (newPassword.length > 0)
                {
                    console.log("changing database password");

                    var database = this.databaseService.getDatabase();
                    database.changePassword(newPassword);

                    toastr.success("Updated database password");
                }

                // Save settings
                if (errorMessage == null)
                {
                    var newSettings = form.value;
                    errorMessage = this.settingsService.save(newSettings);
                }

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

    databaseOptimizeDeletedNodeHistory()
    {
        if (this.backupService.create())
        {
            console.log("optimizing delete node history");
            this.databaseOptimizerService.optimizeDeletedNodeHistory();

            toastr.success("Cleared database node history");
        }
    }

    databaseOptimizeValueHistory()
    {
        if (this.backupService.create())
        {
            console.log("optimizing value history");
            this.databaseOptimizerService.optimizeValueHistory();

            toastr.success("Database value history cleared");
        }
    }

}
