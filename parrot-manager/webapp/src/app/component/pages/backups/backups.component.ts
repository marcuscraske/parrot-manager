import { Component, Renderer, ChangeDetectorRef, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { DatabaseService } from 'app/service/database.service'
import { BackupService } from 'app/service/backup.service'

@Component({
    templateUrl: "backups.component.html",
    providers: [],
    styleUrls: ["backups.component.css"],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class BackupsComponent {

    // Event for refreshing backups
    backupChangeEvent : Function;

    // Cached array of backup files from backup service (reduces I/O to look for files)
    backupFiles : any;
    errorMessage : string;

    constructor(
        public fb: FormBuilder,
        private databaseService: DatabaseService,
        private backupService: BackupService,
        private router: Router,
        public renderer: Renderer,
        private changeDetectorRef: ChangeDetectorRef
    ) {}

    ngOnInit()
    {
        // Fetch backups for initial view
        this.backupFiles = this.backupService.fetch();

        // Setup event to listen for changes to backups
        this.backupChangeEvent = this.renderer.listenGlobal("document", "backupChange", (event) => {
            this.backupFiles = event.data;
            this.changeDetectorRef.markForCheck();
        });
    }

    ngOnDestroy()
    {
        this.backupChangeEvent();
    }

    create()
    {
        console.log("creating backup...");
        this.errorMessage = this.backupService.create();
    }

    isBackups()
    {
        return this.backupFiles != null && this.backupFiles.length > 0;
    }

    trackChildren(index, file)
    {
        return file ? file.getName() : null;
    }

    open(file)
    {
        var path = file.getPath();
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

    restore(file)
    {
        this.errorMessage = this.backupService.restore(file);

        // re-open database if no errors occurred
        if (this.errorMessage == null)
        {
            var path = this.databaseService.getPath();

            // close current database
            this.databaseService.close();

            // navigate to default page
            this.router.navigate(["/"]);

            // prompt for database
            this.databaseService.openWithPrompt(path, null);
        }
    }

    delete(file)
    {
        this.errorMessage = this.backupService.delete(file);
    }

}
