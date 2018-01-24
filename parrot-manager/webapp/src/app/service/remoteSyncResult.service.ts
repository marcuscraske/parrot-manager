import { Injectable } from '@angular/core';

@Injectable()
export class RemoteSyncResultService
{
    remoteSyncResultService : any;

    constructor()
    {
        this.remoteSyncResultService = (window as any).remoteSyncResultService;
    }

    getResults()
    {
        return this.remoteSyncResultService.getResults();
    }

    clear()
    {
        this.remoteSyncResultService.clear();
    }

}
