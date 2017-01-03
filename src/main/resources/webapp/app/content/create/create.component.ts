import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { DatabaseService } from '../../service/database.service'

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

    constructor(public fb: FormBuilder, private databaseService: DatabaseService) {}

    create(event) {

        var form = this.createForm.value;

        if (form.valid) {

            var location = form["location"];
            var password = form["password"];
            var rounds = form["rounds"];

            console.log("creating db... - location: " + location + ", password: " + password + ", rounds: " + rounds);

            this.databaseService.create(location, password, rounds);
        }

    }

}
