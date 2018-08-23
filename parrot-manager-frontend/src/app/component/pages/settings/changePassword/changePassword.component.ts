import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';

import { DatabaseService } from 'app/service/database.service'

@Component({
    templateUrl: "changePassword.component.html",
    selector: "changePassword"
})
export class ChangePasswordComponent
{
    public changePasswordForm = this.fb.group({
        newPassword: [""],
        newPasswordConfirm: [""]
    });

    constructor(
        private databaseService: DatabaseService,
        public fb: FormBuilder
    ) { }

    changePassword()
    {
        var form = this.changePasswordForm;

        if (form.valid)
        {
            var newPassword = form.value["newPassword"];
            var newPasswordConfirm = form.value["newPasswordConfirm"];

            if (newPassword.length > 0)
            {
                // check passwords match (if specified)
                if (newPassword != newPasswordConfirm)
                {
                    toastr.error("New database passwords do not match");
                }
                else
                {
                    console.log("changing database password");

                    // update password
                    this.databaseService.changePassword(newPassword);
                    toastr.success("Updated database password");

                    // reset form
                    form.reset();
                }
            }
        }
    }

}
