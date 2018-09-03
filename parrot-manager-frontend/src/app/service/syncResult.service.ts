import { Injectable } from '@angular/core';

@Injectable()
export class SyncResultService
{
    syncResultService : any;

    constructor()
    {
        this.syncResultService = (window as any).remoteSyncResultService;
    }

    getResults()
    {
        return this.syncResultService.getResults();
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
