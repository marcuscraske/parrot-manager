import { Component } from '@angular/core';
import { DatabaseService } from 'app/service/database.service'
import { RuntimeService } from 'app/service/runtime.service'
import { RecentFileService } from 'app/service/recentFile.service'
import { SettingsService } from 'app/service/settings.service'
import { Router } from '@angular/router';

@Component({
    moduleId: module.id,
    templateUrl: 'open.component.html',
    providers: [RecentFileService, SettingsService],
    styleUrls: ['open.component.css']
})
export class OpenComponent {

    static isStartup : boolean = true;
    errorMessage: string;
    recentFiles: any;

    constructor(
        private databaseService: DatabaseService,
        private runtimeService: RuntimeService,
        private recentFileService: RecentFileService,
        private settingsService: SettingsService,
        private router: Router
    ) { }

    ngOnInit()
    {
        // Fetch recently opened files
        this.refreshRecentFiles();

        // Open database on startup if there's recent files and enabled...
        if (OpenComponent.isStartup && this.recentFiles.length > 0)
        {
            var isEnabled = this.settingsService.fetch("recentFilesOpenLastOnStartup");

            if (isEnabled)
            {
                this.openFile(this.recentFiles[0].getFullPath());
            }
        }

        // Set startup flag, hence the above will only occur on startup
        OpenComponent.isStartup = false;
    }

    refreshRecentFiles()
    {
        this.recentFiles = this.recentFileService.fetch();
    }

    chooseDatabaseFile() : void
    {
        // Open dialogue and read file
        var path = this.runtimeService.pickFile("Open existing database", null, false);
        this.openFile(path);
    }

    openFile(path)
    {
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

    deleteRecentFile(recentFile)
    {
        // delete it...
        this.recentFileService.delete(recentFile);

        // refresh list
        this.refreshRecentFiles();
    }

    trackChildren(index, recentFile)
    {
        return recentFile ? recentFile.getFileName() : null;
    }

}
