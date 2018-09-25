import { Injectable } from '@angular/core';
import { DatabaseService } from 'app/service/database.service'
import { Log } from 'app/model/log'
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
        var log = result.getLog();
        if (log != null)
        {
            var logItems = log.getLogItems();

            var jsonLog = new Log();
            var jsonItems = [];
            for (var i = 0; i < logItems.length; i++)
            {
                var logItem = logItems[i];
                var jsonItem = new LogItem();
                jsonItem.level = logItem.getLevel().toString();
                jsonItem.local = logItem.isLocal();
                jsonItem.text = logItem.getText();
                jsonItems.push(jsonItem);
            }
            jsonLog.items = jsonItems;
            jsonResult.log = jsonLog;
        }
        return jsonResult;
    }

}
