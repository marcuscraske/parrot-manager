import { Injectable } from '@angular/core';

import { DatabaseService } from 'app/service/database.service'

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

    databaseImportText(options, text)
    {
        var database = this.databaseService.getDatabase();
        return this.importExportService.databaseImportText(database, text, options);
    }

    databaseImportFile(options, path)
    {
        var database = this.databaseService.getDatabase();
        return this.importExportService.databaseImportFile(database, path, options);
    }

    databaseExportText(options)
    {
        var database = this.databaseService.getDatabase();
        return this.importExportService.databaseExportText(database, options);
    }

    databaseExportFile(options, path)
    {
        var database = this.databaseService.getDatabase();
        return this.importExportService.databaseExportFile(database, path, options);
    }

}
