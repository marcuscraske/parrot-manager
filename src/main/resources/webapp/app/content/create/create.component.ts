import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';

@Component({
    moduleId: module.id,
    templateUrl: 'create.component.html'
})
export class CreateComponent {

    public createForm = this.fb.group({
        location: ["", Validators.required],
        password: ["", Validators.required],
        confirmPassword: ["", Validators.required],
        rounds: ["", Validators.required]
    });

    constructor(public fb: FormBuilder) {}

    create(event) {
        console.log(event);
        console.log(this.createForm.value);
        console.log(this.createForm["location"]);
        console.log(this.createForm["password"]);
    }

}
