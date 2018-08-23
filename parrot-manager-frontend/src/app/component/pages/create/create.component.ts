import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { DatabaseService } from 'app/service/database.service'
import { RuntimeService } from 'app/service/runtime.service'

@Component({
    templateUrl: 'create.component.html'
})
export class CreateComponent {

    public createForm = this.fb.group({
        location: ["", Validators.required],
        password: ["", Validators.required],
        confirmPassword: ["", Validators.required],
        rounds: ["65536", Validators.required]
    });

    constructor(
        public fb: FormBuilder,
        private databaseService: DatabaseService,
        private runtimeService: RuntimeService,
        private router: Router
    ) {}

    create(event) {

        var form = this.createForm;

        if (form.valid) {

            var location = form.value["location"];
            var password = form.value["password"];
            var rounds = form.value["rounds"];

            console.log("creating database... - location: " + location + ", password: " + password + ", rounds: " + rounds);

            var result = this.databaseService.create(location, password, rounds);

            if (result)
            {
                console.log("navigating to home...");
                this.router.navigate(["/viewer"]);
            }
            else
            {
                console.log("navigating to error...");
                this.router.navigate(["/error"]);
            }

        } else {
            console.log("invalid form");
        }

    }

    openSaveFileDialogue() {
        // Display native dialogue to pick file to save
        var path = this.runtimeService.pickFile("Select file for database", null, true);

        // Update field
        if (path != null)
        {
            console.log("path chosen in dialogue: " + path);

            var form = this.createForm;
            form.value["location"] = path;
        }
        else
        {
            console.log("no path received, user must have cancelled");
        }
    }

}
