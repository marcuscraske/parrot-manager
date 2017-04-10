import { Injectable } from '@angular/core';

@Injectable()
export class BackupService {

    backupService : any;

    constructor()
    {
        this.backupService = (window as any).backupService;
    }

    create() : string
    {
        var result = this.backupService.create();
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

}
