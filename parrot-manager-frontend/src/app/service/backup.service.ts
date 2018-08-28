import { Injectable } from '@angular/core';
import { BackupFile } from 'app/model/backupFile'

@Injectable()
export class BackupService
{
    backupService : any;

    constructor()
    {
        this.backupService = (window as any).backupService;
    }

    // Creates a backup and returns empty (success) or error message
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

    fetch() : BackupFile[]
    {
        var results = [];

        var files = this.backupService.fetch();
        if (files != null && files.length > 0)
        {
            for (var i = 0; i < files.length; i++)
            {
                var file = files[i];
                var result = new BackupFile(file.getName(), file.getPath(), file.getLastModified());
                results.push(result);
            }
        }

        return results;
    }

    delete(path) : string
    {
        var result = this.backupService.delete(path);
        return result;
    }

    restore(path) : string
    {
        var result = this.backupService.restore(path);
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
