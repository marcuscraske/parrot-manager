import { Component, Renderer } from '@angular/core';
import { Router } from '@angular/router';

import { DatabaseService } from 'app/service/database.service'
import { BackupService } from 'app/service/backup.service'

@Component({
  selector: 'backupWarning',
  templateUrl: 'backupWarning.component.html',
  styleUrls: ['backupWarning.component.css']
})
export class BackupWarningComponent
{
    constructor(
        public databaseService: DatabaseService,
        public backupService: BackupService,
        private router: Router
    ) { }

    delete()
    {
        var path = this.backupService.getActualDatabasePath();

        // delete current backup
        this.backupService.deleteCurrentBackup();

        // open main
        this.openMain(path);
    }

    restore()
    {
        var path = this.backupService.getActualDatabasePath();

        // restore this backup as main
        this.backupService.restoreCurrentBackup();

        // open main
        this.openMain(path);
    }

    backToMain()
    {
        var path = this.backupService.getActualDatabasePath();

        // close current database
        this.databaseService.close();

        // open main
        this.openMain(path);
    }

    private openMain(path)
    {
        // navigate to default page
        this.router.navigate(["/"]);

        // prompt for database
        console.log("prompting to open main database - path: " + path);
        this.databaseService.openWithPrompt(path, (message) => {
            // check if failure message
            if (message == null)
            {
                console.log("successfully opened database, redirecting to navigator...");
                this.router.navigate(["/viewer"]);
            }
        });
    }

}
