import { Injectable } from '@angular/core';

@Injectable()
export class RecentFileService {

    recentFileService : any;

    constructor()
    {
        this.recentFileService = (window as any).recentFileService;
    }

    fetch() : any
    {
        var result = this.recentFileService.fetch();
        return result;
    }

}
