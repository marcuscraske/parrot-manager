import { Component } from '@angular/core';
import { DatabaseService } from '../../service/database.service'
import { RuntimeService } from '../../service/runtime.service'
import { Router } from '@angular/router';

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

        // Open with password prompt
        this.databaseService.openWithPrompt(path, (message) => {

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

        });
    }

}
