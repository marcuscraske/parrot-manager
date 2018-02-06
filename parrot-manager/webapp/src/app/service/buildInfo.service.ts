import { Injectable } from '@angular/core';

@Injectable()
export class BuildInfoService {

    private buildInfoService : any;

    constructor()
    {
        this.buildInfoService = (window as any).buildInfoService;
    }

    isJavaOutdated() : boolean
    {
        return this.buildInfoService.isJavaOutdated();
    }

    getBuildInfo() : string
    {
        var result = this.buildInfoService.getBuildInfo();
        return result;
    }

}
