import { Component } from '@angular/core';
import { DatabaseService } from 'app/service/database.service'
import { RuntimeService } from 'app/service/runtime.service'
import { RecentFileService } from 'app/service/recentFile.service'
import { Router } from '@angular/router';

@Component({
    moduleId: module.id,
    templateUrl: 'open.component.html',
    providers: [DatabaseService, RuntimeService, RecentFileService]
})
export class OpenComponent {

    errorMessage: string;
    recentFiles: any;

    constructor(
        private databaseService: DatabaseService,
        private runtimeService: RuntimeService,
        private recentFileService: RecentFileService,
        private router: Router
    ) { }

    ngOnInit()
    {
        this.recentFiles = this.recentFileService.fetch();
    }

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

    trackChildren(index, recentFile)
    {
        return recentFile ? recentFile.getFileName() : null;
    }

}
