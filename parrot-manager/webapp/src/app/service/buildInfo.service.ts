import { Injectable } from '@angular/core';

@Injectable()
export class BuildInfoService {

    private buildInfoService : any;

    constructor()
    {
        this.buildInfoService = (window as any).buildInfoService;
    }

    getBuildInfo() : string
    {
        var result = this.buildInfoService.getBuildInfo();
        return result;
    }

}
