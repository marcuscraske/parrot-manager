import { Injectable } from '@angular/core';

@Injectable()
export class SyncProfileService
{
    syncProfileService : any;

    constructor()
    {
        this.syncProfileService = (window as any).syncProfileService;
    }

    createTemporaryProfile(type)
    {
        return this.syncProfileService.createTemporaryProfile(type);
    }

    fetch()
    {
        return this.syncProfileService.fetch();
    }

    fetchById(nodeId)
    {
        return this.syncProfileService.fetchById(nodeId);
    }

    save(profile)
    {
        this.syncProfileService.save(profile);
    }

    remove(id)
    {
        this.syncProfileService.remove(id);
    }

}
