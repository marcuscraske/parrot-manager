import { Injectable } from '@angular/core';
import { SyncResult } from 'app/model/syncResult'
import { Log } from 'app/model/log'
import { LogItem } from 'app/model/logItem'

@Injectable()
export class SyncResultService
{
    syncResultService : any;

    constructor()
    {
        this.syncResultService = (window as any).syncResultService;
    }

    // TODO cache
    // TODO should be ab
    // TODO shouldnt be needed, drop
    isSuccess() : boolean
    {
        var success;

        var syncResults = this.syncResultService.getResults();
        if (syncResults.length > 0)
        {
            success = true;
            for (var i = 0; i < syncResults.length; i++)
            {
                var syncResult = syncResults[i];
                success &= syncResult.success;
            }
        }
        else
        {
            success = false;
        }

        return success;
    }

    // TODO cache
    // TODO move translation into shared service
    getResults() : SyncResult[]
    {
        var results = [];

        var syncResults = this.syncResultService.getResults();

        // Translate each result
        for (var i = 0; i < syncResults.length; i++)
        {
            var syncResult = syncResults[i];

            var result = new SyncResult();
            result.profileId = syncResult.getProfileId();
            result.hostName = syncResult.getHostName();

            // Translate merge log
            var log = syncResult.getLog();
            if (log != null)
            {
                var logItems = log.getLogItems();

                // Translate log items of each result
                var jsonLog = new Log();
                var items = [];
                for (var j = 0; j < logItems.length; j++)
                {
                    var logItem = logItems[j];
                    var item = new LogItem();
                    item.level = logItem.getLevel().toString();
                    item.local = logItem.isLocal();
                    item.text = logItem.getText();
                    items.push(item);
                }
                jsonLog.items = items;
                result.log = jsonLog;
            }
            results.push(result);
        }

        return results;
    }

    clear()
    {
        this.syncResultService.clear();
    }

    getResultsAsText()
    {
        return this.syncResultService.getResultsAsText();
    }

}
