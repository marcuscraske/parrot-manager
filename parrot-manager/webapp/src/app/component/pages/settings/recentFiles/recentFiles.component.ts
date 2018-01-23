import { Component, Input } from '@angular/core';
import { RecentFileService } from 'app/service/recentFile.service'

@Component({
    templateUrl: "recentFiles.component.html",
    providers: [RecentFileService],
    selector: "recentFiles"
})
export class RecentFilesComponent
{
    @Input()
    globalSettingsForm: any;

    recentFilesClearEnabled : boolean;

    constructor(
        public recentFileService: RecentFileService,
    ) { }

    ngOnInit()
    {
        // Determine if any recent files
        this.recentFilesClearEnabled = this.recentFileService.isAny();
    }

    globalRecentFilesClear()
    {
        console.log("clearing recent files");

        this.recentFileService.clear();
        this.recentFilesClearEnabled = false;

        toastr.success("Cleared recent files");
    }

}