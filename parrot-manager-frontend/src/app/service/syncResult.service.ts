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

    getResults() : SyncResult[]
    {
        var results = [];

        var syncResults = this.syncResultService.getResults();

        // Translate each result
        for (var i = 0; i < syncResults.length; i++)
        {
            var syncResult = syncResults[i];
            var mergeLog = syncResult.getMergeLog();
            var logItems = mergeLog.getLogItems();

            var result = new SyncResult();
            result.hostName = syncResult.getHostName();

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
