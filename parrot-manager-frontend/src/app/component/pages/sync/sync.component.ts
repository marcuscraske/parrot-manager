import { Component, AfterViewChecked, Renderer } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { DatabaseService } from 'app/service/database.service'
import { ClipboardService } from 'app/service/clipboard.service'
import { SyncService } from 'app/service/sync.service'
import { SyncProfileService } from 'app/service/syncProfile.service'
import { SyncResultService } from 'app/service/syncResult.service'

@Component({
    templateUrl: 'sync.component.html',
    styleUrls: ['sync.component.css']
})
export class SyncComponent implements AfterViewChecked {

    public profiles: any;
    private oldChangeLog: string;
    public syncResults: any;

    private syncResultChangeEvent: Function;
    private syncProfileChangeEvent: Function;

    constructor(
        public syncService: SyncService,
        public syncProfileService: SyncProfileService,
        public syncResultService: SyncResultService,
        public databaseService: DatabaseService,
        public clipboardService: ClipboardService,
        public router: Router,
        public fb: FormBuilder,
        private renderer: Renderer
    ) { }

    ngOnInit()
    {
        // Hook for sync result changes
        this.syncResultChangeEvent = this.renderer.listenGlobal("document", "syncResults.change", (event) => {
            this.syncResults = event.data;
        });

        // Hook for sync profile changes
        this.syncProfileChangeEvent = this.renderer.listenGlobal("document", "syncProfiles.change", (event) => {
            this.profiles = event.data;
         });

        // Fetch last results
        this.syncResults = this.syncResultService.getResults();

        // Fetch profiles
        this.profiles = this.syncProfileService.fetch();
    }

    ngOnDestroy()
    {
        this.syncResultChangeEvent();
        this.syncProfileChangeEvent();
    }

    ngAfterViewChecked()
    {
        // Keeps changelog scrolled to bottom
        var changeLog = $("#changeLog");
        var newChangeLog = changeLog.val();

        if (newChangeLog != this.oldChangeLog)
        {
            changeLog.scrollTop(changeLog[0].scrollHeight - changeLog.height());
            this.oldChangeLog = newChangeLog;
        }
    }

    trackChildren(index, profile)
    {
        return profile ? profile.id : null;
    }

    overwrite(profile)
    {
        this.syncService.overwrite(profile);
    }

    unlock(profile)
    {
        this.syncService.unlock(profile);
    }

    sync(profile, promptForAuth)
    {
        this.syncService.sync(profile, promptForAuth);
    }

    delete(profile)
    {
        this.syncProfileService.delete(profile);
    }

    isSyncing() : boolean
    {
        return this.syncService.isSyncing();
    }

    abort()
    {
        console.log("aborting sync");
        this.syncService.abort();
    }

    copySyncLogToClipboard()
    {
        // Fetch log as text
        var text = this.syncResultService.getResultsAsText();

        // Update clipboard
        this.clipboardService.setText(text);
    }

}
