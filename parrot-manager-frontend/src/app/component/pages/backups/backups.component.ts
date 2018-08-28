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
        this.updateFiles();

        // Setup event to listen for changes to backups
        this.backupChangeEvent = this.renderer.listenGlobal("document", "backupChange", (event) => {
            this.updateFiles();
            this.changeDetectorRef.markForCheck();
        });
    }

    ngOnDestroy()
    {
        this.backupChangeEvent();
    }

    updateFiles()
    {
        this.backupFiles = this.backupService.fetch();
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
        return file ? file.name : null;
    }

    open(file)
    {
        this.openMain(file.path);
    }

    restore(file)
    {
        this.errorMessage = this.backupService.restore(file.path);

        // re-open database if no errors occurred
        if (this.errorMessage == null)
        {
            var path = this.databaseService.getPath();

            // close current database
            this.databaseService.close();

            // navigate to default page
            this.router.navigate(["/"]);

            // prompt for database
            this.openMain(path);
        }
    }

    delete(file)
    {
        this.errorMessage = this.backupService.delete(file.path);
    }

    private openMain(path)
    {
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
