import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { SettingsService } from 'app/service/settings.service'
import { Router } from '@angular/router';

@Component({
    moduleId: module.id,
    templateUrl: "settings.component.html",
    providers: [SettingsService],
    styleUrls: ["settings.component.css"]
})
export class SettingsComponent {

    public settingsForm = this.fb.group({
        recentFilesEnabled: [false],
        recentFilesOpenLastOnStartup: [false],
        automaticBackupsOnSave: [false],
        automaticBackupsRetained: [""]
    });

    errorMessage ; string = null;
    successMessage ; string = null;

    constructor(
        private settingsService: SettingsService,
        public fb: FormBuilder,
        private router: Router
    ) { }

    ngOnInit()
    {
        // Read existing settings
        var settings = this.settingsService.fetch();

        // Apply to form
        this.settingsForm.patchValue(settings);
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

}
