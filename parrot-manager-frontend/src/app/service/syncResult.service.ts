import { Injectable } from '@angular/core';
import { SyncResult } from 'app/model/syncResult'
import { MergeLog } from 'app/model/mergeLog'
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
    getResults() : SyncResult[]
    {
        var results = [];

        var syncResults = this.syncResultService.getResults();

        // Translate each result
        for (var i = 0; i < syncResults.length; i++)
        {
            var syncResult = syncResults[i];

            var result = new SyncResult();
            result.hostName = syncResult.getHostName();
            result.error = syncResult.getError();

            // Translate merge log
            var mergeLog = syncResult.getMergeLog();
            if (mergeLog != null)
            {
                var logItems = mergeLog.getLogItems();

                // Translate log items of each result
                var log = new MergeLog();
                var items = [];
                for (var j = 0; j < logItems.length; j++)
                {
                    var logItem = logItems[j];
                    var item = new LogItem();
                    item.level = logItem.getLevel();
                    item.local = logItem.isLocal();
                    item.text = logItem.getText();
                    items.push(item);
                }
                log.items = items;
                result.mergeLog = log;
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
