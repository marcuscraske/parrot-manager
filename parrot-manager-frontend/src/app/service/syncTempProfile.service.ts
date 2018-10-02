import { Injectable } from '@angular/core';

@Injectable()
export class SyncTempProfileService
{
    syncProfileService : any;

    constructor() {
        this.syncProfileService = (window as any).syncProfileService;
    }

    createTemporaryProfile(type)
    {
        return this.syncProfileService.createTemporaryProfile(type);
    }
}
