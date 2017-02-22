import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { BackupService } from 'app/service/backup.service'
import { Router } from '@angular/router';

@Component({
    moduleId: module.id,
    templateUrl: "backups.component.html",
    providers: [BackupService],
    styleUrls: ["backups.component.css"]
})
export class BackupsComponent {

    // Cached array of backup files from backup service (reduces I/O to look for files)
    backupFiles : any;
    errorMessage : string;

    constructor(
        public fb: FormBuilder, private backupService: BackupService,
        private router: Router
    ) {}

    ngOnInit()
    {
        this.refreshBackups();
    }

    create()
    {
        // Create backup
        this.errorMessage = this.backupService.create();

        // Refresh list of backups if no error occurred
        if (this.errorMessage == null)
        {
            this.refreshBackups();
        }
    }

    refreshBackups()
    {
        this.backupFiles = this.backupService.fetch();
    }

}
