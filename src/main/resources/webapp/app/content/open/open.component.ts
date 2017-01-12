import { Component } from '@angular/core';
import { DatabaseService } from '../../service/database.service'
import { RuntimeService } from '../../service/runtime.service'
import { Router } from '@angular/router';

import "app/global-vars"

@Component({
    moduleId: module.id,
    templateUrl: 'open.component.html',
    providers: [DatabaseService, RuntimeService]
})
export class OpenComponent {

    errorMessage: string;

    constructor(private databaseService: DatabaseService, private runtimeService: RuntimeService, private router: Router) { }

    chooseDatabaseFile() : void
    {
        // Open dialogue and read file
        var path = this.runtimeService.pickFile("Open existing database", null, false);

        if (path != null)
        {
            // Prompt for database password...
            console.log("file opened in dialogue, opening password prompt... - path: " + path);

            bootbox.prompt({
                title: "Enter password:",
                inputType: "password",
                callback: (result) => {
                    console.log("password entered, opening database file...");
                    this.openDatabaseFile(path, result);
                }
            });
        }
        else
        {
            console.log("no path received from dialogue, must have cancelled");
        }
    }

    openDatabaseFile(path, password) : void
    {
        if (path != null && password != null)
        {
            // Open database
            console.log("opening database... - path: " + path);
            var message = this.databaseService.open(path, password);

            // Check if failure message
            if (message == null)
            {
                console.log("successfully opened database, redirecting to navigator...");
                this.router.navigate(["/viewer"]);
            }
            else
            {
                this.errorMessage = message;
                console.log("failed to open database - " + message);
            }
        }
        else
        {
            console.log("path or password null, ignoring request to open database file");
        }
    }

}
