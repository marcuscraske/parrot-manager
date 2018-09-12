import { Injectable } from '@angular/core';
import { DatabaseService } from 'app/service/database.service'
import { MergeLog } from 'app/model/mergeLog'
import { LogItem } from 'app/model/logItem'
import { ImportExportResult } from 'app/model/importExportResult'

@Injectable()
export class ImportExportService
{
    importExportService : any;

    constructor(
        private databaseService: DatabaseService
    ) {
        this.importExportService = (window as any).importExportService;
    }

    createOptions(format: string, remoteSync: boolean)
    {
        return this.importExportService.createOptions(format, remoteSync);
    }

    databaseImportText(options, text) : ImportExportResult
    {
        var database = this.databaseService.getDatabase();
        var result = this.importExportService.databaseImportText(database, options, text);
        return this.resultToJson(result);
    }

    databaseImportFile(options, path) : ImportExportResult
    {
        var database = this.databaseService.getDatabase();
        var result = this.importExportService.databaseImportFile(database, options, path);
        return this.resultToJson(result);
    }

    databaseExportText(options) : ImportExportResult
    {
        var database = this.databaseService.getDatabase();
        var result = this.importExportService.databaseExportText(database, options);
        return this.resultToJson(result);
    }

    databaseExportFile(options, path) : ImportExportResult
    {
        var database = this.databaseService.getDatabase();
        var result = this.importExportService.databaseExportFile(database, options, path);
        return this.resultToJson(result);
    }

    resultToJson(result) : ImportExportResult
    {
        var jsonResult = new ImportExportResult();
        jsonResult.text = result.getText();
        jsonResult.error = result.getError();

        // Translate merge log
        var mergeLog = result.getMergeLog();
        if (mergeLog != null)
        {
            var logItems = mergeLog.getLogItems();

            var jsonMergeLog = new MergeLog();
            var jsonItems = [];
            for (var i = 0; i < logItems.length; i++)
            {
                var logItem = logItems[i];
                var jsonItem = new LogItem();
                jsonItem.text = logItem.getText();
                jsonItem.level = logItem.getLevel();
                jsonItems.push(jsonItem);
            }
            jsonMergeLog.items = jsonItems;
            jsonResult.mergeLog = jsonMergeLog;
        }
        return jsonResult;
    }

}
