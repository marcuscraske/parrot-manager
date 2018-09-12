import { Component, Input } from '@angular/core';

@Component({
    selector: 'changeLog',
    templateUrl: 'changeLog.component.html',
    styleUrls: ['changeLog.component.css']
})
export class ChangeLogComponent
{
    @Input()
    syncResults: any;

    constructor() { }

    trackSyncResults(index, syncResult)
    {
        return syncResult ? syncResult.hostName : null;
    }

    trackLogItems(index, logItem)
    {
        return logItem ? logItem.hashCode() : null;
    }

    getLogItemIcon(logItem)
    {
        var level = logItem.getLevel().toString();
        var icon;

        switch (level)
        {
            case "DEBUG":
                icon="icon-bug";
                break;
            case "ADDED":
                icon="icon-plus";
                break;
            case "REMOVED":
                icon="icon-cross";
                break;
            case "INFO":
                icon="icon-share2";
                break;
            case "ERROR":
            default:
                icon="icon-warning";
                break;
        }

        return icon;
    }

}
