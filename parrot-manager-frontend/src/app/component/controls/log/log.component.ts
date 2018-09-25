import { Component, Input } from '@angular/core';
import { SettingsService } from 'app/service/settings.service'

@Component({
    selector: 'log',
    templateUrl: 'log.component.html',
    styleUrls: ['log.component.css']
})
export class LogComponent
{
    @Input()
    log: any;

    constructor(
        private settingsService: SettingsService
    ) { }

    filterByShowDetails(logItems)
    {
        var result;
        var showDetail = this.settingsService.fetch("mergeLogShowDetail");

        if (!showDetail)
        {
            result = [];
            for (var i = 0; i < logItems.length; i++)
            {
                var logItem = logItems[i];
                if (logItem.level != "DEBUG")
                {
                    result.push(logItem);
                }
            }
        }
        else
        {
            result = logItems;
        }

        return result;
    }

    trackLogItems(index, logItem)
    {
        return logItem ? logItem.message : null;
    }

    getLogItemIcon(logItem)
    {
        var level = logItem.level;
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
            case "WARN":
            default:
                icon="icon-warning";
                break;
        }

        return icon;
    }

}
