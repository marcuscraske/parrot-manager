import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { DatabaseService } from '../../service/database.service'
import { Router } from '@angular/router';

@Component({
    moduleId: module.id,
    templateUrl: 'create.component.html',
    providers: [DatabaseService]
})
export class CreateComponent {

    public createForm = this.fb.group({
        location: ["", Validators.required],
        password: ["", Validators.required],
        confirmPassword: ["", Validators.required],
        rounds: ["", Validators.required]
    });

    constructor(public fb: FormBuilder, private databaseService: DatabaseService, private router: Router) {}

    create(event) {

        var form = this.createForm.value;

        if (form.valid) {

            console.log("creating database...");

            var location = form["location"];
            var password = form["password"];
            var rounds = form["rounds"];

            console.log("creating db... - location: " + location + ", password: " + password + ", rounds: " + rounds);

            var result = this.databaseService.create(location, password, rounds);

            if (result)
            {
                console.log("navigating to home...");
                this.router.navigate(["/home"]);
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

}
