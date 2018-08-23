import { Injectable } from '@angular/core';

@Injectable()
export class BackupService
{
    backupService : any;

    constructor()
    {
        this.backupService = (window as any).backupService;
    }

    // Creates a backup and returns boolean (true - success/disabled, false - error occurred)
    create() : string
    {
        var result = this.backupService.create();

        // Show notification when error message
        var success = (result == null);

        if (!success)
        {
            toastr.error(result);
        }

        return result;
    }

    fetch() : any
    {
        var result = this.backupService.fetch();
        return result;
    }

    delete(file) : string
    {
        var result = this.backupService.delete(file);
        return result;
    }

    restore(file) : string
    {
        var result = this.backupService.restore(file);
        return result;
    }

    isBackupOpen()
    {
        return this.backupService.isBackupOpen();
    }

    getActualDatabasePath()
    {
        return this.backupService.getActualDatabasePath();
    }

    deleteCurrentBackup()
    {
        this.backupService.deleteCurrentBackup();
    }

    restoreCurrentBackup()
    {
        this.backupService.restoreCurrentBackup();
    }

}
