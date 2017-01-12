import { Component } from '@angular/core';
import { RuntimeService } from '../service/runtime.service'
import { DatabaseService } from '../service/database.service'
import { Router } from '@angular/router';

import "app/global-vars"

@Component({
  moduleId: module.id,
  selector: 'topbar',
  templateUrl: 'topbar.component.html',
  styleUrls: ['topbar.component.css'],
  providers: [RuntimeService, DatabaseService]
})
export class TopBarComponent {

    constructor(
        private runtimeService: RuntimeService, private databaseService: DatabaseService, private router: Router
    ) { }

    // Currently unused...
    exit()
    {
        this.runtimeService.exit();
    }

    attemptToCloseDatabase()
    {
        var isDirty = this.databaseService.isDirty();

        if (isDirty)
        {
            this.triggerCloseDialogue();
        }
        else
        {
            this.closeDatabase();
        }
    }

    triggerCloseDialogue()
    {
        bootbox.dialog({
            message: "Your database has unsaved changes...",
            buttons: {
                cancel: {
                    label: "Cancel",
                    className: "btn-default",
                    callback: () => { this.handleCloseDatabaseButton("cancel"); }
                },
                exit: {
                    label: "Close",
                    className: "btn-default",
                    callback: () => { this.handleCloseDatabaseButton("exit"); }
                },
                saveAndExit: {
                    label: "Save and Close",
                    className: "btn-primary",
                    callback: () => { this.handleCloseDatabaseButton("save-exit"); }
                }
            }
        });
    }

    handleCloseDatabaseButton(result)
    {
        switch (result)
        {
            case "cancel":
                console.log("user has chosen abort option");
                break;

            case "close":
                console.log("user has chosen close with unsaved changes");
                break;

            case "save-close":
                console.log("user has chosen close and exit");
                break;

            default:
                console.error("unknown option from close dialogue - " + result);
                break;
        }
    }

    closeDatabase()
    {
        console.log("closing database...");
        this.databaseService.close();

        console.log("redirecting to open page...");
        this.router.navigate(["/open"]);
    }

}
