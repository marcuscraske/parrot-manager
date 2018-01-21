import { Injectable } from '@angular/core';

@Injectable()
export class BrowserService
{
    browserService: any;

    constructor()
    {
        this.browserService = (window as any).browserService;
    }

    open(key)
    {
        this.browserService.open(key);
    }

}
